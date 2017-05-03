package x1.hiking.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AbstractJpaDAO.
 *
 * @param <T> the generic type
 * @author joe
 */
public abstract class AbstractJpaDAO<T extends Serializable> implements JpaDAO<T> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  protected Logger getLog() {
    return log;
  }
  
  /**
   * Sets the clazz.
   *
   * @param clazzToSet the new clazz
   */
  public void setClazz(final Class<T> clazzToSet) {
    this.clazz = clazzToSet;
  }

  /* (non-Javadoc)
   * @see x1.hiking.dao.JpaDAO#find(java.lang.Integer)
   */
  @Override
  public T find(final Integer id) {
    return entityManager.find(clazz, id);
  }

  /* (non-Javadoc)
   * @see x1.hiking.dao.JpaDAO#findAll()
   */
  @Override
  public List<T> findAll() {
    return entityManager.createQuery("from " + clazz.getName(), clazz).getResultList();
  }

  /* (non-Javadoc)
   * @see x1.hiking.dao.JpaDAO#persist(T)
   */
  @Override
  public void persist(final T entity) {
    entityManager.persist(entity);
  }

  /* (non-Javadoc)
   * @see x1.hiking.dao.JpaDAO#merge(T)
   */
  @Override
  public T merge(final T entity) {
    return entityManager.merge(entity);
  }

  /* (non-Javadoc)
   * @see x1.hiking.dao.JpaDAO#remove(T)
   */
  @Override
  public void remove(final T entity) {
    entityManager.remove(entity);
  }
  
  /* (non-Javadoc)
   * @see x1.hiking.dao.JpaDAO#refresh(T)
   */
  @Override
  public void refresh(final T entity) {
    entityManager.refresh(entity);
  }
  
  /* (non-Javadoc)
   * @see x1.hiking.dao.JpaDAO#flush(T)
   */
  @Override
  public T flush(final T entity) {
    entityManager.flush();
    return entity;
  }

  /* (non-Javadoc)
   * @see x1.hiking.dao.JpaDAO#remove(java.lang.Integer)
   */
  @Override
  public void remove(final Integer entityId) {
    final T entity = find(entityId);
    remove(entity);
  }
  
  protected TypedQuery<T> createNamedQuery(String name) {
    return getEntityManager().createNamedQuery(name, clazz);
  }
  
  /** get the entity manager */
  protected EntityManager getEntityManager() {
    return entityManager;
  }
  
  private Class<T> clazz;

  @PersistenceContext
  private EntityManager entityManager;
}
