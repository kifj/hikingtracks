package x1.hiking.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.BooleanUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import x1.hiking.geocoding.KmlSampler;
import x1.hiking.model.Image;
import x1.hiking.model.Track;
import x1.hiking.model.TrackData;
import x1.hiking.model.User;

/**
 * track representation.
 * 
 * @author joe
 */
@XmlRootElement(name = "track", namespace = Representation.NS_HIKING_TRACKS)
public class TrackInfo implements Representation {
  private static final long serialVersionUID = -5544990370258937953L;

  /**
   * Instantiates a new track info.
   */
  public TrackInfo() {
  }

  /**
   * Instantiates a new track info.
   * 
   * @param track
   *          the track
   * @param path
   *          the path
   * @param user
   *          the user
   */
  public TrackInfo(Track track, String path, User user) {
    this(track, true, true, path, user);
  }

  /**
   * Instantiates a new track info.
   * 
   * @param track
   *          the track
   * @param withImages
   *          including images
   * @param withTrackData
   *          including trackdata
   * @param path
   *          the path
   * @param user
   *          the user
   */
  public TrackInfo(Track track, boolean withImages, boolean withTrackData, String path, User user) {
    setId(track.getId());
    setName(track.getName());
    setLocation(track.getLocation());
    setDescription(track.getDescription());
    setDate(track.getDate());
    setPublishDate(track.getPublishDate());
    setPublished(track.isPublished());
    setLastChange(track.getLastChange());
    setLatitude(track.getLatitude());
    setLongitude(track.getLongitude());
    if (track.getActivity() != null) {
      setActivity(track.getActivity().getSymbol());
    }
    if (BooleanUtils.isTrue(track.getUser().isPublished())) {
      setUser(new UserInfo(track.getUser()));
    }
    setReadOnly(!track.getUser().equals(user));
    if (withImages) {
      Collections.sort(track.getImages(), new ImageNumberComparator());
      for (Image image : track.getImages()) {
        addImage(new ImageInfo(image, path + SEP + PATH_IMAGES));
      }
    }
    if (withTrackData) {
      double d = 0d;
      for (TrackData td : track.getTrackData()) {
        TrackDataInfo tdi = new TrackDataInfo(td, path + SEP + PATH_KML);
        addTrackData(tdi);
        KmlSampler.Result result = KmlSampler.parse(td, d);
        tdi.setSamples(result.getSamples());
        d += result.getDistance();
      }
      if (d > 0) {
        setDistance(d);
      }
    }
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  @XmlElement(name = "name", required = true)
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   * 
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the description.
   * 
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description.
   * 
   * @param description
   *          the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the location.
   * 
   * @return the location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Sets the location.
   * 
   * @param location
   *          the location to set
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Gets the date.
   * 
   * @return the date
   */
  @XmlSchemaType(name = "date")
  @XmlJavaTypeAdapter(DateAdapter.class)
  public Date getDate() {
    return date;
  }

  /**
   * Sets the date.
   * 
   * @param date
   *          the date to set
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * Gets the user.
   * 
   * @return the user
   */
  @XmlElement(required = true)
  public UserInfo getUser() {
    return user;
  }

  /**
   * Sets the user.
   * 
   * @param user
   *          the user to set
   */
  public void setUser(UserInfo user) {
    this.user = user;
  }

  /**
   * Checks if is published.
   * 
   * @return true, if is published
   */
  public boolean isPublished() {
    return published;
  }

  /**
   * Sets the published.
   * 
   * @param published
   *          the new published
   */
  public void setPublished(boolean published) {
    this.published = published;
  }

  /**
   * Gets the publish date.
   * 
   * @return the publish date
   */
  @XmlSchemaType(name = "date")
  @XmlJavaTypeAdapter(DateAdapter.class)
  public Date getPublishDate() {
    return publishDate;
  }

  /**
   * Sets the date.
   * 
   * @param publishDate
   *          the publish date to set
   */
  public void setPublishDate(Date publishDate) {
    this.publishDate = publishDate;
  }

  /**
   * Gets the id.
   * 
   * @return the id
   */
  public Integer getId() {
    return id;
  }

  /**
   * Sets the id.
   * 
   * @param id
   *          the id to set
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Gets the last change.
   * 
   * @return the lastChange
   */
  public Date getLastChange() {
    return lastChange;
  }

  /**
   * Sets the last change.
   * 
   * @param lastChange
   *          the lastChange to set
   */
  public void setLastChange(Date lastChange) {
    this.lastChange = lastChange;
  }

  /**
   * Gets the images.
   * 
   * @return the images
   */
  @XmlElement(name = "image")
  public List<ImageInfo> getImages() {
    if (this.images == null) {
      this.images = new ArrayList<>();
    }
    return images;
  }

  /**
   * Sets the images.
   * 
   * @param images
   *          the images to set
   */
  public void setImages(List<ImageInfo> images) {
    this.images = images;
  }

  /**
   * Adds the image.
   * 
   * @param element
   *          the element
   */
  public void addImage(ImageInfo element) {
    if (this.images == null) {
      this.images = new ArrayList<>();
    }
    this.images.add(element);
  }

  /**
   * Removes the image.
   * 
   * @param element
   *          the element
   * @return true, if successful
   */
  public boolean removeImage(ImageInfo element) {
    if (this.images == null) {
      return false;
    }
    return this.images.remove(element);
  }

  /**
   * Gets the track data.
   * 
   * @return the trackData
   */
  @XmlElement(name = "trackdata")
  public List<TrackDataInfo> getTrackData() {
    if (this.trackData == null) {
      this.trackData = new ArrayList<>();
    }
    return trackData;
  }

  /**
   * Sets the track data.
   * 
   * @param trackData
   *          the trackData to set
   */
  public void setTrackData(List<TrackDataInfo> trackData) {
    this.trackData = trackData;
  }

  /**
   * Adds the track data.
   * 
   * @param element
   *          the element
   */
  public void addTrackData(TrackDataInfo element) {
    if (this.trackData == null) {
      this.trackData = new ArrayList<>();
    }
    this.trackData.add(element);
  }

  /**
   * Removes the track data.
   * 
   * @param element
   *          the element
   * @return true, if successful
   */
  public boolean removeTrackData(TrackDataInfo element) {
    if (this.trackData == null) {
      return false;
    }
    return this.trackData.remove(element);
  }

  /**
   * Gets the links.
   * 
   * @return the links
   */
  @XmlElement(name = "link")
  public List<Link> getLinks() {
    if (this.links == null) {
      this.links = new ArrayList<>();
    }
    return links;
  }

  /**
   * Sets the links.
   * 
   * @param links
   *          the links to set
   */
  public void setLinks(List<Link> links) {
    this.links = links;
  }

  /**
   * Adds the link.
   * 
   * @param element
   *          the element
   */
  public void addLink(Link element) {
    if (this.links == null) {
      this.links = new ArrayList<>();
    }
    this.links.add(element);
  }

  /**
   * is track new.
   * 
   * @return true, if is new
   */
  @XmlTransient
  @JsonIgnore
  public boolean isNew() {
    return id == null;
  }

  /**
   * Gets the latitude.
   * 
   * @return the latitude
   */
  public Double getLatitude() {
    return latitude;
  }

  /**
   * Sets the latitude.
   * 
   * @param latitude
   *          the new latitude
   */
  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  /**
   * Gets the longitude.
   * 
   * @return the longitude
   */
  public Double getLongitude() {
    return longitude;
  }

  /**
   * Sets the longitude.
   * 
   * @param longitude
   *          the new longitude
   */
  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  /**
   * Gets the distance.
   * 
   * @return the distance
   */
  public Double getDistance() {
    return distance;
  }

  /**
   * Sets the distance.
   * 
   * @param distance
   *          the new distance
   */
  public void setDistance(Double distance) {
    this.distance = distance;
  }

  /**
   * Sets the activity
   * 
   * @param activity
   *          the new activity;
   */
  public void setActivity(String activity) {
    this.activity = activity;
  }

  /**
   * @return the activity
   */
  public String getActivity() {
    return activity;
  }

  /**
   * @param readOnly
   *          is track read only
   */
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  /**
   * @return is track read only
   */
  public boolean isReadOnly() {
    return readOnly;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "<trackinfo " + getId() + ": " + getName() + ">";
  }

  /** Sort image by number */
  private static final class ImageNumberComparator implements Comparator<Image>, Serializable {
    private static final long serialVersionUID = -8842655256273771314L;

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Image i1, Image i2) {
      Integer n1 = i1.getNumber();
      Integer n2 = i2.getNumber();
      if (n1 == null && n2 == null) {
        return i1.getId().compareTo(i2.getId());
      } else if (n2 == null) {
        return -1;
      } else if (n1 == null) {
        return +1;
      } else {
        return n1.compareTo(n2);
      }
    }
  }

  public boolean hasChanged(Double a, Double b) {
    if (a == null && b == null) {
      return false;
    }
    if (a == null || b == null) {
      return true;
    }
    return Math.abs(a - b) > 0.0001f;
  }

  private List<Link> links;
  @NotNull(message = "Name may not be empty")
  @Size(max = 100)
  private String name;
  private String description;
  @Size(max = 100)
  private String location;
  private Date date;
  private Date publishDate;
  @Valid
  private UserInfo user;
  private boolean published;
  private Date lastChange;
  private Double latitude;
  private Double longitude;
  private Double distance;
  private String activity;
  private boolean readOnly;
  private Integer id;
  @Valid
  private List<ImageInfo> images;
  @Valid
  private List<TrackDataInfo> trackData;

}
