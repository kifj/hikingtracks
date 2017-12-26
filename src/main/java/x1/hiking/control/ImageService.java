package x1.hiking.control;

import java.util.List;

import x1.hiking.model.Image;
import x1.hiking.model.ImageData;
import x1.hiking.model.Track;
import x1.hiking.model.User;

/**
 * control images
 */
public interface ImageService {

  /**
   * Find image.
   *
   * @param user the user
   * @param name the name
   * @param id the id
   * @return the image
   */
  Image findImage(User user, String name, Integer id);

  /**
   * Insert.
   *
   * @param entity the entity
   */
  void insert(Image entity);
  
  /**
   * Insert.
   *
   * @param entity the entity
   * @param data the data 
   */
  void insert(Image entity, byte[] data);  
  
  /**
   * Update.
   *
   * @param entity the entity
   * @param data the data
   * @return the image
   */
  Image update(Image entity, byte[] data);

  /**
   * Update.
   *
   * @param entity the entity
   * @return the image
   */
  Image update(Image entity);

  /**
   * Delete.
   *
   * @param entity the entity
   */
  void delete(Image entity);
  
  /**
   * Find first image.
   *
   * @param track the track
   * @return the image
   */
  Image findFirstImage(Track track);
  
  /**
   * get Image data
   * 
   * @param image the image
   * @return the image data
   */
  ImageData getImageData(Image image);
  
  /**
   * delete Image data
   * 
   * @param image the image
   */
  void deleteImageData(Image image);
  
  /**
   * find Images to update.
   *
   * @return the list of images
   */
  List<Image> findImagesToUpdate();
}