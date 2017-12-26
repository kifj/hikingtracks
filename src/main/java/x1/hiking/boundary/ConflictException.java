package x1.hiking.boundary;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

/** Exception for Status Code 409
 * 
 * @author joe
 */
public class ConflictException extends ClientErrorException {
  private static final long serialVersionUID = 5186896208237583061L;

  /**
   * Create a HTTP 409 (Conflict) exception.
   */
  public ConflictException() {
    super(Response.Status.CONFLICT);
  }

  /**
   * Create a HTTP 409 (Conflict) exception.
   * 
   * @param message
   *          the String that is the entity of the 400 response.
   */
  public ConflictException(String message) {
    super(message, Response.Status.CONFLICT);
  }
}