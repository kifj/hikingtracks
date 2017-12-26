package x1.hiking.control;

import java.util.List;

import x1.hiking.model.ActivityType;
import x1.hiking.model.Bounds;
import x1.hiking.model.Image;
import x1.hiking.model.Track;
import x1.hiking.model.TrackData;
import x1.hiking.model.User;

/**
 * management of tracks and trackdata
 */
public interface TrackService {

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
   * Find track.
   *
   * @param name the name
   * @return the track
   */  
  Track findTrack(String name);

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
   * Find track.
   *
   * @param user the user
   * @param name the name
   * @return the track
   */
  Track findTrack(User user, String name);
  
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
   * Load track data for track.
   * 
   * @param track
   *          the track
   * @return the list of track data
   */
  List<TrackData> loadTrackData(Track track);
  
  /**
   * Load images for track.
   * 
   * @param track
   *          the track
   * @return the list of images
   */
  List<Image> loadImages(Track track);

}