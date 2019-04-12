package x1.hiking.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Thumbnail data object
 * 
 * @author joe
 */
@Entity
@Table(name = "thumbnail", indexes = {
    @Index(name = "idx_thumbnail_image_type", unique = true, columnList = Thumbnail.ATTR_TYPE + "," + Thumbnail.COL_IMAGE_ID),
    @Index(name = "idx_thumbnail_image_id", columnList = Thumbnail.COL_IMAGE_ID, unique = false) 
})
@NamedQuery(name = "Thumbnail.findThumbnailsByImageAndType", 
  query = "SELECT t FROM Thumbnail t WHERE t.image = :image AND t.type = :type ORDER BY t.id")
@NamedQuery(name = "Thumbnail.findThumbnailByUserAndNameAndId",
  query = "SELECT t FROM Thumbnail t "
        + "WHERE t.image.track.user = :user AND t.image.track.name = :name "
        + "AND t.image.id = :id AND t.type = :type")
@NamedQuery(name = "Thumbnail.findPublicThumbnailByNameAndId",
  query = "SELECT t FROM Thumbnail t "
        + "WHERE t.image.track.published = true AND t.image.track.name = :name "
        + "AND t.image.id = :id AND t.type = :type")
public class Thumbnail implements Model {
  private static final long serialVersionUID = -3206759449507185411L;
  public static final String COL_IMAGE_ID = "image_id";
  public static final String ATTR_IMAGE = "image";
  public static final String COL_IMAGE_DATA = "image_data";
  public static final String ATTR_TYPE = "type";
  
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
   * @return the image
   */
  public Image getImage() {
    return image;
  }

  /**
   * @param image
   *          the image to set
   */
  public void setImage(Image image) {
    this.image = image;
  }

  /**
   * @return the type
   */
  public ThumbnailType getType() {
    return type;
  }

  /**
   * @param type
   *          the typeto set
   */
  public void setType(ThumbnailType type) {
    this.type = type;
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
    return "<thumbnail id=" + getId() + ":" + getType() + ">";
  }

  @Column(name = ATTR_TYPE, nullable = false)
  @NotNull
  private ThumbnailType type;
  @Column(name = COL_IMAGE_DATA, nullable = false)
  @Lob
  @NotNull
  private byte[] data;
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = COL_IMAGE_ID, foreignKey = @ForeignKey(name = "fk_thumbnail_image"))
  private Image image;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = ATTR_ID)
  private Integer id;
  @Version
  @Column(name = ATTR_VERSION)
  private Integer version;
}
