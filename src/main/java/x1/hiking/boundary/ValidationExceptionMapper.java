package x1.hiking.boundary;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.stream.Collectors;

/**
 * Exception mapper for validations
 *
 * @author joe
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

  /*
   * (non-Javadoc)
   *
   * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
   */
  @Override
  public Response toResponse(ConstraintViolationException e) {
    String body = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage)
        .collect(Collectors.joining("\n"));
    return Response.status(Status.BAD_REQUEST).entity(body).type(MediaType.TEXT_PLAIN).build();
  }
}