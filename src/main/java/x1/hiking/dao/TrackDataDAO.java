package x1.hiking.dao;

import java.util.List;

import x1.hiking.model.Track;
import x1.hiking.model.TrackData;
import x1.hiking.model.User;

/**
 * The TrackData DAO.
 * 
 * @author joe
 *
 */
public interface TrackDataDAO extends JpaDAO<TrackData> {

  /**
   * Find track data.
   *
   * @param user
   *          the user
   * @param name
   *          the name of the track
   * @param id
   *          the id of the track data
   * @return the track data
   */
  TrackData findTrackData(final User user, final String name, final Integer id);

  /**
   * Find track data for track.
   * 
   * @param track
   *          the track
   * @return the list of track data
   */
  List<TrackData> find(Track track);
  
  List<TrackData> withoutData(Track track);
  
  /**
   * Find track data which needs to be updated
   * @param maxResults maximum number of entries
   * @return the list of track data
   */
  List<TrackData> findTrackDataForUpdate(int maxResults);
}