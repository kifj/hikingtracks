package x1.hiking.geocoding;

import x1.hiking.model.Coord;

/**
 * Waypoint extends Coord with the distance (in km) to origin
 */
public class Waypoint extends Coord {
  private static final long serialVersionUID = 1L;
  private double distance;

  public Waypoint() {
    this.distance = 0;
  }

  public Waypoint(Coord coord, double distance) {
    super(coord);
    this.distance = distance;
  }

  public double getDistance() {
    return distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  @Override
  public String toString() {
    return "<coord lat=" + getLat() + ", lng=" + getLng() + ", elevation=" + getElevation() + ", distance="
        + getDistance() + ">";
  }
}
