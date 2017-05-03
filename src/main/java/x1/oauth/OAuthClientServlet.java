package x1.oauth;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Map;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.ServletException;
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
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.GitHubTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.oltu.oauth2.jwt.JWT;
import org.apache.oltu.oauth2.jwt.io.JWTClaimsSetWriter;
import org.apache.oltu.oauth2.jwt.io.JWTHeaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.model.User;
import x1.hiking.model.UserNotFoundException;
import x1.hiking.service.HikingTracksService;
import x1.hiking.utils.ConfigurationValue;

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
  private HikingTracksService service;

  @Inject
  @ConfigurationValue(key = "google.clientid")
  private String googleClientId;

  @Inject
  @ConfigurationValue(key = "google.clientsecret")
  private String googleClientSecret;

  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doPost(req, resp);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
   * , javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String logout = request.getParameter(PARAM_LOGOUT);
    String app = request.getParameter(PARAM_URL);
    if (StringUtils.isNotEmpty(logout)) {
      ServletHelper.revokeSessionCookie(response);
      response.sendRedirect(ServletHelper.getBaseUrl(request));
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
          String redirectUrl = URLDecoder.decode(getState(request), UTF_8);
          log.info("Authenticated {}, redirect to: {}", user, redirectUrl);
          ServletHelper.injectSessionCookie(response, user.getToken());
          response.sendRedirect(redirectUrl);
          return;
        }
      }
      if (app == null) {
        log.warn("Missing identifier");
        response.sendRedirect(ServletHelper.getBaseUrl(request) + LOGIN_PAGE);
        return;
      }
      authRequest(OAuthProviderType.valueOf(app), request, response);
    } else {
      if (app == null) {
        log.warn("Missing identifier");
        response.sendRedirect(ServletHelper.getBaseUrl(request) + LOGIN_PAGE);
        return;
      }
      OAuthParams oauthParams = verifyResponse(OAuthProviderType.valueOf(app), request, response);
      if (oauthParams.getAccessToken() != null) {
        String redirectUrl;
        if (oauthParams.getState() != null) {
          redirectUrl = URLDecoder.decode(oauthParams.getState(), UTF_8);
        } else {
          redirectUrl = ServletHelper.getBaseUrl(request);
        }
        ServletHelper.injectSessionCookie(response, oauthParams.getAccessToken());
        log.info("Authentification successful, redirect to: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
      } else if (!response.isCommitted()) {
          response.sendRedirect(ServletHelper.getBaseUrl(request) + LOGIN_PAGE);        
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
      URI redirect = UriBuilder.fromUri(ServletHelper.getBaseUrl(request) + LOGIN_PAGE)
          .queryParam("message", oauthParams.getErrorMessage()).build();
      response.sendRedirect(redirect.toString());
    }
  }
  
  private String getState(HttpServletRequest request) throws MalformedURLException {
    String referer = request.getHeader(PARAM_REFERER);
    if (StringUtils.isNotEmpty(referer)) {
      URL url = new URL(referer);
      Map<String, String> queryParams = ServletHelper.parseQueryString(url.getQuery());
      if (StringUtils.isNotEmpty(queryParams.get(PARAM_FROM))) {
        return queryParams.get(PARAM_FROM);
      }
    }
    return null;
  }

  private OAuthParams buildOAuthParams(OAuthProviderType oauthProvider, HttpServletRequest request)
      throws MalformedURLException {
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
    default:
      throw new NotImplementedException("No implementation for " + oauthProvider + " available");
    }
    return oauthParams;
  }

  /*
   * configure the return_to URL where your application will receive the
   * authentication responses from the OAuth provider
   */
  private String buildReturnToUrl(OAuthProviderType oauthProvider, HttpServletRequest request)
      throws MalformedURLException {
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
      oauthParams.setAccessToken(oauthResponse.getAccessToken());
      oauthParams.setExpiresIn(oauthResponse.getExpiresIn());
      oauthParams.setRefreshToken(oauthResponse.getRefreshToken());
      oauthParams.setScope(oauthResponse.getScope());

      if (oauthProvider == OAuthProviderType.GOOGLE) {
        OpenIdConnectResponse openIdConnectResponse = (OpenIdConnectResponse) oauthResponse;
        JWT idToken = openIdConnectResponse.getIdToken();
        oauthParams.setIdToken(idToken.getRawString());

        oauthParams.setHeader(new JWTHeaderWriter().write(idToken.getHeader()));
        oauthParams.setClaimsSet(new JWTClaimsSetWriter().write(idToken.getClaimsSet()));
        URL url = new URL(oauthParams.getTokenEndpoint());
        oauthParams.setIdTokenValid(openIdConnectResponse.checkId(url.getHost(), oauthParams.getClientId()));
        String email = idToken.getClaimsSet().getCustomField("email", String.class);
        log.debug("Access Token: {}, Email: {}", oauthParams.getAccessToken(), email);
        if (oauthParams.isIdTokenValid() && StringUtils.isNotEmpty(email)) {
          Date expiryDate = null;
          if (oauthParams.getExpiresIn() != null) {
            expiryDate = DateUtils.addSeconds(new Date(), oauthParams.getExpiresIn().intValue());                   
          }
          checkUser(oauthParams.getAccessToken(), email, expiryDate);
        }
      }
    } catch (OAuthSystemException | OAuthProblemException e) {
      oauthParams.setErrorMessage(e.getMessage());
      log.warn(null, e);
      URI redirect = UriBuilder.fromUri(ServletHelper.getBaseUrl(request) + LOGIN_PAGE)
          .queryParam("message", "Login failed").build();
      response.sendRedirect(redirect.toString());
    }
    return oauthParams;
  }

  private User checkUser(String token, String email, Date expires) {
    if (email == null) {
      return null;
    }
    User user;
    try {
      user = service.findUserByEmail(email);
      user.setToken(token);
      user.setExpires(expires);
      user = service.update(user);
    } catch (UserNotFoundException e) {
      user = new User();
      user.setEmail(email);
      user.setToken(token);
      user.setExpires(expires);
      service.insert(user);
    }
    return user;
  }

  private User authUser(String token) {
    if (token != null) {
      try {
        User user = service.findUserByToken(token);
        if (user.getExpires() == null || user.getExpires().before(new Date())) {
          return null;
        }
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
