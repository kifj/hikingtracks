package x1.hiking.oauth;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.GitHubTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.oltu.oauth2.jwt.JWT;
import org.apache.oltu.oauth2.jwt.io.JWTClaimsSetWriter;
import org.apache.oltu.oauth2.jwt.io.JWTHeaderWriter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.control.UserManagement;
import x1.hiking.model.User;
import x1.hiking.model.UserNotFoundException;
import x1.hiking.utils.AuthorizationConstants;
import x1.hiking.utils.ServletHelper;

/**
 * Authorization servlet
 * 
 * @author joe
 *
 */
public class OAuthClientServlet extends HttpServlet implements AuthorizationConstants {
  private static final long serialVersionUID = -2880212001549684810L;
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Inject
  private OAuthHelper oauthHelper;

  @EJB
  private UserManagement userManagement;

  @Inject
  @ConfigProperty(name = "google.clientid")
  private String googleClientId;

  @Inject
  @ConfigProperty(name = "google.clientsecret")
  private String googleClientSecret;

  @Inject
  @ConfigProperty(name = "github.clientid")
  private String githubClientId;

  @Inject
  @ConfigProperty(name = "github.clientsecret")
  private String githubClientSecret;

  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    doPost(req, resp);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest ,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String logout = request.getParameter(PARAM_LOGOUT);
    String app = request.getParameter(PARAM_URL);
    if (StringUtils.isNotEmpty(logout)) {
      ServletHelper.revokeSessionCookie(response);
      response.sendRedirect(ServletHelper.getBaseUrl(request).build().toString());
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate();
      }
      return;
    }
    if (request.getParameter(PARAM_AUTHENTICATE) == null) {
      String wanted = ServletHelper.getSessionCookieValue(request, PARAM_AUTH_TOKEN);
      if (wanted != null) {
        User user = authUser(wanted);
        if (user != null) {
          String redirectUrl = URLDecoder.decode(getState(request), StandardCharsets.UTF_8.name());
          log.info("Authenticated {}, redirect to: {}", user, redirectUrl);
          ServletHelper.injectSessionCookie(response, user.getToken());
          response.sendRedirect(redirectUrl);
          return;
        }
      }
      if (app == null) {
        log.warn("Missing identifier");
        response.sendRedirect(loginPage(request).build().toString());
        return;
      }
      authRequest(OAuthProviderType.valueOf(app), request, response);
    } else {
      if (app == null) {
        log.warn("Missing identifier");
        response.sendRedirect(loginPage(request).build().toString());
        return;
      }
      OAuthParams oauthParams = verifyResponse(OAuthProviderType.valueOf(app), request, response);
      if (oauthParams.getAccessToken() != null) {
        String redirectUrl;
        if (oauthParams.getState() != null) {
          redirectUrl = URLDecoder.decode(oauthParams.getState(), StandardCharsets.UTF_8.name());
        } else {
          redirectUrl = ServletHelper.getBaseUrl(request).build().toString();
        }
        ServletHelper.injectSessionCookie(response, oauthParams.getAccessToken());
        log.info("Authentification successful, redirect to: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
      } else if (!response.isCommitted()) {
        response.sendRedirect(loginPage(request).build().toString());
      }
    }
  }

  private void authRequest(OAuthProviderType oauthProvider, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    OAuthParams oauthParams = buildOAuthParams(oauthProvider, request);
    try {
      oauthParams.setState(getState(request));
      oauthHelper.validateAuthorizationParams(oauthParams);
      OAuthClientRequest oauthRequest = OAuthClientRequest.authorizationLocation(oauthParams.getAuthzEndpoint())
          .setClientId(oauthParams.getClientId()).setRedirectURI(oauthParams.getRedirectUri())
          .setResponseType(ResponseType.CODE.toString()).setScope(oauthParams.getScope())
          .setState(oauthParams.getState()).buildQueryMessage();
      response.sendRedirect(oauthRequest.getLocationUri());
      log.info("Requesting authorization {}", oauthRequest.getLocationUri());
    } catch (OAuthSystemException | OAuthProblemException e) {
      oauthParams.setErrorMessage(e.getMessage());
      log.warn(null, e);
      URI redirect = loginPage(request).queryParam(PARAM_MESSAGE, oauthParams.getErrorMessage()).build();
      response.sendRedirect(redirect.toString());
    }
  }

  private UriBuilder loginPage(HttpServletRequest request) {
    return ServletHelper.getBaseUrl(request).path(LOGIN_PAGE);
  }

  private String getState(HttpServletRequest request) throws MalformedURLException {
    String referer = request.getHeader(PARAM_REFERER);
    if (StringUtils.isNotEmpty(referer)) {
      Map<String, String> queryParams = ServletHelper.parseQueryString(new URL(referer));
      if (StringUtils.isNotEmpty(queryParams.get(PARAM_FROM))) {
        return queryParams.get(PARAM_FROM);
      }
    }
    return "/";
  }

  private OAuthParams buildOAuthParams(OAuthProviderType oauthProvider, HttpServletRequest request) {
    OAuthParams oauthParams = new OAuthParams();
    oauthParams.setApplication(oauthProvider.name());
    oauthParams.setAuthzEndpoint(oauthProvider.getAuthzEndpoint());
    oauthParams.setTokenEndpoint(oauthProvider.getTokenEndpoint());
    oauthParams.setRedirectUri(buildReturnToUrl(oauthProvider, request));
    switch (oauthProvider) {
    case GOOGLE:
      oauthParams.setClientId(googleClientId);
      oauthParams.setClientSecret(googleClientSecret);
      oauthParams.setScope("openid email");
      break;
    case GITHUB:
      oauthParams.setClientId(githubClientId);
      oauthParams.setClientSecret(githubClientSecret);
      oauthParams.setScope("user:email");
      break;
    default:
      throw new NotImplementedException("No implementation for " + oauthProvider + " available");
    }
    return oauthParams;
  }

  /**
   * configure the return_to URL where your application will receive the
   * authentication responses from the OAuth provider
   */
  private String buildReturnToUrl(OAuthProviderType oauthProvider, HttpServletRequest request) {
    UriBuilder b = ServletHelper.getRequestUrl(request);
    b.queryParam(PARAM_AUTHENTICATE, Boolean.TRUE);
    b.queryParam(PARAM_URL, oauthProvider.name());
    return b.build().toString();
  }

  private OAuthParams verifyResponse(OAuthProviderType oauthProvider, HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    OAuthParams oauthParams = buildOAuthParams(oauthProvider, request);
    try {
      oauthParams.setState(request.getParameter(STATE));
      oauthParams.setAuthzCode(request.getParameter("code"));
      oauthParams.setRedirectUri(buildReturnToUrl(oauthProvider, request));
      OAuthClientRequest oauthRequest = OAuthClientRequest.tokenLocation(oauthParams.getTokenEndpoint())
          .setClientId(oauthParams.getClientId()).setClientSecret(oauthParams.getClientSecret())
          .setRedirectURI(oauthParams.getRedirectUri()).setCode(oauthParams.getAuthzCode())
          .setGrantType(GrantType.AUTHORIZATION_CODE).buildBodyMessage();

      oauthHelper.validateTokenParams(oauthParams);
      log.debug("Requesting token: {}" + oauthRequest.getBody());

      OAuthClient client = new OAuthClient(new URLConnectionClient());
      Class<? extends OAuthAccessTokenResponse> cl = getOAuthAccessTokenResponse(oauthProvider);
      OAuthAccessTokenResponse oauthResponse = client.accessToken(oauthRequest, cl);
      client.shutdown();

      oauthParams.setAccessToken(oauthResponse.getAccessToken());
      oauthParams.setExpiresIn(oauthResponse.getExpiresIn());
      oauthParams.setRefreshToken(oauthResponse.getRefreshToken());
      oauthParams.setScope(oauthResponse.getScope());

      switch (oauthProvider) {
      case GOOGLE:
        verifyResponse(oauthParams, (OpenIdConnectResponse) oauthResponse, "email");
        break;
      case GITHUB:
        verifyResponse(oauthParams, (GitHubTokenResponse) oauthResponse);
        break;
      default:
        throw new NotImplementedException(oauthProvider.name());
      }
    } catch (OAuthSystemException | OAuthProblemException e) {
      oauthParams.setErrorMessage(e.getMessage());
      log.warn(null, e);
      URI redirect = loginPage(request).queryParam(PARAM_MESSAGE, "Login failed").build();
      response.sendRedirect(redirect.toString());
    }
    return oauthParams;
  }

  private void verifyResponse(OAuthParams oauthParams, OpenIdConnectResponse openIdConnectResponse, String emailField)
      throws MalformedURLException {
    JWT idToken = openIdConnectResponse.getIdToken();
    oauthParams.setIdToken(idToken.getRawString());

    oauthParams.setHeader(new JWTHeaderWriter().write(idToken.getHeader()));
    oauthParams.setClaimsSet(new JWTClaimsSetWriter().write(idToken.getClaimsSet()));
    URL url = new URL(oauthParams.getTokenEndpoint());
    oauthParams.setIdTokenValid(openIdConnectResponse.checkId(url.getHost(), oauthParams.getClientId()));
    String email = idToken.getClaimsSet().getCustomField(emailField, String.class);
    log.debug("Access Token: {}, Email: {}", oauthParams.getAccessToken(), email);
    if (oauthParams.isIdTokenValid() && StringUtils.isNotEmpty(email)) {
      Date expires = null;
      if (oauthParams.getExpiresIn() != null) {
        expires = DateUtils.addSeconds(new Date(), oauthParams.getExpiresIn().intValue());
      }
      checkUser(oauthParams.getAccessToken(), email, expires, null);
    } else {
      oauthParams.setAccessToken(null);
    }
  }

  private void verifyResponse(OAuthParams oauthParams, GitHubTokenResponse githubTokenResponse)
      throws OAuthProblemException, OAuthSystemException {
    OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest("https://api.github.com/user")
        .setAccessToken(oauthParams.getAccessToken()).buildQueryMessage();
    OAuthClient client = new OAuthClient(new URLConnectionClient());

    OAuthResourceResponse resourceResponse = client.resource(bearerClientRequest, OAuth.HttpMethod.GET,
        OAuthResourceResponse.class);
    String body = resourceResponse.getBody();
    try (JsonReader reader = Json.createReader(new StringReader(body))) {
      JsonObject userData = reader.readObject();
      String email = userData.getString("email");
      log.debug("Access Token: {}, Email: {}", oauthParams.getAccessToken(), email);
      if (StringUtils.isNotEmpty(email)) {
        Date expires = null;
        if (oauthParams.getExpiresIn() != null) {
          expires = DateUtils.addSeconds(new Date(), oauthParams.getExpiresIn().intValue());
        }
        String name = userData.getString("name");
        checkUser(oauthParams.getAccessToken(), email, expires, name);
      } else {
        oauthParams.setAccessToken(null);
      }
    }
    client.shutdown();
  }

  private User checkUser(String token, String email, Date expires, String name) {
    User user;
    try {
      user = userManagement.login(email, token, expires);
    } catch (UserNotFoundException e) {
      user = new User();
      user.setEmail(email);
      user.setToken(token);
      user.setExpires(expires);
      user.setName(name);
      userManagement.insert(user);
    }
    return user;
  }

  private User authUser(String token) {
    if (token != null) {
      try {
        User user = userManagement.findUserByToken(token);
        if (user.getExpires() == null || user.getExpires().before(new Date())) {
          return null;
        }
        return user;
      } catch (UserNotFoundException e) {
        return null;
      }
    }
    return null;
  }

  private Class<? extends OAuthAccessTokenResponse> getOAuthAccessTokenResponse(OAuthProviderType oauthProvider) {
    Class<? extends OAuthAccessTokenResponse> cl;
    switch (oauthProvider) {
    case FACEBOOK:
    case GITHUB:
      cl = GitHubTokenResponse.class;
      break;
    case GOOGLE:
      cl = OpenIdConnectResponse.class;
      break;
    default:
      cl = OAuthJSONAccessTokenResponse.class;
      break;
    }
    return cl;
  }
}
