package x1.hiking.model;

import javax.persistence.*;

/**
 * Track data (KML) model class
 * 
 * @author joe
 *
 */
@Entity
@Table(name = "track_data", indexes = {
  @Index(name = "idx_start_lat", columnList = "start_lat"),
  @Index(name = "idx_start_lng", columnList = "start_lng"),
  @Index(name = "idx_end_lat", columnList = "end_lat"),
  @Index(name = "idx_end_lng", columnList = "end_lng")
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
      Coord startPoint = coordinates[0];
      Coord endPoint = coordinates[coordinates.length - 1];
      Coord lowestPoint = coordinates[0];
      Coord highestPoint = coordinates[0];
      for (Coord c : coordinates) {
        if (c.getElevation() < lowestPoint.getElevation()) {
          lowestPoint = c;
        }
        if (c.getElevation() > highestPoint.getElevation()) {
          highestPoint = c;
        }
      }
      setStartPoint(startPoint);
      setEndPoint(endPoint);
      setLowestPoint(lowestPoint);
      setHighestPoint(highestPoint);
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

  @Column(name = "name", nullable = false, length = 100)
  private String name;
  @Column(name = "url", nullable = true, length = 200)
  private String url;
  @Column(name = "track_data", nullable = true)
  @Lob
  private byte[] data;
  @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, optional = false, fetch = FetchType.LAZY)
  @PrimaryKeyJoinColumn(name = "track", foreignKey = @ForeignKey(name = "fk_track_track_data"))
  private Track track;
  @Embedded
  @AttributeOverrides({ @AttributeOverride(name = "lat", column = @Column(name = "start_lat")),
      @AttributeOverride(name = "lng", column = @Column(name = "start_lng")),
      @AttributeOverride(name = "elevation", column = @Column(name = "start_elev")) })
  private Coord startPoint;
  @Embedded
  @AttributeOverrides({ @AttributeOverride(name = "lat", column = @Column(name = "end_lat")),
      @AttributeOverride(name = "lng", column = @Column(name = "end_lng")),
      @AttributeOverride(name = "elevation", column = @Column(name = "end_elev")) })
  private Coord endPoint;
  @Embedded
  @AttributeOverrides({ @AttributeOverride(name = "lat", column = @Column(name = "low_lat")),
      @AttributeOverride(name = "lng", column = @Column(name = "low_lng")),
      @AttributeOverride(name = "elevation", column = @Column(name = "low_elev")) })
  private Coord lowestPoint;
  @Embedded
  @AttributeOverrides({ @AttributeOverride(name = "lat", column = @Column(name = "high_lat")),
      @AttributeOverride(name = "lng", column = @Column(name = "high_lng")),
      @AttributeOverride(name = "elevation", column = @Column(name = "high_elev")) })
  private Coord highestPoint;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  private Integer id;
  @Version
  @Column(name = "version")
  private Integer version;
}
