package x1.hiking.geocoding;

import x1.hiking.model.Coord;

/**
 * Calculate distance between 2 points
 */
public class DistanceCalculator {
  private Coord start;
  private Coord end;

  public DistanceCalculator(Coord start, Coord end) {
    this.start = start;
    this.end = end;
  }

  public double distance() {
    int r = 6371000; // metres
    double toRadians = Math.PI / 180.0d;
    double f1 = start.getLat() * toRadians;
    double f2 = end.getLat() * toRadians;
    double dlat = (end.getLat() - start.getLat()) * toRadians;
    double dlong = (end.getLng() - start.getLng()) * toRadians;

    double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
        + Math.cos(f1) * Math.cos(f2) * Math.sin(dlong / 2) * Math.sin(dlong / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return r * c;
  }
}
