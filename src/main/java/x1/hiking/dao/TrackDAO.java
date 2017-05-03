package x1.hiking.dao;

import java.util.List;

import x1.hiking.model.ActivityType;
import x1.hiking.model.Bounds;
import x1.hiking.model.Image;
import x1.hiking.model.Track;
import x1.hiking.model.User;

/** The Track DAO.
 * 
 * @author joe
 *
 */
public interface TrackDAO extends JpaDAO<Track> {

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
   * Find tracks within bounds.
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
   * @param activity the activity
   * @return the long
   */
  Long countTracks(User user, String text, ActivityType activity);

  /**
   * Count tracks.
   *
   * @param text the text
   * @param activity the activity
   * @return the long
   */
  Long countTracks(String text, ActivityType activity);

  /**
   * Find track.
   *
   * @param user the user
   * @param name the name
   * @return the track
   */
  Track findTrack(User user, String name);

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
  Track next(Track track, User user);
  
  /**
   * previous track.
   *
   * @param track the track
   * @param user the user
   * @return the track
   */
  Track previous(Track track, User user);
  
  /**
   * Find first image.
   *
   * @param track the track
   * @return the image
   */
  Image findFirstImage(Track track);
  
  /**
   * Find tracks for geolocation update
   *
   * @param maxResults limit how many tracks
   * @return the list
   */
  List<Track> findTracksForGeolocationUpdate(int maxResults);

}