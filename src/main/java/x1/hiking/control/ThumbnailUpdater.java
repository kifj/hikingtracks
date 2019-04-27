package x1.hiking.control;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.geocoding.InverseGeocoder;
import x1.hiking.model.Coord;
import x1.hiking.model.Image;
import x1.hiking.model.ImageData;
import x1.hiking.model.ThumbnailType;
import x1.hiking.model.Track;

/**
 * Job for updating thumbnails
 */
@Singleton
@Startup
public class ThumbnailUpdater {
  private final Logger log = LoggerFactory.getLogger(ThumbnailUpdater.class);
  private static final String INFO_TEXT = "ThumbnailUpdater";

  @EJB
  private ThumbnailService thumbnailService;

  @EJB
  private ImageService imageService;

  @Inject
  private InverseGeocoder inverseGeocoder;
  
  /**
   * Update all thumbnails which need to be updated
   */
  @Schedule(hour = "*", minute = "*/2", second = "0", persistent = true, info = INFO_TEXT)
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void updateThumbnails() {
    log.trace("Updating thumbnails...");
    List<Image> images = thumbnailService.findImagesToUpdate();
    for (Image image : images) {
      try {
        updateThumbnails(image);
        image = updateGeolocation(image);
        imageService.deleteImageData(image);
      } catch (Exception e) {
        log.error(null, e);
      }
    }
    if (!images.isEmpty()) {
      log.info("Updated thumbnails for {} images", images.size());
    }
  }

  /**
   * Update geolocation data from image
   *
   * @param image
   *          the image
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Image updateGeolocation(Image image) {
    Optional<ImageData> imageData = imageService.getImageData(image);
    if (imageData.isPresent()) {
      image = new ExivReader(imageData.get()).readExiv();
      image = imageService.update(image);
      Track track = image.getTrack();
      if (StringUtils.isEmpty(track.getLocation()) && image.getLatitude() != null && image.getLongitude() != null) {
        inverseGeocoder.getLocationsForImage(new Coord(image.getLatitude(), image.getLongitude()))
            .ifPresent(geolocation -> track.setLocation(geolocation.getLocation() + ", " + geolocation.getCountry()));
      }
      if (image.getDate() != null && track.getDate() == null) {
        track.setDate(image.getDate());
      }
    }
    return image;
  }

  /**
   * Update all thumbnails for one image
   *
   * @param image
   *          the image
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void updateThumbnails(Image image) throws IOException {
    for (ThumbnailType type : ThumbnailType.values()) {
      if (type != ThumbnailType.NONE) {
        updateThumbnail(image, type);
      }
    }
  }

  private void updateThumbnail(Image image, ThumbnailType type) throws IOException {
    log.info("Updating thumbnail {} for image {}", type, image);
    thumbnailService.findThumbnails(image, type).forEach(thumbnailService::delete);
    thumbnailService.createThumbnail(image, type).ifPresent(thumbnailService::insert);
  }

}
