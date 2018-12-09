package x1.hiking.boundary;

import java.util.Date;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;

import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.control.UserManagement;
import x1.hiking.model.TokenExpiredException;
import x1.hiking.model.User;
import x1.hiking.model.UserNotFoundException;
import x1.hiking.utils.AuthorizationConstants;
import x1.hiking.utils.ServletHelper;

/**
 * Session Validator Implementation
 * 
 * @author joe
 * 
 */
@Named("sessionValidator")
public class SessionValidatorImpl implements AuthorizationConstants, SessionValidator {
  private final Logger log = LoggerFactory.getLogger(getClass());

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
    String token = checkToken(false, request);
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
    String token = checkToken(allowPublic, request);
    if (token == null && allowPublic) {
      return null;
    }
    return checkUser(allowPublic, request, response, token);
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
      throw new ForbiddenException(MSG_MISSING_TOKEN + PARAM_AUTH_TOKEN);
    } catch (UserNotFoundException e) {
      String realm = ServletHelper.getRequestUrl(request).build().getHost();
      String challenge = String.format("Bearer realm=\"%s\", error=\"%s\", error_description=\"%s\"", realm,
          MSG_INVALID_TOKEN, token);
      throw new NotAuthorizedException(challenge);
    }
  }

  private String checkToken(boolean allowPublic, HttpServletRequest request) {
    String token = ServletHelper.getSessionCookieValue(request, AuthorizationConstants.PARAM_AUTH_TOKEN);
    if (token == null) {
      token = request.getParameter(AuthorizationConstants.PARAM_AUTH_TOKEN);
    }
    if (token == null) {
      if (allowPublic) {
        return null;
      }
      throw new ForbiddenException(MSG_MISSING_TOKEN + PARAM_AUTH_TOKEN);
    }
    return token;
  }

  @EJB
  private UserManagement userManagement;

}
