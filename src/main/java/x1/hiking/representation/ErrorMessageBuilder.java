package x1.hiking.representation;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static javax.ws.rs.core.Response.Status.*;

import x1.hiking.boundary.ConflictException;

/**
 * helper class for error reponses
 */
public final class ErrorMessageBuilder {
  private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("ValidationMessages");

  private ErrorMessageBuilder() {
  }

  public static BadRequestException badRequest(String messageKey, Object... args) {
    return new BadRequestException(errorResponse(BAD_REQUEST, getMessageTemplate(messageKey), args));
  }

  public static NotFoundException notFound(String messageKey, Object... args) {
    return new NotFoundException(errorResponse(NOT_FOUND, getMessageTemplate(messageKey), args));
  }

  public static ConflictException conflict(String messageKey, Object... args) {
    return new ConflictException(errorResponse(CONFLICT, getMessageTemplate(messageKey), args));
  }

  public static ForbiddenException forbidden(String messageKey, Object... args) {
    return new ForbiddenException(errorResponse(FORBIDDEN, getMessageTemplate(messageKey), args));
  }

  public static NotAuthorizedException notAuthorized(String challenge) {
    return new NotAuthorizedException(challenge);
  }

  private static String getMessageTemplate(String messageKey) {
    return MESSAGES.getString(messageKey);
  }

  private static Response errorResponse(Response.Status status, String messageTemplate, Object... args) {
    return Response.status(status).type(MediaType.TEXT_PLAIN).entity(MessageFormat.format(messageTemplate, args))
        .build();
  }
}
