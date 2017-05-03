package x1.hiking.dao;

import java.util.List;

import x1.hiking.model.Image;
import x1.hiking.model.Track;
import x1.hiking.model.User;

/**
 * The Image DAO.
 * 
 * @author joe
 *
 */
public interface ImageDAO extends JpaDAO<Image> {

  /**
   * Find image.
   *
   * @param user
   *          the user
   * @param name
   *          the name
   * @param id
   *          the id
   * @return the image
   */
  Image findImage(final User user, final String name, final Integer id);

  /**
   * find Images to update.
   *
   * @return the list of images
   */
  List<Image> findImagesToUpdate();

  /**
   * find Images for Track
   * 
   * @param track
   *          the track
   * @return the list of images
   */
  List<Image> find(Track track);
}