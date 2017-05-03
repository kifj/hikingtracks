package x1.hiking.dao;

import x1.hiking.model.Image;
import x1.hiking.model.ImageData;

/** The ImageData DAO.
 * 
 * @author joe
 */
public interface ImageDataDAO extends JpaDAO<ImageData> {
  
  /**
   * Gets the image data.
   *
   * @param image the image
   * @return the image data
   */
  ImageData getImageData(Image image);
  
  /**
   * remove image data from image 
   * @param image the image
   */
  void removeImageData(Image image);
}
