package x1.hiking.representation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import x1.hiking.model.Image;

/**
 * image representation.
 * 
 * @author joe
 */
@JsonInclude(Include.NON_NULL)
public class ImageInfo implements Representation, FilenameInfo {
  private static final long serialVersionUID = 4915092534000608062L;
  private final Logger log = LoggerFactory.getLogger(ImageInfo.class);
  
  /**
   * Default constructor.
   */
  public ImageInfo() {
  }

  /**
   * Constructor with image.
   * 
   * @param image
   *          the image
   * @param path
   *          the path
   */
  public ImageInfo(Image image, String path) {
    setId(image.getId());
    setName(image.getName());
    setLatitude(image.getLatitude());
    setLongitude(image.getLongitude());
    setDate(image.getDate());
    if (StringUtils.isNotEmpty(image.getUrl())) {
      setUrl(image.getUrl());
    } else {
      setUrl(image, path);
    }
  }

  private void setUrl(Image image, String path) {
    try {
      URI location = new URI(path + image.getId());
      setUrl(location.toString());
    } catch (URISyntaxException e) {
      log.warn(null, e);
    }
  }
  
  /**
   * Gets the name.
   * 
   * @return the name
   */
  @XmlElement(required = true)
  @Override
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   * 
   * @param name
   *          the name to set
   */
  @Override
  public void setName(String name) {
    this.name = name;
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
   *          the latitude to set
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
   *          the longitude to set
   */
  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }
  
  /**
   * Gets the date.
   * 
   * @return the date
   */
  public Date getDate() {
    return date;
  }
  
  /**
   * Sets the date
   * 
   * @param date
   *          the date to set
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.representation.FilenameInfo#getUrl()
   */
  @Override
  public String getUrl() {
    return url;
  }
  
  /*
   * (non-Javadoc)
   * @see x1.hiking.representation.FilenameInfo#setUrl(java.lang.String)
   */
  @Override
  public void setUrl(String url) {
    this.url = url;
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.representation.FilenameInfo#getId()
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
  public void setId(Integer id) {
    this.id = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "<imageinfo " + getId() + ": " + getName() + ">";
  }

  private Integer id;
  @Size(max = 100)
  @NotNull(message = "Image must have a name")
  private String name;
  @Size(max = 200)
  private String url;
  private Double latitude;
  private Double longitude;
  private Date date;
}
