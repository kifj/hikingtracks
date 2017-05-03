package x1.hiking.dao;

import x1.hiking.model.User;
import x1.hiking.model.UserNotFoundException;

/** The User DAO.
 * 
 * @author joe
 *
 */
public interface UserDAO extends JpaDAO<User> {

  /**
   * Find user.
   *
   * @param email the email
   * @return the user
   * @throws UserNotFoundException 
   */
  User findUserByEmail(String email) throws UserNotFoundException;

  /**
   * Find user.
   *
   * @param token the token
   * @return the user
   * @throws UserNotFoundException 
   */
  User findUserByToken(String token) throws UserNotFoundException;

}