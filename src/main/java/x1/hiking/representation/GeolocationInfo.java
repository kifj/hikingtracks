package x1.hiking.representation;

import x1.hiking.model.Coord;
import x1.hiking.model.Geolocation;

/**
 * Geolocation holder object
 */
public class GeolocationInfo {
  private String locality;
  private String country;
  private String area;
  private Coord coord;

  public GeolocationInfo() {
  }

  public GeolocationInfo(Geolocation geolocation) {
    this.coord = geolocation.getCoord();
    this.locality = geolocation.getLocation();
    this.area = geolocation.getArea();
    this.country = geolocation.getCountry();
  }

  public String getLocality() {
    return locality;
  }

  public void setLocality(String locality) {
    this.locality = locality;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getArea() {
    return area;
  }

  public void setArea(String area) {
    this.area = area;
  }

  public Coord getCoord() {
    return coord;
  }

  public void setLatLng(Coord coord) {
    this.coord = coord;
  }

  @Override
  public String toString() {
    return "[" + coord + " -> " + locality + ", " + area + ", " + country + "]";
  }

}
