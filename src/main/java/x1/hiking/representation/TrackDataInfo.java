package x1.hiking.representation;

import java.net.URI;
import java.net.URISyntaxException;

import javax.activation.MimetypesFileTypeMap;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.geocoding.Waypoint;
import x1.hiking.model.Coord;
import x1.hiking.model.TrackData;

/**
 * Trackdata representation
 * 
 * @author joe
 * 
 */
public class TrackDataInfo implements Representation, FilenameInfo {
  private static final long serialVersionUID = -9176892648902446018L;
  private final Logger log = LoggerFactory.getLogger(TrackDataInfo.class);

  /**
   * Default constructor
   */
  public TrackDataInfo() {
  }

  /**
   * Constructor with TrackData
   * 
   * @param trackData
   *          the trackData
   * @param path
   *          the path
   */
  public TrackDataInfo(TrackData trackData, String path) {
    setId(trackData.getId());
    setName(trackData.getName());
    setUrl(trackData.getUrl());
    setStartPoint(trackData.getStartPoint());
    setEndPoint(trackData.getEndPoint());
    setLowestPoint(trackData.getLowestPoint());
    setHighestPoint(trackData.getHighestPoint());
    if (trackData.getData() != null) {
      try {
        URI location = new URI(path + trackData.getId());
        setUrl(location.toString());
      } catch (URISyntaxException e) {
        log.warn(null, e);
      }
    }
  }

  public static String getMediaType(String filename) {
    if (StringUtils.endsWith(filename, FILE_EXTENSION_KML)) {
      return MEDIA_TYPE_VND_KML;
    } else if (StringUtils.endsWith(filename, FILE_EXTENSION_KMZ)) {
      return MEDIA_TYPE_VND_KMZ;
    } else {
      // META-INF/mime.types
      return MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(filename);
    }
  }
  
  /*
   * (non-Javadoc)
   * @see x1.hiking.representation.FilenameInfo#getName()
   */
  @XmlElement(required = true)
  @Override
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.representation.FilenameInfo#setName(java.lang.String)
   */
  @Override
  public void setName(String name) {
    this.name = name;
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
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * @return the samples
   */
  public Waypoint[] getSamples() {
    return samples;
  }

  /**
   * @param samples the samples
   */
  public void setSamples(Waypoint[] samples) {
    this.samples = samples;
  }
  
  /**
   * @return the startPoint
   */
  @XmlElement
  public Coord getStartPoint() {
    return startPoint;
  }

  /**
   * @param startPoint the startPoint to set
   */
  public void setStartPoint(Coord startPoint) {
    this.startPoint = startPoint;
  }

  /**
   * @return the endPoint
   */
  @XmlElement
  public Coord getEndPoint() {
    return endPoint;
  }

  /**
   * @param endPoint the endPoint to set
   */
  public void setEndPoint(Coord endPoint) {
    this.endPoint = endPoint;
  }

  /**
   * @return the lowestPoint
   */
  @XmlElement
  public Coord getLowestPoint() {
    return lowestPoint;
  }

  /**
   * @param lowestPoint the lowestPoint to set
   */
  public void setLowestPoint(Coord lowestPoint) {
    this.lowestPoint = lowestPoint;
  }

  /**
   * @return the highestPoint
   */
  @XmlElement
  public Coord getHighestPoint() {
    return highestPoint;
  }

  /**
   * @param highestPoint the highestPoint to set
   */
  public void setHighestPoint(Coord highestPoint) {
    this.highestPoint = highestPoint;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "<trackdatainfo " + getId() + ": " + getName() + ">";
  }

  @Size(max = 100)
  @NotNull(message = "Track data must have a name")
  private String name;
  @Size(max = 200)
  private String url;
  private Integer id;
  private Waypoint[] samples;
  private Coord startPoint;
  private Coord endPoint;
  private Coord lowestPoint;
  private Coord highestPoint;
}
