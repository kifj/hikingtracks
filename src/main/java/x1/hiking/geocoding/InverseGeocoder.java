package x1.hiking.geocoding;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.model.Coord;
import x1.hiking.model.Geolocation;
import x1.hiking.model.GeolocationSource;
import x1.hiking.utils.ConfigurationValue;

/**
 * Facility to retrieve geonames for coordinates
 */
public class InverseGeocoder {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private static final String PARAM_RESULT_TYPE = "result_type";
  private static final String VALUE_RESULT_TYPE = "locality";
  private static final String PARAM_KEY = "key";
  private static final String PARAM_LATLNG = "latlng";
  private static final String PARAM_LANGUAGE = "language";
  private static final String VALUE_LANGUAGE = "en";

  @Inject
  @ConfigurationValue(key = "google.apikey")
  private String key;

  @Inject
  @ConfigurationValue(key = "google.geolocation")
  private String baseUrl;

  public Coord[] getWaypoints(Coord[] coords, double minDistance) {
    List<Coord> result = new ArrayList<>();
    Coord last = null;
    for (Coord c : coords) {
      if (last == null) {
        result.add(c);
        last = c;
      } else {
        DistanceCalculator calculator = new DistanceCalculator(last, c);
        double distance = calculator.distance();
        if (distance > minDistance) {
          result.add(c);
          last = c;
        }
      }
    }
    return result.toArray(new Coord[result.size()]);
  }

  public Geolocation[] getLocationsForWaypoints(Coord[] coords, double minDistance) {
    List<Geolocation> geolocations = new ArrayList<>();

    Coord[] waypoints = getWaypoints(coords, minDistance);
    for (Coord waypoint : waypoints) {
      URI uri = UriBuilder.fromUri(baseUrl).queryParam(PARAM_RESULT_TYPE, VALUE_RESULT_TYPE)
          .queryParam(PARAM_LANGUAGE, VALUE_LANGUAGE).queryParam(PARAM_KEY, key)
          .queryParam(PARAM_LATLNG, waypoint.getLat() + "," + waypoint.getLng()).build();
      Response resp = ClientBuilder.newClient().target(uri).request(MediaType.APPLICATION_JSON).get();
      String body = resp.readEntity(String.class);
      JsonReader rdr = Json.createReader(new StringReader(body));
      JsonObject obj = rdr.readObject();

      obj.getJsonArray("results").getValuesAs(JsonObject.class).forEach(
          result -> extractGeolocation(geolocations, new Geolocation(waypoint, GeolocationSource.TRACKDATA), result));
    }
    return geolocations.toArray(new Geolocation[geolocations.size()]);
  }

  private void extractGeolocation(List<Geolocation> result, Geolocation geolocation, JsonObject jsonObject) {
    jsonObject.getJsonArray("address_components").getValuesAs(JsonObject.class)
        .forEach(address -> extractGeolocation(geolocation, address));
    log.info("Found {}", geolocation);
    if (geolocation.hasValues()) {
      result.add(geolocation);
    }
  }

  private void extractGeolocation(Geolocation geolocation, JsonObject address) {
    String longName = address.getString("long_name");
    String shortName = address.getString("short_name");
    address.getJsonArray("types").getValuesAs(JsonString.class).forEach(type -> {
      switch (type.getString()) {
      case "administrative_area_level_1":
        // level 1 overwrites level 2 if it exists
        geolocation.setArea(shortName);
        break;
      case "administrative_area_level_2":
        geolocation.setArea(longName);
        break;
      case "country":
        geolocation.setCountry(longName);
        break;
      case "locality":
        geolocation.setLocation(longName);
        break;
      default:
        break;
      }
    });
  }
}
