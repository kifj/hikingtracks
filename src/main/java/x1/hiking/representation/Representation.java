package x1.hiking.representation;

import java.io.Serializable;

/**
 * Base interface for REST representation
 * 
 * @author joe
 *
 */
public interface Representation extends Serializable {
  String NS_HIKING_TRACKS = "http://www.x1/hikingtracks";
  String SEP = "/";
  String PATH_KML = "kml/";
  String PATH_IMAGES = "images/";
  String MEDIA_TYPE_VND_KML = "application/vnd.google-earth.kml+xml";
  String MEDIA_TYPE_VND_KMZ = "application/vnd.google-earth.kmz";
  String MEDIA_TYPE_VND_GPX = "application/gpx+xml";
  String MEDIA_TYPE_IMAGE_JPEG = "image/jpeg";
  String FILE_EXTENSION_KML = ".kml";
  String FILE_EXTENSION_GPX = ".gpx";
  String FILE_EXTENSION_KMZ = ".kmz";
  String FILE_EXTENSION_JPG = ".jpg";
  String PATH_SERVICE = "/services/hikingtracks/1.0";
}
