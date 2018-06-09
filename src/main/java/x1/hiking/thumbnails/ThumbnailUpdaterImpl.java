package x1.hiking.thumbnails;

import java.io.IOException;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.control.ImageService;
import x1.hiking.model.Image;
import x1.hiking.model.Thumbnail;
import x1.hiking.model.ThumbnailType;

/**
 * Job for updating thumbnails
 * 
 * @author joe
 * 
 */
@Singleton
@Startup
public class ThumbnailUpdaterImpl implements ThumbnailUpdater {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private static final String INFO_TEXT = "ThumbnailUpdater";
  
  @EJB
  private ThumbnailService thumbnailService;
  
  @EJB
  private ImageService imageService;

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.thumbnails.ThumbnailUpdater#updateThumbnails()
   */
  @Schedule(hour = "*", minute = "*/2", second = "0", persistent = true, info = INFO_TEXT)
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public void updateThumbnails() {
    log.trace("Updating thumbnails...");
    List<Image> images = thumbnailService.findImagesToUpdate();
    for (Image image : images) {
      try {
        updateThumbnails(image);
        imageService.deleteImageData(image);
      } catch (Exception e) {
        log.error(null, e);
      }
    }
    if (!images.isEmpty()) {
      log.info("Updated thumbnails for {} images", images.size());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * x1.hiking.thumbnails.ThumbnailUpdater#updateThumbnails(x1.hiking.model.
   * Image)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void updateThumbnails(Image image) throws IOException {
    for (ThumbnailType type : ThumbnailType.values()) {
      if (!type.equals(ThumbnailType.NONE)) {
        updateThumbnail(image, type);
      }
    }
  }

  private void updateThumbnail(Image image, ThumbnailType type) throws IOException {
    log.info("Updating thumbnail {} for image {}", type, image);
    List<Thumbnail> thumbnails = thumbnailService.findThumbnails(image, type);
    for (Thumbnail thumbnail : thumbnails) {
      thumbnailService.delete(thumbnail);
    }
    Thumbnail thumbnail = thumbnailService.createThumbnail(image, type);
    if (thumbnail != null) {
      thumbnailService.insert(thumbnail);
    }
  }

}
