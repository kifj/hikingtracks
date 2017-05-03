package x1.hiking.model;

import java.io.Serializable;
import java.text.DecimalFormat;

import javax.persistence.*;

/**
 * Model for coordinates (lat, long, elevation) 
 * 
 * @author joe
 *
 */
@Embeddable
public class Coord implements Serializable {
  private static final long serialVersionUID = -7734756125595121288L;
  public static final String COORDINATES_PRECISION = "#,###,###,##0";
  private static final int MINUTES_PER_HOUR = 60;
  private static final int SECONDS_PER_MINUTE = 60;

  public Coord(Coord coord) {
    this.lat = coord.getLat();
    this.lng = coord.getLng();
    this.elevation = coord.getElevation();
  }

  public Coord(float lat, float lng, float elevation) {
    this.lat = lat;
    this.lng = lng;
    this.elevation = elevation;
  }
  
  public Coord(Double lat, Double lng) {
    this.lat = lat.floatValue();
    this.lng = lng.floatValue();
  }

  public Coord() {
  }

  public float getLat() {
    return lat;
  }

  public void setLat(float lat) {
    this.lat = lat;
  }

  public float getLng() {
    return lng;
  }

  public void setLng(float lng) {
    this.lng = lng;
  }

  public float getElevation() {
    return elevation;
  }

  public void setElevation(float elevation) {
    this.elevation = elevation;
  }

  @Override
  public String toString() {
    return "<coord lat=" + lat + ", lng=" + lng + ", elevation=" + elevation + ">";
  }

  /**
   * Get a string representation of the latitude and longitude in the simplified
   * form 52°39'27"N 143'4"E
   * 
   * @return
   */
  public String simpleLocationString() {
    DecimalFormat decimalFormat = new DecimalFormat(COORDINATES_PRECISION);

    StringBuilder latStr = new StringBuilder();
    int latDeg = (int) Math.floor(Math.abs(getLat()));
    int latMin = (int) Math.floor((Math.abs(getLat()) - latDeg) * MINUTES_PER_HOUR);
    double latSec = (((Math.abs(getLat()) - latDeg) * MINUTES_PER_HOUR) - latMin) * SECONDS_PER_MINUTE;
    String latSec2 = decimalFormat.format(latSec);
    latStr.append(latDeg).append("°").append(latMin).append("'").append(latSec2).append("\"");
    if (getLat() < 0) {
      latStr.append("S");
    } else {
      latStr.append("N");
    }

    StringBuilder lngStr = new StringBuilder();
    int lngDeg = (int) Math.floor(Math.abs(getLng()));
    int lngMin = (int) Math.floor((Math.abs(getLng()) - lngDeg) * MINUTES_PER_HOUR);
    double lngSec = (((Math.abs(getLng()) - lngDeg) * MINUTES_PER_HOUR) - lngMin) * SECONDS_PER_MINUTE;
    String lngSec2 = decimalFormat.format(lngSec);
    lngStr.append(lngDeg).append("°").append(lngMin).append("'").append(lngSec2).append("\"");
    if (getLng() < 0) {
      lngStr.append("W");
    } else {
      lngStr.append("E");
    }

    return latStr.toString() + " - " + lngStr.toString();
  }
  
  @Column(name = "lat")
  private float lat;
  @Column(name = "lng")
  private float lng;
  @Column(name = "elev")
  private float elevation;
}
