package x1.hiking.service;

import java.util.List;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheResult;

import x1.hiking.dao.QueryOptions;
import x1.hiking.model.ActivityType;
import x1.hiking.model.Bounds;
import x1.hiking.model.Image;
import x1.hiking.model.ImageData;
import x1.hiking.model.Track;
import x1.hiking.model.TrackData;
import x1.hiking.model.User;
import x1.hiking.model.UserNotFoundException;
import x1.hiking.utils.UserCacheKeyGenerator;

/**
 * HikingTracks Service: internal data access service.
 *
 * @author joe
 */
public interface HikingTracksService {

  /**
   * Insert.
   *
   * @param entity the entity
   */
  void insert(User entity);

  /**
   * Delete.
   *
   * @param entity the entity
   */
  @CacheRemove(afterInvocation = true, cacheName = "user-cache", cacheKeyGenerator = UserCacheKeyGenerator.class)
  void delete(@CacheKey User entity);

  /**
   * Update.
   *
   * @param entity the entity
   * @return the user
   */
  @CacheRemove(afterInvocation = false, cacheName = "user-cache")
  @CacheResult(cacheName = "user-cache", cacheKeyGenerator = UserCacheKeyGenerator.class)
  User update(@CacheKey User entity);

  /**
   * Find user.
   *
   * @param id the id
   * @return the user
   */
  User findUser(Integer id);

  /**
   * Insert.
   *
   * @param entity the entity
   */
  void insert(Track entity);

  /**
   * Delete.
   *
   * @param entity the entity
   */
  void delete(Track entity);

  /**
   * Update.
   *
   * @param entity the entity
   * @return the track
   */
  Track update(Track entity);

  /**
   * Find track.
   *
   * @param id the id
   * @return the track
   */
  Track findTrack(Integer id);

  /**
   * next track.
   *
   * @param track the track
   * @param user the user
   * @return the track
   */
  Track nextTrack(Track track, User user);

  /**
   * Find track.
   *
   * @param track the track
   * @param user the user
   * @return the track
   */
  Track previousTrack(Track track, User user);

  /**
   * Find user.
   *
   * @param email the email
   * @return the user
   * @throws UserNotFoundException 
   */
  @CacheResult(cacheName = "user-cache", cacheKeyGenerator = UserCacheKeyGenerator.class)
  User findUserByEmail(@CacheKey String email) throws UserNotFoundException;

  /**
   * Find user.
   *
   * @param token the token
   * @return the user
   * @throws UserNotFoundException 
   */
  User findUserByToken(@CacheKey String token) throws UserNotFoundException;

  /**
   * Find track.
   *
   * @param name the name
   * @param loadAll load referenced data
   * @return the track
   */
  Track findTrack(String name, boolean loadAll);
  
  /**
   * Find track.
   *
   * @param user the user
   * @param name the name
   * @param loadAll load referenced data
   * @return the track
   */
  Track findTrack(User user, String name, boolean loadAll);

  /**
   * Find tracks.
   *
   * @param user the user
   * @param text the text
   * @param options the options
   * @return the list
   */
  List<Track> findTracks(User user, String text, QueryOptions options);

  /**
   * Find tracks.
   *
   * @param text the text
   * @param options the options
   * @return the list
   */
  List<Track> findTracks(String text, QueryOptions options);

  /**
   * Find tracks within bounds
   *
   * @param text the text
   * @param options the options
   * @return the list
   */
  List<Track> findTracks(String text, Bounds bounds, QueryOptions options);

  /**
   * Count tracks.
   *
   * @param user the user
   * @param text the text
   * @param activity the acitivity
   * @return the count
   */
  Long countTracks(User user, String text, ActivityType activity);

  /**
   * Count tracks.
   *
   * @param text the text
   * @param activity the acitivity
   * @return the count
   */
  Long countTracks(String text, ActivityType activity);

  /**
   * Find image.
   *
   * @param user the user
   * @param name the name
   * @param id the id
   * @return the image
   */
  Image findImage(User user, String name, Integer id);

  /**
   * Find track data.
   *
   * @param user the user
   * @param name the name
   * @param id the id
   * @return the track data
   */
  TrackData findTrackData(User user, String name, Integer id);
  
  /**
   * Insert.
   *
   * @param entity the entity
   */
  void insert(Image entity);
  
  /**
   * Insert.
   *
   * @param entity the entity
   * @param data the data 
   */
  void insert(Image entity, byte[] data);  
  
  /**
   * Update.
   *
   * @param entity the entity
   * @param data the data
   * @return the image
   */
  Image update(Image entity, byte[] data);

  /**
   * Update.
   *
   * @param entity the entity
   * @return the image
   */
  Image update(Image entity);

  /**
   * Delete.
   *
   * @param entity the entity
   */
  void delete(Image entity);
  
  /**
   * Insert.
   *
   * @param entity the entity
   */
  void insert(TrackData entity);
  
  /**
   * Update.
   *
   * @param entity the entity
   * @return the track data
   */
  TrackData update(TrackData entity);
  
  /**
   * Delete.
   *
   * @param entity the entity
   */
  void delete(TrackData entity);

  /**
   * Find first image.
   *
   * @param track the track
   * @return the image
   */
  Image findFirstImage(Track track);
  
  /**
   * get Image data
   * 
   * @param image the image
   * @return the image data
   */
  ImageData getImageData(Image image);
  
  /**
   * delete Image data
   * 
   * @param image the image
   */
  void deleteImageData(Image image);
}