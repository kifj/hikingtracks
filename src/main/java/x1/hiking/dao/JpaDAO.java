package x1.hiking.dao;

import java.io.Serializable;
import java.util.List;

/**
 * The JPA DAO interface
 *
 * @param <T> the generic type
 * @author joe
 */

public interface JpaDAO<T extends Serializable> {

  /**
   * Find.
   *
   * @param id the id
   * @return the t
   */
  T find(final Integer id);

  /**
   * Find all.
   *
   * @return the list
   */
  List<T> findAll();

  /**
   * Persist.
   *
   * @param entity the entity
   */
  void persist(final T entity);

  /**
   * Merge.
   *
   * @param entity the entity
   * @return the t
   */
  T merge(final T entity);

  /**
   * Removes the entity.
   *
   * @param entity the entity
   */
  void remove(final T entity);

  /**
   * Refresh.
   *
   * @param entity the entity
   */
  void refresh(final T entity);

  /**
   * Flush.
   *
   * @param entity the entity
   * @return the t
   */
  T flush(final T entity);

  /**
   * Removes the entity by ID
   *
   * @param entityId the entity id
   */
  void remove(final Integer entityId);

}