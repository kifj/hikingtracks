package x1.hiking.oauth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import x1.hiking.model.User;

/**
 * Session validator
 * 
 * @author joe
 *
 */
public interface SessionValidator {
  String MSG_MISSING_TOKEN = "x1.hiking.boundary.SessionValidator.missing_auth_token.message";
  String ERROR_INVALID_TOKEN = "invalid_token";
  
  /**
   * validate user session
   * 
   * @param allowPublic
   *          is public access allowed
   * @param request
   *          the request
   * @param response
   *          the response
   * @throws javax.ws.rs.NotAuthorizedException when not authorized
   * @throws javax.ws.rs.ForbiddenException when not allowed
   */
  User validateUser(boolean allowPublic, HttpServletRequest request, HttpServletResponse response);
  
  /**
   * validate session token
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @throws javax.ws.rs.NotAuthorizedException when not authorized
   * @throws javax.ws.rs.ForbiddenException when not allowed
   */
  void validateToken(HttpServletRequest request, HttpServletResponse response);
}
