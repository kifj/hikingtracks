package x1.hiking.representation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Model object for list of tracks.
 * 
 * @author joe
 */
@JsonInclude(Include.NON_NULL)
@XmlRootElement(name = "tracks", namespace = Representation.NS_HIKING_TRACKS)
public class TrackInfoList implements Representation, Cacheable {
  private static final long serialVersionUID = 3489560212351571863L;

  /**
   * Instantiates a new track info list.
   */
  public TrackInfoList() {
    this.trackInfos = new ArrayList<>();
  }

  /**
   * Instantiates a new track info list.
   * 
   * @param trackInfos
   *          the track infos
   */
  public TrackInfoList(Collection<TrackInfo> trackInfos) {
    this.trackInfos = new ArrayList<>(trackInfos);
  }

  /**
   * Instantiates a new track info list.
   * 
   * @param trackInfos
   *          the track infos
   */
  public TrackInfoList(TrackInfo[] trackInfos) {
    this.trackInfos = Arrays.asList(trackInfos);
  }

  /**
   * Gets the track infos.
   * 
   * @return the track infos
   */
  @XmlElement(name = "track")
  public List<TrackInfo> getTrackInfos() {
    return trackInfos;
  }

  /**
   * Sets the track infos.
   * 
   * @param trackInfos
   *          the new track infos
   */
  public void setTrackInfos(List<TrackInfo> trackInfos) {
    this.trackInfos = trackInfos;
  }

  /**
   * get the total number of tracks
   * 
   * @return the total
   */
  @XmlAttribute(name = "total")
  public Long getTotal() {
    return total;
  }

  /**
   * Sets the total.
   * 
   * @param total
   *          the new total number
   */
  public void setTotal(Long total) {
    this.total = total;
  }

  /** 
   * get the start position
   * 
   * @return the startPosition
   */
  @XmlAttribute(name = "start")
  public Integer getStartPosition() {
    return startPosition;
  }

  /** Sets the start position
   * 
   * @param startPosition
   *          the startPosition to set
   */
  public void setStartPosition(Integer startPosition) {
    this.startPosition = startPosition;
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.representation.Cacheable#computeEntityTag(java.lang.StringBuilder)
   */
  @Override
  public void computeEntityTag(StringBuilder buffer) {
    buffer.append(";").append(getStartPosition());
    buffer.append(";").append(getTotal());
  }

  private Integer startPosition;
  private Long total;
  private List<TrackInfo> trackInfos;
}
