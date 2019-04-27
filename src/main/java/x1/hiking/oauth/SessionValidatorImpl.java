package x1.hiking.oauth;

import static x1.hiking.representation.ErrorMessageBuilder.*;

import java.util.Date;
import java.util.Optional;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.control.UserManagement;
import x1.hiking.model.TokenExpiredException;
import x1.hiking.model.User;
import x1.hiking.model.UserNotFoundException;
import x1.hiking.utils.ServletHelper;

/**
 * Session Validator Implementation
 * 
 * @author joe
 * 
 */
@Named("sessionValidator")
public class SessionValidatorImpl implements SessionValidator {
  private static final String MSG_OAUTH_CHALLENGE = "Bearer realm=\"%s\", error=\"%s\", error_description=\"%s\"";

  private final Logger log = LoggerFactory.getLogger(SessionValidatorImpl.class);

  @Inject
  @Named("user-cache")
  private Cache<String, Date> cache;

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.boundary.SessionValidator#validateToken(javax.servlet.http.
   * HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void validateToken(HttpServletRequest request, HttpServletResponse response) {
    String token = checkToken(false, request)
        .orElseThrow(() -> forbidden(MSG_MISSING_TOKEN, ServletHelper.PARAM_AUTH_TOKEN));
    Date expires = cache.get(token);
    if (expires == null || expires.before(new Date())) {
      User user = validateUser(false, request, response);
      cache.put(token, user.getExpires());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.boundary.SessionValidator#validateUser(boolean,
   * javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public User validateUser(boolean allowPublic, HttpServletRequest request, HttpServletResponse response) {
    Optional<String> token = checkToken(allowPublic, request);
    if (!token.isPresent() && allowPublic) {
      return null;
    }
    return checkUser(allowPublic, request, response, token.get());
  }

  private User checkUser(boolean allowPublic, HttpServletRequest request, HttpServletResponse response, String token) {
    try {
      log.trace("get user for token: {}", token);
      User user = userManagement.findUserByToken(token);
      ServletHelper.injectSessionCookie(response, token);
      return user;
    } catch (TokenExpiredException e) {
      if (allowPublic) {
        return null;
      }
      String realm = ServletHelper.getRequestUrl(request).build().getHost();
      String challenge = String.format(MSG_OAUTH_CHALLENGE, realm, ERROR_INVALID_TOKEN, "The access token expired");
      throw notAuthorized(challenge);
    } catch (UserNotFoundException e) {
      String realm = ServletHelper.getRequestUrl(request).build().getHost();
      String challenge = String.format(MSG_OAUTH_CHALLENGE, realm, ERROR_INVALID_TOKEN,
          "The access token is not valid");
      throw notAuthorized(challenge);
    }
  }

  private Optional<String> checkToken(boolean allowPublic, HttpServletRequest request) {
    String token = ServletHelper.getSessionCookieValue(request, ServletHelper.PARAM_AUTH_TOKEN);
    if (token == null) {
      token = request.getParameter(ServletHelper.PARAM_AUTH_TOKEN);
    }
    if (token == null) {
      if (allowPublic) {
        return Optional.empty();
      }
      throw forbidden(MSG_MISSING_TOKEN, ServletHelper.PARAM_AUTH_TOKEN);
    }
    return Optional.of(token);
  }

  @EJB
  private UserManagement userManagement;

}
