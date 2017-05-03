package x1.hiking.model;

/**
 * geographic boundaries
 */
public class Bounds {
  private float north;
  private float south;
  private float east;
  private float west;

  public float getNorth() {
    return north;
  }

  public void setNorth(float north) {
    this.north = north;
  }

  public float getSouth() {
    return south;
  }

  public void setSouth(float south) {
    this.south = south;
  }

  public float getEast() {
    return east;
  }

  public void setEast(float east) {
    this.east = east;
  }

  public float getWest() {
    return west;
  }

  public void setWest(float west) {
    this.west = west;
  }

  @Override
  public String toString() {
    return "[" + north + ", " + east + ", " + south + ", " + west + "]";
  }
}
