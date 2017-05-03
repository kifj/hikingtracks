package x1.hiking.dao;

import java.util.Date;
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
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import x1.hiking.model.ActivityType;
import x1.hiking.model.Bounds;
import x1.hiking.model.Geolocation;
import x1.hiking.model.Image;
import x1.hiking.model.Track;
import x1.hiking.model.TrackData;
import x1.hiking.model.User;

/**
 * The Track DAO.
 *
 * @author joe
 */
@Stateless
@Local
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TrackDAOImpl extends AbstractJpaDAO<Track> implements TrackDAO {

  private static final String PARAM_ID = "id";
  private static final String PARAM_TRACK = "track";
  private static final String PARAM_NAME = "name";
  private static final String PARAM_USER = "user";

  /**
   * Instantiates a new track dao.
   */
  public TrackDAOImpl() {
    super.setClazz(Track.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDAO#findTracks(x1.hiking.model.User,
   * java.lang.String, x1.hiking.service.QueryOptions)
   */
  @Override
  public List<Track> findTracks(User user, String text, QueryOptions options) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Track> q = cb.createQuery(Track.class);
    Root<Track> from = q.from(Track.class);
    Join<Track, Geolocation> join = from.join("geolocation", JoinType.LEFT);
    ActivityType activity = null;
    if (options != null && options.getActivity() != null) {
      activity = options.getActivity();
    }
    buildWhereClause(cb, q, from, join, user, text, activity);
    buildOrderByClause(cb, q, from, text);
    q.distinct(true);
    TypedQuery<Track> query = getEntityManager().createQuery(q);
    applyQueryOptions(options, query);
    return query.getResultList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDAO#findTracks(java.lang.String,
   * x1.hiking.model.Bounds, x1.hiking.dao.QueryOptions)
   */
  @Override
  public List<Track> findTracks(String text, Bounds bounds, QueryOptions options) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Track> q = cb.createQuery(Track.class);
    Root<Track> from = q.from(Track.class);
    Join<Track, Geolocation> join = from.join("geolocation", JoinType.LEFT);
    ActivityType activity = null;
    if (options != null && options.getActivity() != null) {
      activity = options.getActivity();
    }
    Expression<Boolean> where = buildWhereClause(cb, q, from, join, Boolean.TRUE, text, activity);
    addBoundsClause(cb, q, from, where, bounds);
    buildOrderByClause(cb, q, from, text);
    q.distinct(true);
    TypedQuery<Track> query = getEntityManager().createQuery(q);
    applyQueryOptions(options, query);
    return query.getResultList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDAO#findTracks(java.lang.String,
   * x1.hiking.service.QueryOptions)
   */
  @Override
  public List<Track> findTracks(String text, QueryOptions options) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Track> q = cb.createQuery(Track.class);
    Root<Track> from = q.from(Track.class);
    Join<Track, Geolocation> join = from.join("geolocation", JoinType.LEFT);
    ActivityType activity = null;
    if (options != null && options.getActivity() != null) {
      activity = options.getActivity();
    }
    buildWhereClause(cb, q, from, join, Boolean.TRUE, text, activity);
    buildOrderByClause(cb, q, from, text);
    q.distinct(true);
    TypedQuery<Track> query = getEntityManager().createQuery(q);
    applyQueryOptions(options, query);
    return query.getResultList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDAO#countTracks(x1.hiking.model.User,
   * java.lang.String, x1.hiking.model.ActivityType)
   */
  @Override
  public Long countTracks(User user, String text, ActivityType activity) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Long> q = cb.createQuery(Long.class);
    Root<Track> from = q.from(Track.class);
    Join<Track, Geolocation> join = from.join("geolocation", JoinType.LEFT);
    q.select(cb.countDistinct(from.get("id")));
    buildWhereClause(cb, q, from, join, user, text, activity);
    TypedQuery<Long> query = getEntityManager().createQuery(q);
    return query.getSingleResult();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDAO#countTracks(java.lang.String,
   * x1.hiking.model.ActivityType)
   */
  @Override
  public Long countTracks(String text, ActivityType activity) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Long> q = cb.createQuery(Long.class);
    Root<Track> from = q.from(Track.class);
    Join<Track, Geolocation> join = from.join("geolocation", JoinType.LEFT);
    q.select(cb.countDistinct(from.get("id")));
    buildWhereClause(cb, q, from, join, Boolean.TRUE, text, activity);
    TypedQuery<Long> query = getEntityManager().createQuery(q);
    return query.getSingleResult();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDAO#findTrack(x1.hiking.model.User,
   * java.lang.String)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Track findTrack(User user, String name) {
    TypedQuery<Track> q = createNamedQuery("Track.findTrackByUserAndName");
    q.setParameter(PARAM_NAME, name);
    q.setParameter(PARAM_USER, user);
    try {
      return q.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDAO#findTrack(java.lang.String)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Track findTrack(String name) {
    TypedQuery<Track> q = createNamedQuery("Track.findPublicTrackByName");
    q.setParameter(PARAM_NAME, name);
    try {
      return q.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDAO#next(x1.hiking.model.Track,
   * x1.hiking.model.User)
   */
  @Override
  public Track next(Track track, User user) {
    TypedQuery<Track> q;
    if (user == null) {
      q = createNamedQuery("Track.nextPublicTrack");
    } else {
      q = createNamedQuery("Track.nextTrack");
      q.setParameter(PARAM_USER, user);
    }
    q.setMaxResults(1);
    q.setParameter(PARAM_ID, track.getId());
    try {
      return q.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDAO#previous(x1.hiking.model.Track,
   * x1.hiking.model.User)
   */
  @Override
  public Track previous(Track track, User user) {
    TypedQuery<Track> q;
    if (user == null) {
      q = createNamedQuery("Track.previousPublicTrack");
    } else {
      q = createNamedQuery("Track.previousTrack");
      q.setParameter(PARAM_USER, user);
    }
    q.setMaxResults(1);
    q.setParameter(PARAM_ID, track.getId());
    try {
      return q.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDAO#findFirstImage(x1.hiking.model.Track)
   */
  @Override
  public Image findFirstImage(Track track) {
    TypedQuery<Image> q = getEntityManager().createNamedQuery("Image.getImages", Image.class);
    q.setParameter(PARAM_TRACK, track);
    q.setMaxResults(1);
    List<Image> images = q.getResultList();
    if (images.isEmpty()) {
      return null;
    }
    return images.get(0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.AbstractJpaDAO#remove(java.io.Serializable)
   */
  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDAO#remove(x1.hiking.model.Track)
   */
  @Override
  public void remove(Track entity) {
    entity = merge(entity);
    super.remove(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.AbstractJpaDAO#persist(java.io.Serializable)
   */
  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDAO#persist(x1.hiking.model.Track)
   */
  @Override
  public void persist(Track entity) {
    if (entity.isPublished()) {
      entity.setPublishDate(new Date());
    } else {
      entity.setPublishDate(null);
    }
    entity.setUser(getEntityManager().merge(entity.getUser()));
    super.persist(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.JpaDAO#merge(T)
   */
  @Override
  public Track merge(Track entity) {
    if (entity.isPublished() && entity.getPublishDate() == null) {
      entity.setPublishDate(new Date());
    } else {
      entity.setPublishDate(null);
    }
    return super.merge(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.TrackDAO#findTracksForGeolocationUpdate(int)
   */
  @Override
  public List<Track> findTracksForGeolocationUpdate(int maxResults) {
    TypedQuery<Track> q = createNamedQuery("Track.findTracksForGeolocationUpdate");
    q.setMaxResults(maxResults);
    return q.getResultList();
  }

  private void buildWhereClause(CriteriaBuilder cb, CriteriaQuery<?> q, Root<Track> from, Join<Track, Geolocation> join,
      User user, String text, ActivityType activity) {
    Expression<Boolean> where = cb.equal(from.get("user"), user);
    where = buildSearchClause(cb, from, join, text, where);
    if (activity != null) {
      where = cb.and(where, cb.equal(from.get("activity"), activity));
    }
    q.where(where);
  }

  private Expression<Boolean> buildWhereClause(CriteriaBuilder cb, CriteriaQuery<?> q, Root<Track> from,
      Join<Track, Geolocation> join, Boolean published, String text, ActivityType activity) {
    Expression<Boolean> where = cb.equal(from.get("published"), published);
    where = buildSearchClause(cb, from, join, text, where);
    if (activity != null) {
      where = cb.and(where, cb.equal(from.get("activity"), activity));
    }
    q.where(where);
    return where;
  }

  private Expression<Boolean> buildSearchClause(CriteriaBuilder cb, Root<Track> from, Join<Track, Geolocation> join,
      String text, Expression<Boolean> where) {
    if (!StringUtils.isEmpty(text)) {
      String value = "%" + text + "%";
      Predicate p = cb.or(cb.like(from.get("name"), value), cb.like(from.get("location"), value),
          cb.like(join.get("location"), value), cb.like(join.get("area"), value), cb.like(join.get("country"), value));
      where = cb.and(where, p);
    }
    return where;
  }

  private void addBoundsClause(CriteriaBuilder cb, CriteriaQuery<Track> q, Root<Track> from, Expression<Boolean> where,
      Bounds bounds) {
    Join<Track, TrackData> join = from.join("trackData", JoinType.LEFT);
    Predicate p = cb.and(
        cb.le(join.get("startPoint").get("lng"), bounds.getEast()),
        cb.ge(join.get("startPoint").get("lng"), bounds.getWest()),
        cb.le(join.get("startPoint").get("lat"), bounds.getNorth()),
        cb.ge(join.get("startPoint").get("lat"), bounds.getSouth()));
    q.where(cb.and(where, p));    
  }

  private void buildOrderByClause(CriteriaBuilder cb, CriteriaQuery<?> q, Root<Track> from, String text) {
    if (!StringUtils.isEmpty(text)) {
      q.orderBy(cb.asc(from.get("name")));
    } else {
      q.orderBy(cb.desc(from.get("id")));
    }
  }

  private void applyQueryOptions(QueryOptions options, TypedQuery<Track> q) {
    if (options != null) {
      if (options.getStartPosition() > 0) {
        q.setFirstResult(options.getStartPosition());
      }
      q.setMaxResults(options.getMaxResults());
    }
  }

}
