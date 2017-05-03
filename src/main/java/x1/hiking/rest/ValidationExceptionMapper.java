package x1.hiking.rest;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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
    StringBuilder response = new StringBuilder();
    boolean isFirst = true;
    for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
      if (!isFirst) {
        response.append("\n");
      }
      response.append(violation.getMessage());
      isFirst = false;
    }
    return Response.status(Status.BAD_REQUEST).entity(response.toString()).type(MediaType.TEXT_PLAIN).build();
  }
}