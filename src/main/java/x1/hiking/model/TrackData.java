package x1.hiking.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Track data (KML) model class
 *
 * @author joe
 *
 */
@Entity
@Table(name = "track_data", indexes = {
  @Index(name = "idx_track_data_start_lat", columnList = "start_lat"),
  @Index(name = "idx_track_data_start_lng", columnList = "start_lng"),
  @Index(name = "idx_track_data_end_lat", columnList = "end_lat"),
  @Index(name = "idx_track_data_end_lng", columnList = "end_lng"),
  @Index(name = "idx_track_data_track_id", columnList = TrackData.COL_TRACK_ID, unique = false) 
})
@NamedQueries({
    @NamedQuery(name = "TrackData.findTrackDataByUserAndNameAndId", 
        query = "SELECT td FROM TrackData td WHERE td.track.user = :user AND td.track.name = :name AND td.id = :id"),
    @NamedQuery(name = "TrackData.findPublicTrackDataByNameAndId", 
        query = "SELECT td FROM TrackData td WHERE td.track.published = true AND td.track.name = :name AND td.id = :id"),
    @NamedQuery(name = "TrackData.getTrackData",
        query = "SELECT td FROM TrackData td WHERE td.track = :track"),
    @NamedQuery(name = "TrackData.findTrackDataForLocationUpdate", 
        query = "SELECT td FROM TrackData td WHERE td.startPoint.lat IS NULL AND data IS NOT NULL")
})
public class TrackData implements Model {
  private static final long serialVersionUID = 3602772925993086613L;
  public static final String ATTR_TRACK = "track";
  public static final String COL_TRACK_ID = "track_id";
  public static final String ATTR_NAME = "name";
  public static final String ATTR_URL = "url";
  public static final String ATTR_START_POINT = "startPoint";
  public static final String ATTR_END_POINT = "endPoint";
  public static final String ATTR_LOWEST_POINT = "lowestPoint";
  public static final String ATTR_HIGHEST_POINT = "highestPoint";

  /**
   * Default constructor
   */
  public TrackData() {
  }

  /**
   * special constructor for JPA query
   */
  public TrackData(Integer id, Integer version, String name, Track track, Coord startPoint, Coord endPoint,
      Coord lowestPoint, Coord highestPoint, String url) {
    super();
    this.id = id;
    this.name = name;
    this.version = version;
    this.track = track;
    this.startPoint = startPoint;
    this.endPoint = endPoint;
    this.lowestPoint = lowestPoint;
    this.highestPoint = highestPoint;
    this.url = url;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url
   *          the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @return the data
   */
  public byte[] getData() {
    return data;
  }

  /**
   * @param data
   *          the data to set
   */
  public void setData(byte[] data) {
    this.data = data;
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
   * @return the startPoint
   */
  public Coord getStartPoint() {
    return startPoint;
  }

  /**
   * @param startPoint
   *          the startPoint to set
   */
  public void setStartPoint(Coord startPoint) {
    this.startPoint = startPoint;
  }

  /**
   * @return the endPoint
   */
  public Coord getEndPoint() {
    return endPoint;
  }

  /**
   * @param endPoint
   *          the endPoint to set
   */
  public void setEndPoint(Coord endPoint) {
    this.endPoint = endPoint;
  }

  /**
   * @return the lowestPoint
   */
  public Coord getLowestPoint() {
    return lowestPoint;
  }

  /**
   * @param lowestPoint
   *          the lowestPoint to set
   */
  public void setLowestPoint(Coord lowestPoint) {
    this.lowestPoint = lowestPoint;
  }

  /**
   * @return the highestPoint
   */
  public Coord getHighestPoint() {
    return highestPoint;
  }

  /**
   * @param highestPoint
   *          the highestPoint to set
   */
  public void setHighestPoint(Coord highestPoint) {
    this.highestPoint = highestPoint;
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

  public void setLocation(Coord[] coordinates) {
    if (coordinates.length > 0) {
      Coord newStartPoint = coordinates[0];
      Coord newEndPoint = coordinates[coordinates.length - 1];
      Coord newLowestPoint = coordinates[0];
      Coord newHighestPoint = coordinates[0];
      for (Coord c : coordinates) {
        if (c.getElevation() < newLowestPoint.getElevation()) {
          newLowestPoint = c;
        }
        if (c.getElevation() > newHighestPoint.getElevation()) {
          newHighestPoint = c;
        }
      }
      setStartPoint(newStartPoint);
      setEndPoint(newEndPoint);
      setLowestPoint(newLowestPoint);
      setHighestPoint(newHighestPoint);
    } else {
      setStartPoint(null);
      setEndPoint(null);
      setLowestPoint(null);
      setHighestPoint(null);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "<trackdata id=" + getId() + ": " + getName() + ">";
  }

  @Column(name = ATTR_NAME, nullable = false, length = 100)
  @NotNull
  @Size(max = 100)
  private String name;
  @Column(name = ATTR_URL, nullable = true, length = 200)
  @Size(max = 200)
  private String url;
  @Column(name = "track_data", nullable = true)
  @Lob
  private byte[] data;
  @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = COL_TRACK_ID, foreignKey = @ForeignKey(name = "fk_track_track_data"))
  private Track track;
  @Embedded
  @AttributeOverrides({ @AttributeOverride(name = Coord.ATTR_LAT, column = @Column(name = "start_lat")),
      @AttributeOverride(name = Coord.ATTR_LNG, column = @Column(name = "start_lng")),
      @AttributeOverride(name = Coord.ATTR_ELEVATION, column = @Column(name = "start_elev")) })
  private Coord startPoint;
  @Embedded
  @AttributeOverrides({ @AttributeOverride(name = Coord.ATTR_LAT, column = @Column(name = "end_lat")),
      @AttributeOverride(name = Coord.ATTR_LNG, column = @Column(name = "end_lng")),
      @AttributeOverride(name = Coord.ATTR_ELEVATION, column = @Column(name = "end_elev")) })
  private Coord endPoint;
  @Embedded
  @AttributeOverrides({ @AttributeOverride(name = Coord.ATTR_LAT, column = @Column(name = "low_lat")),
      @AttributeOverride(name = Coord.ATTR_LNG, column = @Column(name = "low_lng")),
      @AttributeOverride(name = Coord.ATTR_ELEVATION, column = @Column(name = "low_elev")) })
  private Coord lowestPoint;
  @Embedded
  @AttributeOverrides({ @AttributeOverride(name = Coord.ATTR_LAT, column = @Column(name = "high_lat")),
      @AttributeOverride(name = Coord.ATTR_LNG, column = @Column(name = "high_lng")),
      @AttributeOverride(name = Coord.ATTR_ELEVATION, column = @Column(name = "high_elev")) })
  private Coord highestPoint;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = ATTR_ID)
  private Integer id;
  @Version
  @Column(name = ATTR_VERSION)
  private Integer version;
}
