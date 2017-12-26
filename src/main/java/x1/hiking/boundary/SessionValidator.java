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
  String MSG_MISSING_TOKEN = "Missing Auth Token: ";
  String MSG_INVALID_TOKEN = "Invalid Auth Token";

  /**
   * validate user session
   * 
   * @param allowPublic
   *          is public access allowed
   * @param request
   *          the request
   * @param response
   *          the response
   */
  User validateUser(boolean allowPublic, HttpServletRequest request, HttpServletResponse response);

}