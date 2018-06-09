package x1.hiking.model;

import java.util.List;

import javax.persistence.*;

/**
 * Image model class
 * 
 * @author joe
 *
 */
@Entity
@Table(name = "image", indexes = { 
    @Index(name = "idx_image_nr", columnList = Image.COL_NR, unique = false), 
    @Index(name = "idx_image_track_id", columnList = Image.COL_TRACK_ID, unique = false) 
})
@NamedQuery(name = "Image.findImageByUserAndNameAndId", 
    query = "SELECT i FROM Image i WHERE i.track.user = :user AND i.track.name = :name AND i.id = :id ORDER by i.number")
@NamedQuery(name = "Image.findPublicImageByNameAndId", 
    query = "SELECT i FROM Image i WHERE i.track.published = true AND i.track.name = :name AND i.id = :id ORDER by i.number")
@NamedQuery(name = "Image.findMissingThumbnails", 
    query = "SELECT i FROM Image i WHERE i.thumbnails IS EMPTY")
@NamedQuery(name = "Image.getImages", 
    query = "SELECT i FROM Image i WHERE i.track = :track ORDER BY i.number")
@Cacheable
public class Image implements Model {
  private static final long serialVersionUID = 1260256371051142805L;
  public static final String ATTR_NAME = "name";
  public static final String ATTR_URL = "url";
  public static final String ATTR_TRACK = "track";
  public static final String COL_TRACK_ID = "track_id";
  public static final String ATTR_LOCATION = "location";
  public static final String ATTR_LATITUDE = "latitude";
  public static final String ATTR_NUMBER = "number";
  public static final String COL_LAT = "lat";
  public static final String COL_LON = "lon";
  public static final String COL_NR = "nr";

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
   * @return the latitude
   */
  public Double getLatitude() {
    return latitude;
  }

  /**
   * @param latitude
   *          the latitude to set
   */
  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  /**
   * @return the longitude
   */
  public Double getLongitude() {
    return longitude;
  }

  /**
   * @param longitude
   *          the longitude to set
   */
  public void setLongitude(Double longitude) {
    this.longitude = longitude;
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
   * @return the number
   */
  public Integer getNumber() {
    return number;
  }

  /**
   * @param number
   *          the number to set
   */
  public void setNumber(Integer number) {
    this.number = number;
  }

  public List<ImageData> getImageData() {
    return imageData;
  }

  public void setImageData(List<ImageData> imageData) {
    this.imageData = imageData;
  }

  /**
   * @return the thumbnails
   */
  public List<Thumbnail> getThumbnails() {
    return thumbnails;
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
    return "<image id=" + getId() + ": " + getName() + ">";
  }

  @Column(name = ATTR_NAME, nullable = false, length = 100)
  private String name;
  @Column(name = COL_LAT, nullable = true, precision = 9, scale = 6)
  private Double latitude;
  @Column(name = COL_LON, nullable = true, precision = 9, scale = 6)
  private Double longitude;
  @Column(name = ATTR_URL, nullable = true, length = 200)
  private String url;
  @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = COL_TRACK_ID, foreignKey = @ForeignKey(name = "fk_track_image"))
  private Track track;
  @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = Thumbnail.ATTR_IMAGE, fetch = FetchType.EAGER)
  private List<Thumbnail> thumbnails;
  @Column(name = COL_NR, nullable = false)
  private Integer number;
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = ImageData.ATTR_IMAGE, fetch = FetchType.LAZY)
  private List<ImageData> imageData;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = ATTR_ID)
  private Integer id;
  @Version
  @Column(name = ATTR_VERSION)
  private Integer version;
}
