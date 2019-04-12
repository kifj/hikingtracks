package x1.hiking.control;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;

import x1.hiking.model.Image;
import x1.hiking.model.ImageData;

public class ExivReader {
  private final Logger log = LoggerFactory.getLogger(ExivReader.class);
  private ImageData imageData;
  
  public ExivReader(ImageData imageData) {
    this.imageData = imageData;
  }
  
  public Image readExiv() {
    if (imageData.getData() != null) {
      try {
        ByteArrayInputStream in = new ByteArrayInputStream(imageData.getData());
        return readExiv(in);
      } catch (Exception e) {
        log.warn(e.getMessage());
      }
    }
    return imageData.getImage();
  }

  private Image readExiv(InputStream in) throws ImageProcessingException, IOException {
    Metadata metaData = ImageMetadataReader.readMetadata(in);
    Image image = imageData.getImage();
    GpsDirectory directory = metaData.getFirstDirectoryOfType(GpsDirectory.class);
    if (directory != null) {
      GeoLocation location = directory.getGeoLocation();
      Date date = directory.getGpsDate();
      if (location != null) {
        log.info("Found Geolocation {} for image {}", location, image);
        image.setLatitude(location.getLatitude());
        image.setLongitude(location.getLongitude());
      } else {
        image.setLatitude(null);
        image.setLongitude(null);
      }
      image.setDate(date);
    }
    return image;
  }
  
}
