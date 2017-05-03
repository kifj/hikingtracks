package x1.hiking.dao;

import java.util.List;

import x1.hiking.model.Image;
import x1.hiking.model.Thumbnail;
import x1.hiking.model.ThumbnailType;
import x1.hiking.model.User;

/** The Thumbnail DAO.
 * 
 * @author joe
 *
 */
public interface ThumbnailDAO extends JpaDAO<Thumbnail> {

  /**
   * Find thumbnails.
   *
   * @param image the image
   * @param type the type
   * @return the list
   */
  List<Thumbnail> findThumbnails(final Image image, final ThumbnailType type);

  /**
   * Find thumbnail.
   *
   * @param user the user
   * @param name the name
   * @param id the id
   * @param type the type
   * @return the thumbnail
   */
  Thumbnail findThumbnail(final User user, final String name, final Integer id, final ThumbnailType type);

}