package x1.hiking.geocoding;

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
}