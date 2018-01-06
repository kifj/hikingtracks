package x1.hiking.control;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.model.ActivityType;
import x1.hiking.model.Bounds;
import x1.hiking.model.Coord;
import x1.hiking.model.Geolocation;
import x1.hiking.model.Image;
import x1.hiking.model.Track;
import x1.hiking.model.TrackData;
import x1.hiking.model.User;

/**
 * management of tracks and track data
 * 
 * @author joe
 */
@Stateless
@Local
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TrackServiceBean implements TrackService {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private static final String PARAM_ID = "id";
  private static final String PARAM_TRACK = "track";
  private static final String PARAM_NAME = "name";
  private static final String PARAM_USER = "user";

  @PersistenceContext
  private EntityManager em;

  @Resource
  private SessionContext ctx;

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#persist(x1.hiking.model.Track)
   */
  @Override
  public void insert(Track entity) {
    log.debug("insert track {}", entity);
    if (entity.isPublished()) {
      entity.setPublishDate(new Date());
    } else {
      entity.setPublishDate(null);
    }
    entity.setUser(em.merge(entity.getUser()));
    em.persist(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#remove(x1.hiking.model.Track)
   */
  @Override
  public void delete(Track entity) {
    log.debug("delete track {}", entity);
    entity = em.merge(entity);
    em.remove(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#merge(x1.hiking.model.Track)
   */
  @Override
  public Track update(Track entity) {
    log.debug("update track {}", entity);
    if (entity.isPublished() && entity.getPublishDate() == null) {
      entity.setPublishDate(new Date());
    } else {
      entity.setPublishDate(null);
    }
    return em.merge(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#findTrack(java.lang.Integer)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Track findTrack(Integer id) {
    return em.find(Track.class, id);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#nextTrack(x1.hiking.model.Track,
   * x1.hiking.model.User)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Track nextTrack(Track track, User user) {
    TypedQuery<Track> q;
    if (user == null) {
      q = em.createNamedQuery("Track.nextPublicTrack", Track.class);
    } else {
      q = em.createNamedQuery("Track.nextTrack", Track.class);
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
   * @see
   * x1.hiking.service.HikingTracksService#previousTrack(x1.hiking.model.Track,
   * x1.hiking.model.User)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Track previousTrack(Track track, User user) {
    TypedQuery<Track> q;
    if (user == null) {
      q = em.createNamedQuery("Track.previousPublicTrack", Track.class);
    } else {
      q = em.createNamedQuery("Track.previousTrack", Track.class);
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
   * @see x1.hiking.service.HikingTracksService#findTracks(x1.hiking.model.User,
   * String)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public List<Track> findTracks(User user, String text, QueryOptions options) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Track> q = cb.createQuery(Track.class);
    Root<Track> from = q.from(Track.class);
    Join<Track, Geolocation> join = from.join(Track.ATTR_GEOLOCATION, JoinType.LEFT);
    ActivityType activity = null;
    if (options != null && options.getActivity() != null) {
      activity = options.getActivity();
    }
    buildWhereClause(cb, q, from, join, user, text, activity);
    buildOrderByClause(cb, q, from, text);
    q.distinct(true);
    return getResultList(options, q);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#findTracks(String, QueryOptions)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public List<Track> findTracks(String text, QueryOptions options) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Track> q = cb.createQuery(Track.class);
    Root<Track> from = q.from(Track.class);
    Join<Track, Geolocation> join = from.join(Track.ATTR_GEOLOCATION, JoinType.LEFT);
    ActivityType activity = null;
    if (options != null && options.getActivity() != null) {
      activity = options.getActivity();
    }
    buildWhereClause(cb, q, from, join, Boolean.TRUE, text, activity);
    buildOrderByClause(cb, q, from, text);
    q.distinct(true);
    return getResultList(options, q);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#findTracks(String, Bounds,
   * QueryOptions)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public List<Track> findTracks(String text, Bounds bounds, QueryOptions options) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Track> q = cb.createQuery(Track.class);
    Root<Track> from = q.from(Track.class);
    Join<Track, Geolocation> join = from.join(Track.ATTR_GEOLOCATION, JoinType.LEFT);
    ActivityType activity = null;
    if (options != null && options.getActivity() != null) {
      activity = options.getActivity();
    }
    Expression<Boolean> where = buildWhereClause(cb, q, from, join, Boolean.TRUE, text, activity);
    addBoundsClause(cb, q, from, where, bounds);
    buildOrderByClause(cb, q, from, text);
    q.distinct(true);
    List<Track> tracks = getResultList(options, q);
    for (Track track : tracks) {
      track.setTrackData(withoutData(track));
    }
    return tracks;
  }

  private List<Track> getResultList(QueryOptions options, CriteriaQuery<Track> q) {
    TypedQuery<Track> query = em.createQuery(q);
    applyQueryOptions(options, query);
    return query.getResultList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#countTracks(x1.hiking.model.User,
   * java.lang.String, x1.hiking.model.ActivityType)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Long countTracks(User user, String text, ActivityType activity) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Long> q = cb.createQuery(Long.class);
    Root<Track> from = q.from(Track.class);
    Join<Track, Geolocation> join = from.join(Track.ATTR_GEOLOCATION, JoinType.LEFT);
    q.select(cb.countDistinct(from.get(Track.ATTR_ID)));
    buildWhereClause(cb, q, from, join, user, text, activity);
    TypedQuery<Long> query = em.createQuery(q);
    return query.getSingleResult();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#countTracks(java.lang.String,
   * x1.hiking.model.ActivityType)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Long countTracks(String text, ActivityType activity) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Long> q = cb.createQuery(Long.class);
    Root<Track> from = q.from(Track.class);
    Join<Track, Geolocation> join = from.join(Track.ATTR_GEOLOCATION, JoinType.LEFT);
    q.select(cb.countDistinct(from.get(Track.ATTR_ID)));
    buildWhereClause(cb, q, from, join, Boolean.TRUE, text, activity);
    TypedQuery<Long> query = em.createQuery(q);
    return query.getSingleResult();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#findTrack(x1.hiking.model.User,
   * java.lang.String, boolean)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Track findTrack(User user, String name, boolean loadAll) {
    Track t = ctx.getBusinessObject(TrackService.class).findTrack(user, name);
    if (t != null && loadAll) {
      t.setImages(ctx.getBusinessObject(TrackService.class).loadImages(t));
      t.setTrackData(ctx.getBusinessObject(TrackService.class).loadTrackData(t));
    }
    return t;
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.control.TrackService#findTrack(x1.hiking.model.User,
   * java.lang.String)
   */
  @Override
  public Track findTrack(User user, String name) {
    TypedQuery<Track> q = em.createNamedQuery("Track.findTrackByUserAndName", Track.class);
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
   * @see x1.hiking.service.HikingTracksService#findTrack(String, boolean)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Track findTrack(String name, boolean loadAll) {
    Track t = ctx.getBusinessObject(TrackService.class).findTrack(name);
    if (t != null && loadAll) {
      t.setImages(ctx.getBusinessObject(TrackService.class).loadImages(t));
      t.setTrackData(ctx.getBusinessObject(TrackService.class).loadTrackData(t));
    }
    return t;
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.control.TrackService#findTrack(java.lang.String)
   */
  @Override
  public Track findTrack(String name) {
    TypedQuery<Track> q = em.createNamedQuery("Track.findPublicTrackByName", Track.class);
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
   * @see
   * x1.hiking.service.HikingTracksService#findTrackData(x1.hiking.model.User,
   * java.lang.String, java.lang.Integer)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public TrackData findTrackData(User user, String name, Integer id) {
    TypedQuery<TrackData> q;
    if (user != null) {
      q = em.createNamedQuery("TrackData.findTrackDataByUserAndNameAndId", TrackData.class);
      q.setParameter(PARAM_NAME, name);
      q.setParameter(PARAM_USER, user);
      q.setParameter(PARAM_ID, id);
    } else {
      q = em.createNamedQuery("TrackData.findPublicTrackDataByNameAndId", TrackData.class);
      q.setParameter(PARAM_NAME, name);
      q.setParameter(PARAM_ID, id);
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
   * @see x1.hiking.service.HikingTracksService#insert(x1.hiking.model.TrackData)
   */
  @Override
  public void insert(TrackData entity) {
    entity.getTrack().setGeolocationAvailable(null);
    entity.setTrack(em.merge(entity.getTrack()));
    em.persist(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#update(x1.hiking.model.TrackData)
   */
  @Override
  public TrackData update(TrackData entity) {
    return em.merge(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#delete(x1.hiking.model.TrackData)
   */
  @Override
  public void delete(TrackData entity) {
    entity = em.merge(entity);
    em.remove(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.TrackService#find(x1.hiking.model.Track)
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public List<TrackData> loadTrackData(Track track) {
    TypedQuery<TrackData> q = em.createNamedQuery("TrackData.getTrackData", TrackData.class);
    q.setParameter(PARAM_TRACK, track);
    return q.getResultList();
  }

  private List<TrackData> withoutData(Track track) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<TrackData> q = cb.createQuery(TrackData.class);
    Root<TrackData> from = q.from(TrackData.class);
    Expression<Boolean> where = cb.equal(from.get(TrackData.ATTR_TRACK), track);
    q.where(where);
    // in order of the argument list in the constructor, leaving out the expensive
    // data
    q.multiselect(from.get(TrackData.ATTR_ID), from.get(TrackData.ATTR_VERSION), from.get(TrackData.ATTR_NAME),
        from.get(TrackData.ATTR_TRACK), from.get(TrackData.ATTR_START_POINT), from.get(TrackData.ATTR_END_POINT),
        from.get(TrackData.ATTR_LOWEST_POINT), from.get(TrackData.ATTR_HIGHEST_POINT), from.get(TrackData.ATTR_URL));
    TypedQuery<TrackData> query = em.createQuery(q);
    return query.getResultList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.control.TrackService#loadImages(x1.hiking.model.Track)
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public List<Image> loadImages(Track track) {
    TypedQuery<Image> q = em.createNamedQuery("Image.getImages", Image.class);
    q.setParameter(PARAM_TRACK, track);
    return q.getResultList();
  }

  private void buildWhereClause(CriteriaBuilder cb, CriteriaQuery<?> q, Root<Track> from, Join<Track, Geolocation> join,
      User user, String text, ActivityType activity) {
    Expression<Boolean> where = cb.equal(from.get(Track.ATTR_USER), user);
    where = buildSearchClause(cb, from, join, text, where);
    if (activity != null) {
      where = cb.and(where, cb.equal(from.get(Track.ATTR_ACTIVITY), activity));
    }
    q.where(where);
  }

  private Expression<Boolean> buildWhereClause(CriteriaBuilder cb, CriteriaQuery<?> q, Root<Track> from,
      Join<Track, Geolocation> join, Boolean published, String text, ActivityType activity) {
    Expression<Boolean> where = cb.equal(from.get(Track.ATTR_PUBLISHED), published);
    where = buildSearchClause(cb, from, join, text, where);
    if (activity != null) {
      where = cb.and(where, cb.equal(from.get(Track.ATTR_ACTIVITY), activity));
    }
    q.where(where);
    return where;
  }

  private Expression<Boolean> buildSearchClause(CriteriaBuilder cb, Root<Track> from, Join<Track, Geolocation> join,
      String text, Expression<Boolean> where) {
    if (!StringUtils.isEmpty(text)) {
      String value = "%" + text + "%";
      Predicate p = cb.or(cb.like(from.get(Track.ATTR_NAME), value), cb.like(from.get(Track.ATTR_LOCATION), value),
          cb.like(join.get(Geolocation.ATTR_LOCATION), value), cb.like(join.get(Geolocation.ATTR_AREA), value),
          cb.like(join.get(Geolocation.ATTR_COUNTRY), value));
      where = cb.and(where, p);
    }
    return where;
  }

  private void addBoundsClause(CriteriaBuilder cb, CriteriaQuery<Track> q, Root<Track> from, Expression<Boolean> where,
      Bounds bounds) {
    Join<Track, TrackData> join = from.join(Track.ATTR_TRACK_DATA, JoinType.LEFT);
    Predicate p1 = cb.and(cb.le(join.get(TrackData.ATTR_START_POINT).get(Coord.ATTR_LNG), bounds.getEast()),
        cb.ge(join.get(TrackData.ATTR_START_POINT).get(Coord.ATTR_LNG), bounds.getWest()),
        cb.le(join.get(TrackData.ATTR_START_POINT).get(Coord.ATTR_LAT), bounds.getNorth()),
        cb.ge(join.get(TrackData.ATTR_START_POINT).get(Coord.ATTR_LAT), bounds.getSouth()));
    Predicate p2 = cb.and(cb.le(join.get(TrackData.ATTR_HIGHEST_POINT).get(Coord.ATTR_LNG), bounds.getEast()),
        cb.ge(join.get(TrackData.ATTR_HIGHEST_POINT).get(Coord.ATTR_LNG), bounds.getWest()),
        cb.le(join.get(TrackData.ATTR_HIGHEST_POINT).get(Coord.ATTR_LAT), bounds.getNorth()),
        cb.ge(join.get(TrackData.ATTR_HIGHEST_POINT).get(Coord.ATTR_LAT), bounds.getSouth()));
    Predicate p3 = cb.and(cb.le(join.get(TrackData.ATTR_END_POINT).get(Coord.ATTR_LNG), bounds.getEast()),
        cb.ge(join.get(TrackData.ATTR_END_POINT).get(Coord.ATTR_LNG), bounds.getWest()),
        cb.le(join.get(TrackData.ATTR_END_POINT).get(Coord.ATTR_LAT), bounds.getNorth()),
        cb.ge(join.get(TrackData.ATTR_END_POINT).get(Coord.ATTR_LAT), bounds.getSouth()));
    Predicate p4 = cb.and(cb.le(from.get(Track.ATTR_LONGITUDE), bounds.getEast()),
        cb.ge(from.get(Track.ATTR_LONGITUDE), bounds.getWest()),
        cb.le(from.get(Track.ATTR_LATITUDE), bounds.getNorth()),
        cb.ge(from.get(Track.ATTR_LATITUDE), bounds.getSouth()));
    q.where(cb.and(where, cb.or(p1, p2, p3, p4)));
  }

  private void buildOrderByClause(CriteriaBuilder cb, CriteriaQuery<?> q, Root<Track> from, String text) {
    if (!StringUtils.isEmpty(text)) {
      q.orderBy(cb.asc(from.get(Track.ATTR_NAME)));
    } else {
      q.orderBy(cb.desc(from.get(Track.ATTR_ID)));
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
