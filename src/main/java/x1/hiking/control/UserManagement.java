package x1.hiking.control;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheResult;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.model.User;
import x1.hiking.model.UserNotFoundException;
import x1.hiking.utils.UserCacheKeyGenerator;

/**
 * control of users
 */
@Stateless
@Local
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class UserManagement {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @PersistenceContext
  private EntityManager em;
  
  /** Insert.
   *
   * @param entity the entity
   */
  public void insert(User entity) {
    log.debug("insert user {}", entity);
    em.persist(entity);
  }

  /** Delete.
   *
   * @param entity the entity
   */
  @CacheRemove(afterInvocation = true, cacheName = "user-cache", cacheKeyGenerator = UserCacheKeyGenerator.class)
  public void delete(@CacheKey User entity) {
    log.debug("delete user {}", entity);
    entity = em.merge(entity);
    em.remove(entity);
  }

  /** Update.
   *
   * @param entity the entity
   * @return the user
   */
  @CacheRemove(afterInvocation = false, cacheName = "user-cache")
  @CacheResult(cacheName = "user-cache", cacheKeyGenerator = UserCacheKeyGenerator.class)
  public User update(@CacheKey User entity) {
    log.debug("update user {}", entity);
    return em.merge(entity);
  }

  /**
   * Find user.
   *
   * @param id the id
   * @return the user
   */
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public User findUser(Integer id) {
    return em.find(User.class, id);
  }

  /**
   * Find user.
   *
   * @param email the email
   * @return the user
   * @throws UserNotFoundException 
   */
  @CacheResult(cacheName = "user-cache", cacheKeyGenerator = UserCacheKeyGenerator.class)
  public User findUserByEmail(@CacheKey String email) throws UserNotFoundException {
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
   * Find user.
   *
   * @param token the token
   * @return the user
   * @throws UserNotFoundException 
   */
  public User findUserByToken(@CacheKey String token) throws UserNotFoundException {
    try {
      TypedQuery<User> q = em.createNamedQuery("User.findUserByToken", User.class);
      q.setParameter("token", token);
      return q.getSingleResult();
    } catch (NoResultException e) {
      log.info("No user found for token {}", token);
      throw new UserNotFoundException("No user found for token " + token);
    }
  }

}
