package x1.hiking.geocoding;

import java.util.List;

import x1.hiking.model.Geolocation;
import x1.hiking.model.Track;

/** 
 * Job for updating geolocation tags
 * 
 * @author joe
 *
 */
public interface GeolocationTagUpdater {

  /** Update geolocations for track
   *  
   * @param id the track id
   */
  void updateGeolocations(Integer id);

  /**
   *  Update all geolocations which need to be updated
   */
  void updateGeolocations();
  
  /** Find geolocations for track
   * 
   * @param track the track
   * @return the geolocations
   */
  List<Geolocation> findGeolocation(final Track track);
  
  /** Find tracks for location update
   * 
   * @param maxResults maximum of result set
   * @return tracks with no location data
   */
  List<Track> findTracksForGeolocationUpdate(int maxResults);
}