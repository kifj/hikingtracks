package x1.hiking.model;

import javax.persistence.*;

import org.apache.commons.lang3.StringUtils;

/**
 * Geolocation model class
 * 
 * @author joe
 *
 */
@Entity
@Table(name = "geolocation", indexes = {
    @Index(name = "idx_geolocation_location", columnList = Geolocation.ATTR_LOCATION, unique = false),
    @Index(name = "idx_geolocation_area", columnList = Geolocation.ATTR_AREA, unique = false),
    @Index(name = "idx_geolocation_country", columnList = Geolocation.ATTR_COUNTRY, unique = false),
    @Index(name = "idx_geolocation_track_id", columnList = Geolocation.COL_TRACK_ID, unique = false) 
})
@NamedQuery(name = "Geolocation.findByTrack", query = "SELECT g FROM Geolocation g WHERE g.track = :track")
public class Geolocation implements Model {
  private static final long serialVersionUID = 5320955152495158323L;
  public static final String ATTR_LOCATION = "location";
  public static final String ATTR_COUNTRY = "country";
  public static final String ATTR_AREA = "area";
  public static final String ATTR_TRACK = "track";
  public static final String COL_TRACK_ID = "track_id";
  public static final String ATTR_SOURCE = "source";

  /**
   * Default constructor
   */
  public Geolocation() {
  }

  /**
   * Create from origin
   * 
   * @param coord
   *          the source coordinate
   * @param source
   *          the source
   */
  public Geolocation(Coord coord, GeolocationSource source) {
    this.coord = coord;
    this.source = source;
  }

  /**
   * @return the location
   */
  public String getLocation() {
    return location;
  }

  /**
   * @param location
   *          the location to set
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * @return the area
   */
  public String getArea() {
    return area;
  }

  /**
   * @param area
   *          the area to set
   */
  public void setArea(String area) {
    this.area = area;
  }

  /**
   * @return the country
   */
  public String getCountry() {
    return country;
  }

  /**
   * @param country
   *          the country to set
   */
  public void setCountry(String country) {
    this.country = country;
  }

  /**
   * @return the coord
   */
  public Coord getCoord() {
    return coord;
  }

  /**
   * @param coord
   *          the coord to set
   */
  public void setCoord(Coord coord) {
    this.coord = coord;
  }

  /**
   * @return the track
   */
  public Track getTrack() {
    return track;
  }

  /**
   * @param track
   *          the track to set
   */
  public void setTrack(Track track) {
    this.track = track;
  }

  /**
   * @return the source
   */
  public GeolocationSource getSource() {
    return source;
  }

  /**
   * @param source
   *          the source
   */
  public void setSource(GeolocationSource source) {
    this.source = source;
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.model.Model#getId()
   */
  @Override
  public Integer getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.model.Model#setId(java.lang.Integer)
   */
  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.model.Model#getVersion()
   */
  @Override
  public Integer getVersion() {
    return version;
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.model.Model#setVersion(java.lang.Integer)
   */
  @Override
  public void setVersion(Integer version) {
    this.version = version;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "<geolocation id=" + getId() + " " + getSource() + ": " + getLocation() + ">";
  }

  /**
   * Check if object has meaningful values
   */
  public boolean hasValues() {
    return area != null || location != null;
  }

  /**
   * Check if two locations are the same
   */
  public boolean sameLocation(Geolocation other) {
    return StringUtils.equals(location, other.getLocation())
        && ((country == null || other.getCountry() == null) || StringUtils.equals(country, other.getCountry()));
  }

  @Column(name = ATTR_LOCATION, nullable = true, length = 100)
  private String location;
  @Column(name = ATTR_AREA, nullable = true, length = 100)
  private String area;
  @Column(name = ATTR_COUNTRY, nullable = true, length = 100)
  private String country;
  @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = COL_TRACK_ID, foreignKey = @ForeignKey(name = "fk_track_geolocation"))
  private Track track;
  @Embedded
  private Coord coord;
  @Column(name = ATTR_SOURCE, nullable = false)
  private GeolocationSource source;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = ATTR_ID)
  private Integer id;
  @Version
  @Column(name = ATTR_VERSION)
  private Integer version;

}
