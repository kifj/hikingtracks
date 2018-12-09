package x1.hiking.model;

/**
 * Exception when the token is expired
 * 
 * @author joe
 *
 */
public class TokenExpiredException extends Exception {
  private static final long serialVersionUID = -6288771066435028546L;

  /**
   * @param message the message
   */
  public TokenExpiredException(String message) {
    super(message);
  }  
}
