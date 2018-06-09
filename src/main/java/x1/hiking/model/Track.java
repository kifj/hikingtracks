package x1.hiking.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

/** Track model class
 * 
 * @author joe
 *
 */
@Entity
@Table(name = "track", 
  uniqueConstraints = @UniqueConstraint(name = "idx_track_name_unq", columnNames = { Track.ATTR_NAME, Track.COL_USER_ID }),
  indexes = {
    @Index(name = "idx_track_name", columnList = Track.ATTR_NAME, unique = false),
    @Index(name = "idx_track_published", columnList = Track.ATTR_PUBLISHED, unique = false),
    @Index(name = "idx_track_location", columnList = Track.ATTR_LOCATION, unique = false),
    @Index(name = "idx_track_user_id", columnList = Track.COL_USER_ID, unique = false)
  })
@NamedQuery(name = "Track.findTrackByUserAndName", 
  query = "SELECT t FROM Track t WHERE t.user = :user AND t.name = :name")
@NamedQuery(name = "Track.findPublicTrackByName", 
  query = "SELECT t FROM Track t WHERE t.published = true AND t.name = :name")
@NamedQuery(name = "Track.nextTrack", 
  query = "SELECT t FROM Track t WHERE t.user = :user AND t.id < :id ORDER BY t.id DESC")
@NamedQuery(name = "Track.previousTrack", 
  query = "SELECT t FROM Track t WHERE t.user = :user AND t.id > :id ORDER BY t.id ASC")
@NamedQuery(name = "Track.nextPublicTrack", 
  query = "SELECT t FROM Track t WHERE t.published = true AND t.id < :id ORDER BY t.id DESC")
@NamedQuery(name = "Track.previousPublicTrack", 
  query = "SELECT t FROM Track t WHERE t.published = true AND t.id > :id ORDER BY t.id ASC")
@NamedQuery(name = "Track.findTracksForGeolocationUpdate", 
  query = "SELECT t FROM Track t WHERE t.geolocationAvailable IS NULL")
@NamedQuery(name = "Track.countTracks",
  query = "SELECT COUNT(t.id) FROM Track t")
