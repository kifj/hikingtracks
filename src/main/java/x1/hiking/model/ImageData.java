package x1.hiking.model;

import javax.persistence.*;

/**
 * Image data model class.
 * 
 * @author joe
 */
@Entity
@Table(name = "image_data")
@NamedQueries({
  @NamedQuery(name = "ImageData.getImage", query = "SELECT i FROM ImageData i WHERE i.image = :image"),
  @NamedQuery(name = "ImageData.deleteImage", query = "DELETE FROM ImageData i WHERE i.image = :image")
})
public class ImageData implements Model {
  private static final long serialVersionUID = 7915371631791439871L;

  /**
   * Gets the data.
   * 
   * @return the data
   */
  public byte[] getData() {
    return data;
  }

  /**
   * Sets the data.
   * 
   * @param data
   *          the data to set
   */
  public void setData(byte[] data) {
    this.data = data;
  }

  /**
   * Gets the image.
   * 
   * @return the image
   */
  public Image getImage() {
    return image;
  }

  /**
   * Sets the image.
   * 
   * @param image
   *          the new image
   */
  public void setImage(Image image) {
    this.image = image;
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
    return "<" + getId() + ">";
  }

  @Column(name = "image_data", nullable = true)
  @Lob
  private byte[] data;

  @OneToOne(optional = false)
  @JoinColumn(name = "image_id", foreignKey = @ForeignKey(name = "fk_image_data_image"))
  private Image image;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  private Integer id;

  @Version
  @Column(name = "version")
  private Integer version;
}
