package x1.hiking.rest;

import java.util.Date;

import javax.ejb.EJB;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.model.User;
import x1.hiking.model.UserNotFoundException;
import x1.hiking.service.HikingTracksService;
import x1.hiking.utils.ServletHelper;
import x1.oauth.AuthorizationConstants;

/**
 * Session Validator Implementation
 * 
 * @author joe
 * 
 */
@Named("sessionValidator")
public class SessionValidatorImpl implements AuthorizationConstants, SessionValidator {
  private final Logger log = LoggerFactory.getLogger(getClass());

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.SessionValidator#validateUser(boolean,
   * javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public User validateUser(boolean allowPublic, HttpServletRequest request, HttpServletResponse response) {
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

    try {
      log.trace("get user for token: {}", token);
      User user = hikingTrackService.findUserByToken(token);
      if (user == null || user.getExpires() == null || user.getExpires().compareTo(new Date()) < 0) {
        if (allowPublic) {
          return user;
        }
        throw new ForbiddenException(MSG_MISSING_TOKEN + PARAM_AUTH_TOKEN);
      }
      ServletHelper.injectSessionCookie(response, token);
      return user;
    } catch (UserNotFoundException e) {
      String realm = ServletHelper.getRequestUrl(request).build().getHost();
      String challenge = String.format("Bearer realm=\"%s\", error=\"%s\", error_description=\"%s\"",
          realm, MSG_INVALID_TOKEN, token);
      throw new NotAuthorizedException(challenge);
    }
  }

  @EJB
  private HikingTracksService hikingTrackService;
}