public class Track implements Model {
  private static final long serialVersionUID = -8607582176696499706L;
  public static final String ATTR_GEOLOCATION = "geolocation";
  public static final String ATTR_TRACK_DATA = "trackData";
  public static final String ATTR_USER = "user";
  public static final String ATTR_ACTIVITY = "activity";
  public static final String ATTR_NAME = "name";
  public static final String ATTR_PUBLISHED = "published";
  public static final String ATTR_LOCATION = "location";
  public static final String ATTR_LONGITUDE = "longitude";
  public static final String ATTR_LATITUDE = "latitude";
  public static final String ATTR_DESCRIPTION = "description";
  public static final String COL_USER_ID = "user_id";
  public static final String COL_LAT = "lat";
  public static final String COL_LON = "lon";
  public static final String COL_LAST_CHANGE = "last_change";
  public static final String COL_TRACK_DATE = "track_date";
  public static final String COL_PUBLISH_DATE = "publish_date";
  
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
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription(String description) {
    this.description = description;
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
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
   * @param date
   *          the date to set
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * @return the user
   */
  public User getUser() {
    return user;
  }

  /**
   * @param user
   *          the user to set
   */
  public void setUser(User user) {
    this.user = user;
  }

  /**
   * @return the images
   */
  public List<Image> getImages() {
    if (this.images == null) {
      this.images = new ArrayList<>();
    }
    return images;
  }

  /**
   * @param images
   *          the images to set
   */
  public void setImages(List<Image> images) {
    this.images = images;
  }

  /**
   * @param trackData
   *          the trackData to set
   */
  public void setTrackData(List<TrackData> trackData) {
    this.trackData = trackData;
  }

  /**
   * 
   * @param element
   */
  public void addTrackData(TrackData element) {
    if (this.trackData == null) {
      this.trackData = new ArrayList<>();
    }
    this.trackData.add(element);
  }

  /**
   * 
   * @param element
   * @return
   */
  public boolean removeTrackData(TrackData element) {
    if (this.trackData == null) {
      return false;
    }
    return this.trackData.remove(element);
  }

  /**
   * 
   * @param element
   */
  public void addImage(Image element) {
    if (this.images == null) {
      this.images = new ArrayList<>();
    }
    this.images.add(element);
  }

  /**
   * 
   * @param element
   * @return
   */
  public boolean removeImage(Image element) {
    if (this.images == null) {
      return false;
    }
    return this.images.remove(element);
  }

  /**
   * @return the trackData
   */
  public List<TrackData> getTrackData() {
    if (this.trackData == null) {
      this.trackData = new ArrayList<>();
    }
    return trackData;
  }

  /**
   * 
   * @return
   */
  public boolean isPublished() {
    return published;
  }

  /**
   * 
   * @param published
   */
  public void setPublished(boolean published) {
    this.published = published;    
  }
  
  /**
   * @return 
   */
  public Date getPublishDate() {
      return publishDate;
  }

  /**
   * @param publishDate 
   */
  public void setPublishDate(Date publishDate) {
      this.publishDate = publishDate;
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
   * @return the activity
   */
  public ActivityType getActivity() {
    return activity;
  }

  /**
   * @param activity 
   *          the activity to set
   */
  public void setActivity(ActivityType activity) {
    this.activity = activity;
  }
  
  /**
   * @param geolocationAvailable geolocation data has been searched
   */
  public void setGeolocationAvailable(Boolean geolocationAvailable) {
    this.geolocationAvailable = geolocationAvailable;
  }
  
  /**
   * @return is geolocation data available, NULL means not yet searched
   */
  public Boolean isGeolocationAvailable() {
    return geolocationAvailable;
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.model.Model#getId()
   */
  @Override
  public Integer getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.model.Model#setId(java.lang.Integer)
   */
  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.model.Model#getVersion()
   */
  @Override
  public Integer getVersion() {
    return version;
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.model.Model#setVersion(java.lang.Integer)
   */
  @Override
  public void setVersion(Integer version) {
    this.version = version;
  }

  /**
   * Get the last change date
   * 
   * @return the lastChange
   */
  public Date getLastChange() {
    return lastChange;
  }

  /**
   * Sets the last change date
   * 
   * @param lastChange the lastChange to set
   */
  public void setLastChange(Date lastChange) {
    this.lastChange = lastChange;
  }

  /**
   * set audit fields at insert
   */
  @PrePersist
  public void onCreate() {
      this.setLastChange(new Date());
  }

  /**
   * set audit fields at update
   */
  @PreUpdate
  public void onUpdate() {
      this.setLastChange(new Date());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "<track id=" + getId() + ": " + getName() + ">";
  }

  @Column(name = ATTR_NAME, nullable = false, length = 100)
  private String name;
  @Column(name = ATTR_DESCRIPTION, nullable = true)
  @Lob
  private String description;
  @Column(name = ATTR_LOCATION, nullable = true, length = 100)
  private String location;
  @Column(name = COL_TRACK_DATE, nullable = true)
  @Temporal(TemporalType.DATE)
  private Date date;
  @Column(name = "publish_date", nullable = true)
  @Temporal(TemporalType.DATE)
  private Date publishDate;
  @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = COL_USER_ID, foreignKey = @ForeignKey(name = "fk_user_track"))
  private User user;
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = Image.ATTR_TRACK)
  private List<Image> images;
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = TrackData.ATTR_TRACK)
  private List<TrackData> trackData;
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = Geolocation.ATTR_TRACK)
  private List<Geolocation> geolocation;
  @Column(name = ATTR_PUBLISHED) 
  private boolean published;
  @Column(name = COL_LAT, nullable = true, precision = 9, scale = 6)
  private Double latitude;
  @Column(name = COL_LON, nullable = true, precision = 9, scale = 6)
  private Double longitude;
  @Column(name = ATTR_ACTIVITY, nullable = true, length = 32)
  @Enumerated(EnumType.STRING)
  private ActivityType activity;
  @Column(name = "geolocation_available")
  private Boolean geolocationAvailable;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = ATTR_ID)
  private Integer id;
  @Version
  @Column(name = ATTR_VERSION)
  private Integer version;
  @Column(name = COL_LAST_CHANGE)
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastChange;  
}
