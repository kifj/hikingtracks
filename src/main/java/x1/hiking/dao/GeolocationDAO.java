package x1.hiking.dao;

import java.util.List;

import x1.hiking.model.Geolocation;
import x1.hiking.model.Track;

/**
 * The Geolocation DAO.
 * 
 * @author joe
 *
 */
public interface GeolocationDAO extends JpaDAO<Geolocation> {


  /**
   * Find geolocation for track.
   * 
   * @param track
   *          the track
   * @return the list of geolocation
   */
  List<Geolocation> findGeolocation(Track track);
}