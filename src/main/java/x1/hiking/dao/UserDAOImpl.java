package x1.hiking.dao;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import x1.hiking.model.User;
import x1.hiking.model.UserNotFoundException;

/**
 * The Class UserDAO.
 *
 * @author joe
 */
@Stateless
@Local
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class UserDAOImpl extends AbstractJpaDAO<User> implements UserDAO {
  
  /**
   * Instantiates a new user dao.
   */
  public UserDAOImpl() {
    super.setClazz(User.class);
  }

  /* (non-Javadoc)
   * @see x1.hiking.dao.UserDA#findUser(java.lang.String)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public User findUserByEmail(String email) throws UserNotFoundException {
    try {
      TypedQuery<User> q = createNamedQuery("User.findUserByEmail");
      q.setParameter("email", email);
      return q.getSingleResult();
    } catch (NoResultException e) {
      getLog().info("No user found for email {}", email);
      throw new UserNotFoundException("No user found for email " + email);
    }
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.dao.UserDAO#findUserByToken(java.lang.String)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public User findUserByToken(String token) throws UserNotFoundException {
    try {
      TypedQuery<User> q = createNamedQuery("User.findUserByToken");
      q.setParameter("token", token);
      return q.getSingleResult();
    } catch (NoResultException e) {
      getLog().info("No user found for token {}", token);
      throw new UserNotFoundException("No user found for token " + token);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.AbstractJpaDAO#remove(java.io.Serializable)
   */
  /* (non-Javadoc)
   * @see x1.hiking.dao.UserDA#remove(x1.hiking.model.User)
   */
  @Override
  public void remove(User entity) {
    entity = merge(entity);
    super.remove(entity);
  }
}
