package x1.hiking.control;

import java.util.Date;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.model.TokenExpiredException;
import x1.hiking.model.User;
import x1.hiking.model.UserNotFoundException;

/**
 * control of users
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class UserManagement {
  private final Logger log = LoggerFactory.getLogger(UserManagement.class);

  @PersistenceContext
  private EntityManager em;
  
  /** Insert.
   *
   * @param entity the entity
   */
  public User insert(@NotNull @Valid User entity) {
    log.debug("insert user {}", entity);
    em.persist(entity);
    return entity;
  }

  /** Delete.
   *
   * @param entity the entity
   */
  public void delete(@NotNull User entity) {
    log.debug("delete user {}", entity);
    entity = em.merge(entity);
    em.remove(entity);
  }

  /** Update.
   *
   * @param entity the entity
   * @return the user
   */
  public User update(@NotNull @Valid User entity) {
    log.debug("update user {}", entity);
    return em.merge(entity);
  }

  /**
   * Login a given user with token
   * @return the user
   */
 @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 public User login(@NotNull String email, @NotNull String token, Date expires) throws UserNotFoundException {
   User user = findUserByEmail(email);
   user.setToken(token);
   user.setExpires(expires);

   log.debug("update user {}", user);
   return em.merge(user);
 }

  /**
   * Find user.
   *
   * @param id the id
   * @return the user
   */
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public User findUser(@NotNull Integer id) {
    return em.find(User.class, id);
  }

  /**
   * Find user.
   *
   * @param email the email
   * @return the user
   * @throws UserNotFoundException if user is not found
   */
  public User findUserByEmail(@NotNull String email) throws UserNotFoundException {
    try {
      TypedQuery<User> q = em.createNamedQuery("User.findUserByEmail", User.class);
      q.setParameter("email", email);
      return q.getSingleResult();
    } catch (NoResultException e) {
      log.info("No user found for email {}", email);
      throw new UserNotFoundException("No user found for email " + email);
    }
  }

  /**
   * Find user by token. 
   *
   * @param token the token
   * @return the user
   * @throws UserNotFoundException if user is not found
   * @throws TokenExpiredException if token is expired
   */
  public User findUserByToken(@NotNull String token) throws UserNotFoundException, TokenExpiredException  {
    try {
      TypedQuery<User> q = em.createNamedQuery("User.findUserByToken", User.class);
      q.setParameter("token", token);
      User user = q.getSingleResult();
      if (user.getExpires() == null || user.getExpires().before(new Date())) {
        throw new TokenExpiredException("token expired: " + user.getExpires());
      }
      return user;
    } catch (NoResultException e) {
      log.info("No user found for token {}", token);
      throw new UserNotFoundException("No user found for token " + token);
    }
  }

}
