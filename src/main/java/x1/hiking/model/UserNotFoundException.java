package x1.hiking.model;

/**
 * Exception when user is missing
 * 
 * @author joe
 *
 */
public class UserNotFoundException extends Exception {
  private static final long serialVersionUID = 8963303168325578246L;

  /**
   * @param message the message
   */
  public UserNotFoundException(String message) {
    super(message);
  }

  
}
