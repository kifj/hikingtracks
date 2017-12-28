package x1.hiking.geocoding;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.control.TrackService;
import x1.hiking.model.Coord;
import x1.hiking.model.Geolocation;
import x1.hiking.model.Track;
import x1.hiking.model.TrackData;
import x1.hiking.utils.ConfigurationValue;

/**
 * Job for updating geolocation tags
 * 
 * @author joe
 * 
 */
@Stateless
public class GeolocationTagUpdaterImpl implements GeolocationTagUpdater {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Inject
  @ConfigurationValue(key = "geolocation.min_distance", defaultValue = "10000")
  private String minDistance;

  @Inject
  @ConfigurationValue(key = "geolocation.max_result", defaultValue = "5")
  private String maxResult;

  @PersistenceContext
  private EntityManager em;

  @EJB
  private TrackService trackService;

  @Inject
  private InverseGeocoder geocoder;
  
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
    findTracksForGeolocationUpdate(getMaxResult()).forEach(this::updateGeolocationsInternal);
    log.trace("Updating Trackdata locations...");
    findTrackDataForUpdate(getMaxResult()).forEach(this::updateTrackdataLocationInternal);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.geocoding.GeolocationTagUpdater#updateGeolocations(x1.hiking.
   * model.Track)
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public void updateGeolocations(Integer id) {
    updateGeolocationsInternal(em.find(Track.class, id));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * x1.hiking.geocoding.GeolocationTagUpdater#findGeolocation(x1.hiking.model.
   * Track)
   */
  @Override
  public List<Geolocation> findGeolocation(final Track track) {
    TypedQuery<Geolocation> q = em.createNamedQuery("Geolocation.findByTrack", Geolocation.class);
    q.setParameter("track", track);
    return q.getResultList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * x1.hiking.geocoding.GeolocationTagUpdater#findTracksForGeolocationUpdate(int)
   */
  @Override
  public List<Track> findTracksForGeolocationUpdate(int maxResults) {
    TypedQuery<Track> q = em.createNamedQuery("Track.findTracksForGeolocationUpdate", Track.class);
    q.setMaxResults(maxResults);
    return q.getResultList();
  }

  private void updateGeolocationsInternal(Track track) {
    log.trace("Updating geolocations for track {}", track);
    findGeolocation(track).forEach(em::remove);
    List<Geolocation> geolocations = createGeolocations(track, getMinDistance());
    geolocations.forEach(em::persist);
    track.setGeolocationAvailable(!geolocations.isEmpty());
    log.info("Found {} geolocations for track {}", geolocations.size(), track);
    trackService.update(track);
  }

  private List<Geolocation> createGeolocations(Track track, double minDistance) {
    List<Geolocation> result = new ArrayList<>();
    if (track.getLatitude() != null && track.getLongitude() != null) {
      List<Geolocation> hits = createGeolocation(track, minDistance);
      hits.forEach(geolocation -> addToGeolocations(result, geolocation));
    }
    trackService.loadTrackData(track).forEach(td -> {
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
      trackService.update(trackdata);
      log.info("Updated location for trackdata {}", trackdata);
    }
  }

  private List<TrackData> findTrackDataForUpdate(int maxResults) {
    TypedQuery<TrackData> q = em.createNamedQuery("TrackData.findTrackDataForLocationUpdate", TrackData.class);
    q.setMaxResults(maxResults);
    return q.getResultList();
  }
  
  private int getMaxResult() {
    return Integer.parseInt(maxResult);
  }

  private double getMinDistance() {
    return Double.parseDouble(minDistance);
  }

}
