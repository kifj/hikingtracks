package x1.hiking.thumbnails;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import x1.hiking.model.Image;
import x1.hiking.model.ImageData;
import x1.hiking.model.Thumbnail;
import x1.hiking.model.ThumbnailType;
import x1.hiking.model.User;

/**
 * Service for creating thumbnails.
 *
 * @author joe
 */
public interface ThumbnailService {
  
  /**
   * Creates the thumbnail.
   *
   * @param in the in
   * @param out the out
   * @param size the size
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void createThumbnail(InputStream in, OutputStream out, ThumbnailSize size) throws IOException;

  /**
   * Creates the thumbnail.
   *
   * @param in the in
   * @param size the size
   * @return the byte[]
   * @throws IOException Signals that an I/O exception has occurred.
   */
  byte[] createThumbnail(byte[] in, ThumbnailSize size) throws IOException;

  /**
   * Creates the thumbnail.
   *
   * @param image the image
   * @param type the type
   * @return the thumbnail
   * @throws IOException Signals that an I/O exception has occurred.
   */
  Thumbnail createThumbnail(ImageData image, ThumbnailType type) throws IOException;

  /**
   * Creates the thumbnail.
   *
   * @param image the image
   * @param type the type
   * @return the thumbnail
   * @throws IOException Signals that an I/O exception has occurred.
   */
  Thumbnail createThumbnail(Image image, ThumbnailType type) throws IOException;

  
  /**
   * Insert.
   *
   * @param thumbnail the thumbnail
   */
  void insert(Thumbnail thumbnail);

  /**
   * Update.
   *
   * @param thumbnail the thumbnail
   * @return the thumbnail
   */
  Thumbnail update(Thumbnail thumbnail);

  /**
   * Delete.
   *
   * @param thumbnail the thumbnail
   */
  void delete(Thumbnail thumbnail);

  /**
   * Find thumbnails.
   *
   * @param image the image
   * @param type the type
   * @return the list
   */
  List<Thumbnail> findThumbnails(Image image, ThumbnailType type);

  /**
   * Find images to update.
   *
   * @return the list
   */
  List<Image> findImagesToUpdate();

  /**
   * Find thumbnail.
   *
   * @param user the user
   * @param name the name
   * @param id the id
   * @param type the type
   * @return the thumbnail
   */
  Thumbnail findThumbnail(User user, String name, Integer id, ThumbnailType type);
}
