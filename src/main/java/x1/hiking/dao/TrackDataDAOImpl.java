package x1.hiking.dao;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import x1.hiking.model.Track;
import x1.hiking.model.TrackData;
import x1.hiking.model.User;

/**
 * The TrackData DAO implementation.
 *
 * @author joe
 */
@Stateless
@Local
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TrackDataDAOImpl extends AbstractJpaDAO<TrackData> implements TrackDataDAO {

  /**
   * Instantiates a new track data dao.
   */
  public TrackDataDAOImpl() {
    super.setClazz(TrackData.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDataDAO#findTrackData(x1.hiking.model.User,
   * java.lang.String, java.lang.Integer)
   */
  @Override
  public TrackData findTrackData(final User user, final String name, final Integer id) {
    TypedQuery<TrackData> q;
    if (user != null) {
      q = createNamedQuery("TrackData.findTrackDataByUserAndNameAndId");
      q.setParameter("name", name);
      q.setParameter("user", user);
      q.setParameter("id", id);
    } else {
      q = createNamedQuery("TrackData.findPublicTrackDataByNameAndId");
      q.setParameter("name", name);
      q.setParameter("id", id);
    }
    try {
      return q.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.AbstractJpaDAO#persist(java.io.Serializable)
   */
  @Override
  public void persist(TrackData entity) {
    entity.setTrack(getEntityManager().merge(entity.getTrack()));
    super.persist(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.AbstractJpaDAO#remove(java.io.Serializable)
   */
  @Override
  public void remove(TrackData entity) {
    entity = merge(entity);
    super.remove(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDataDAO#find(x1.hiking.model.Track)
   */
  @Override
  public List<TrackData> find(Track track) {
    TypedQuery<TrackData> q = createNamedQuery("TrackData.getTrackData");
    q.setParameter("track", track);
    return q.getResultList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDataDAO#getWithoutData(x1.hiking.model.Track)
   */
  @Override
  public List<TrackData> withoutData(Track track) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<TrackData> q = cb.createQuery(TrackData.class);
    Root<TrackData> from = q.from(TrackData.class);
    Expression<Boolean> where = cb.equal(from.get("track"), track);
    q.where(where);
    // in order of the argument list in the constructor, leaving out the
    // expensive data
    q.multiselect(from.get("id"), from.get("version"), from.get("name"), from.get("track"), from.get("startPoint"),
        from.get("endPoint"), from.get("lowestPoint"), from.get("highestPoint"), from.get("url"));
    TypedQuery<TrackData> query = getEntityManager().createQuery(q);
    return query.getResultList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDataDAO#findTrackDataForUpdate(int)
   */
  @Override
  public List<TrackData> findTrackDataForUpdate(int maxResults) {
    TypedQuery<TrackData> q = createNamedQuery("TrackData.findTrackDataForLocationUpdate");
    q.setMaxResults(maxResults);
    return q.getResultList();
  }
}
