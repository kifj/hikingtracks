package x1.hiking.geocoding;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.dao.GeolocationDAO;
import x1.hiking.dao.TrackDAO;
import x1.hiking.dao.TrackDataDAO;
import x1.hiking.model.Coord;
import x1.hiking.model.Geolocation;
import x1.hiking.model.Track;
import x1.hiking.model.TrackData;

/**
 * Job for updating geolocation tags
 * 
 * @author joe
 * 
 */
@Stateless
public class GeolocationTagUpdaterImpl implements GeolocationTagUpdater {
  private final Logger log = LoggerFactory.getLogger(getClass());
  // add config value
  private static final double MIN_DISTANCE = 10000;
  private static final int MAX_RESULT = 5;

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.geocoding.GeolocationTagUpdater#updateGeolocations()
   */
  @Schedule(hour = "*", minute = "*/5", second = "0", persistent = false)
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public void updateGeolocations() {
    log.trace("Updating Geolocations...");
    trackDAO.findTracksForGeolocationUpdate(MAX_RESULT).forEach(this::updateGeolocationsInternal);
    log.trace("Updating Trackdata locations...");
    trackDataDAO.findTrackDataForUpdate(MAX_RESULT).forEach(this::updateTrackdataLocationInternal);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * x1.hiking.geocoding.GeolocationTagUpdater#updateGeolocations(x1.hiking.
   * model.Track)
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public void updateGeolocations(Integer id) {
    updateGeolocationsInternal(trackDAO.find(id));
  }

  private void updateGeolocationsInternal(Track track) {
    log.trace("Updating geolocations for track {}", track);
    geolocationDAO.findGeolocation(track).forEach(geolocationDAO::remove);
    List<Geolocation> geolocations = createGeolocations(track, MIN_DISTANCE);
    geolocations.forEach(geolocationDAO::persist);
    track.setGeolocationAvailable(!geolocations.isEmpty());
    log.info("Found {} geolocations for track {}", geolocations.size(), track);
    trackDAO.merge(track);
  }

  private List<Geolocation> createGeolocations(Track track, double minDistance) {
    List<Geolocation> result = new ArrayList<>();
    if (track.getLatitude() != null && track.getLongitude() != null) {
      List<Geolocation> hits = createGeolocation(track, minDistance);
      hits.forEach(geolocation -> addToGeolocations(result, geolocation));
    }
    trackDataDAO.find(track).forEach(td -> {
      List<Geolocation> hits = createGeolocations(td, minDistance);
      hits.forEach(geolocation -> addToGeolocations(result, geolocation));
    });
    return result;
  }

  private void addToGeolocations(List<Geolocation> result, Geolocation geolocation) {
    boolean found = false;
    for (Geolocation other : result) {
      found = geolocation.sameLocation(other);
      if (found) {
        break;
      }
    }
    if (!found) {
      result.add(geolocation);
    }
  }

  private List<Geolocation> createGeolocations(TrackData trackData, double minDistance) {
    List<Geolocation> result = new ArrayList<>();
    Coord[] coords = KmlSampler.parse(trackData).getSamples();
    if (coords.length > 0) {
      Geolocation[] waypoints = geocoder.getLocationsForWaypoints(coords, minDistance);
      for (Geolocation waypoint : waypoints) {
        waypoint.setTrack(trackData.getTrack());
        result.add(waypoint);
      }
    }
    return result;
  }

  private List<Geolocation> createGeolocation(Track track, double minDistance) {
    List<Geolocation> result = new ArrayList<>();
    Coord[] coords = new Coord[] { new Coord(track.getLatitude(), track.getLongitude()) };
    Geolocation[] waypoints = geocoder.getLocationsForWaypoints(coords, minDistance);
    for (Geolocation waypoint : waypoints) {
      waypoint.setTrack(track);
      result.add(waypoint);
    }
    return result;
  }
  
  private void updateTrackdataLocationInternal(TrackData trackdata) {
    log.trace("Updating location for trackdata {}", trackdata);
    
    KmlSampler.Result result = KmlSampler.parse(trackdata);
    Coord[] coordinates = result.getSamples();
    if (coordinates.length > 0) {
      trackdata.setLocation(coordinates);
      trackDataDAO.merge(trackdata);
      log.info("Updated location for trackdata {}", trackdata);
    }
  }

  @EJB
  private GeolocationDAO geolocationDAO;

  @EJB
  private TrackDAO trackDAO;

  @EJB
  private TrackDataDAO trackDataDAO;

  @Inject
  private InverseGeocoder geocoder;
}
