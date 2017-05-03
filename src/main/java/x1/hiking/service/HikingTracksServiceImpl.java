package x1.hiking.service;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.dao.ImageDAO;
import x1.hiking.dao.ImageDataDAO;
import x1.hiking.dao.QueryOptions;
import x1.hiking.dao.TrackDAO;
import x1.hiking.dao.TrackDataDAO;
import x1.hiking.dao.UserDAO;
import x1.hiking.model.ActivityType;
import x1.hiking.model.Bounds;
import x1.hiking.model.Image;
import x1.hiking.model.ImageData;
import x1.hiking.model.Track;
import x1.hiking.model.TrackData;
import x1.hiking.model.User;
import x1.hiking.model.UserNotFoundException;

/**
 * HikingTracks Service: internal data access service
 * 
 * @author joe
 * 
 */
@Stateless
@Local
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class HikingTracksServiceImpl implements HikingTracksService {
  private final Logger log = LoggerFactory.getLogger(getClass());

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#persist(x1.hiking.model.User)
   */
  @Override
  public void insert(User entity) {
    log.debug("insert user {}", entity);
    userDAO.persist(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#remove(x1.hiking.model.User)
   */
  @Override
  public void delete(User entity) {
    log.debug("delete user {}", entity);
    userDAO.remove(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#merge(x1.hiking.model.User)
   */
  @Override
  public User update(User entity) {
    log.debug("update user {}", entity);
    return userDAO.merge(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#findUser(java.lang.Integer)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public User findUser(Integer id) {
    return userDAO.find(id);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#persist(x1.hiking.model.Track)
   */
  @Override
  public void insert(Track entity) {
    log.debug("insert track {}", entity);
    trackDAO.persist(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#remove(x1.hiking.model.Track)
   */
  @Override
  public void delete(Track entity) {
    log.debug("delete track {}", entity);
    trackDAO.remove(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#merge(x1.hiking.model.Track)
   */
  @Override
  public Track update(Track entity) {
    log.debug("update track {}", entity);
    return trackDAO.merge(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#findTrack(java.lang.Integer)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Track findTrack(Integer id) {
    return trackDAO.find(id);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#nextTrack(x1.hiking.model.Track,
   * x1.hiking.model.User)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Track nextTrack(Track track, User user) {
    return trackDAO.next(track, user);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * x1.hiking.service.HikingTracksService#previousTrack(x1.hiking.model.Track,
   * x1.hiking.model.User)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Track previousTrack(Track track, User user) {
    return trackDAO.previous(track, user);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * x1.hiking.service.HikingTracksService#findUserByEmail(java.lang.String)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public User findUserByEmail(String email) throws UserNotFoundException {
    return userDAO.findUserByEmail(email);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#findUser(java.lang.String)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public User findUserByToken(String token) throws UserNotFoundException {
    return userDAO.findUserByToken(token);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#findTracks(x1.hiking.model.User,
   * String)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public List<Track> findTracks(User user, String text, QueryOptions options) {
    return trackDAO.findTracks(user, text, options);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#findTracks(String, QueryOptions)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public List<Track> findTracks(String text, QueryOptions options) {
    return trackDAO.findTracks(text, options);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#findTracks(String, Bounds, QueryOptions)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public List<Track> findTracks(String text, Bounds bounds, QueryOptions options) {
    List<Track> tracks = trackDAO.findTracks(text, bounds, options);
    for (Track track : tracks) {
        track.setTrackData(trackDataDAO.withoutData(track));
    }
    return tracks;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * x1.hiking.service.HikingTracksService#countTracks(x1.hiking.model.User,
   * java.lang.String, x1.hiking.model.ActivityType)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Long countTracks(User user, String text, ActivityType activity) {
    return trackDAO.countTracks(user, text, activity);
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
    return trackDAO.countTracks(text, activity);
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
    Track t = trackDAO.findTrack(user, name);
    if (t != null && loadAll) {
      t.setImages(imageDAO.find(t));
      t.setTrackData(trackDataDAO.find(t));
    }
    return t;
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#findTrack(String, boolean)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Track findTrack(String name, boolean loadAll) {
    Track t = trackDAO.findTrack(name);
    if (t != null && loadAll) {
      t.setImages(imageDAO.find(t));
      t.setTrackData(trackDataDAO.find(t));
    }
    return t;
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#findImage(x1.hiking.model.User,
   * java.lang.String, java.lang.Integer)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Image findImage(User user, String name, Integer id) {
    return imageDAO.findImage(user, name, id);
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
    return trackDataDAO.findTrackData(user, name, id);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#insert(x1.hiking.model.Image)
   */
  @Override
  public void insert(Image entity) {
    imageDAO.persist(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#insert(x1.hiking.model.Image,
   * byte[])
   */
  @Override
  public void insert(Image entity, byte[] data) {
    imageDAO.persist(entity);
    insertImageData(entity, data);
  }

  private ImageData insertImageData(Image entity, byte[] data) {
    if (data != null && data.length > 0) {
      ImageData imageData = new ImageData();
      imageData.setData(data);
      imageData.setImage(entity);
      imageDataDAO.persist(imageData);
      return imageData;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#update(x1.hiking.model.Image)
   */
  @Override
  public Image update(Image entity) {
    return imageDAO.merge(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#update(x1.hiking.model.Image,
   * byte[])
   */
  @Override
  public Image update(Image entity, byte[] data) {
    ImageData imageData = imageDataDAO.getImageData(entity);
    if (imageData != null) {
      updateImageData(imageData, data);
    } else {
      insertImageData(entity, data);
    }
    return imageDAO.merge(entity);
  }

  private ImageData updateImageData(ImageData imageData, byte[] data) {
    if (data == null) {
      return null;
    } else {
      imageData.setData(data);
      return imageDataDAO.merge(imageData);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * x1.hiking.service.HikingTracksService#getImageData(x1.hiking.model.Image)
   */
  @Override
  public ImageData getImageData(Image image) {
    return imageDataDAO.getImageData(image);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#deleteImageData(x1.hiking.model.
   * Image)
   */
  @Override
  public void deleteImageData(Image image) {
    imageDataDAO.removeImageData(image);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.HikingTracksService#delete(x1.hiking.model.Image)
   */
  @Override
  public void delete(Image entity) {
    imageDAO.remove(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * x1.hiking.service.HikingTracksService#insert(x1.hiking.model.TrackData)
   */
  @Override
  public void insert(TrackData entity) {
    trackDataDAO.persist(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * x1.hiking.service.HikingTracksService#update(x1.hiking.model.TrackData)
   */
  @Override
  public TrackData update(TrackData entity) {
    return trackDataDAO.merge(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * x1.hiking.service.HikingTracksService#delete(x1.hiking.model.TrackData)
   */
  @Override
  public void delete(TrackData entity) {
    trackDataDAO.remove(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * x1.hiking.service.HikingTracksService#findFirstImage(x1.hiking.model.Track)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Image findFirstImage(Track track) {
    return trackDAO.findFirstImage(track);
  }

  @EJB
  private UserDAO userDAO;

  @EJB
  private TrackDataDAO trackDataDAO;

  @EJB
  private ImageDAO imageDAO;

  @EJB
  private TrackDAO trackDAO;

  @EJB
  private ImageDataDAO imageDataDAO;
}
