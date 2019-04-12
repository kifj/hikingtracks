package x1.hiking.boundary;

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

  /**
   * validate user session
   * 
   * @param allowPublic
   *          is public access allowed
   * @param request
   *          the request
   * @param response
   *          the response
   * @throws javax.ws.rs.NotAuthorizedException
   * @throws javax.ws.rs.ForbiddenException
   */
  User validateUser(boolean allowPublic, HttpServletRequest request, HttpServletResponse response);
  
  /**
   * validate session token
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @throws javax.ws.rs.NotAuthorizedException
   * @throws javax.ws.rs.ForbiddenException
   */
  void validateToken(HttpServletRequest request, HttpServletResponse response);
}