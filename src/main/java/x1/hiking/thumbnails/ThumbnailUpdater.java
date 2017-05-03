package x1.hiking.thumbnails;

import java.io.IOException;

import x1.hiking.model.Image;

/** Job for updating thumbnails
 * 
 * @author joe
 *
 */
public interface ThumbnailUpdater {

  /** Update all thumbnails
   *  
   * @param image the image
   */
  void updateThumbnails(Image image) throws IOException;

  /**
   *  Update all thumbnails which need to be updated
   */
  void updateThumbnails();
}