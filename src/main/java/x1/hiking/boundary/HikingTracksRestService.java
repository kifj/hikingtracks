package x1.hiking.boundary;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import x1.hiking.model.ActivityType;
import x1.hiking.model.ThumbnailType;
import x1.hiking.representation.Representation;
import x1.hiking.representation.Search;
import x1.hiking.representation.TrackInfo;
import x1.hiking.representation.UserInfo;

/**
 * Public accessible REST service.
 *
 * @author joe
 */
@Path("/1.0/")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface HikingTracksRestService extends Representation {
  String PATH_USER = "/user";
  String PATH_TRACKS = "/tracks";
  String PATH_SEARCH = "/search";
  String HEADER_FILE_NAME = "X-File-Name";
  String MSG_MISSING_NAME = "Missing name";
  String MSG_EMPTY_BODY = "Empty body"; 
  String THUMBNAIL = "thumbnail";
  String HEADER_VARY_ACCEPT = "Accept";
  String HEADER_CORS_ALL = "*";
  String ACCESS_CONTROL_ALLOW_ORIGIN = "ACCESS_CONTROL_ALLOW_ORIGIN";
  String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
  String HEADER_CORS_ALLOWED_HEADERS = "Content-Type, Cache-Control";

  /**
   * get all tracks.
   *
   * @param name the name
   * @param startPosition the start position
   * @param maxResults max number of results
   * @param onlyPublished only published tracks
   * @param thumbnail requested thumnail size
   * @param activityType requested activity type
   * @return the tracks
   */
  @GET
  @Path(PATH_TRACKS)
  Response getTracks(@QueryParam("name") String name, @QueryParam("start") Integer startPosition,
      @QueryParam("max") Integer maxResults, @QueryParam("public") boolean onlyPublished, 
      @QueryParam("thumbnail") @DefaultValue(value = "MEDIUM") ThumbnailType thumbnail,
      @QueryParam("activity") ActivityType activityType);

  /**
   * get all tracks within given bounds
   *
   * @param search the search
   * @return the tracks
   */
  @POST
  @Path(PATH_SEARCH)
  Response getTracks(Search search);

  /**
   * get track.
   *
   * @param name the name
   * @param includePublished include published tracks
   * @return the track
   */
  @GET
  @Path(PATH_TRACKS + SEP + "{name}")
  Response getTrack(@PathParam("name") String name, @QueryParam("public") boolean includePublished);

  /**
   * delete track.
   *
   * @param name the name
   * @return the response
   */
  @DELETE
  @Path(PATH_TRACKS + SEP + "{name}")
  Response deleteTrack(@PathParam("name") String name);

  /**
   * insert track.
   *
   * @param track the track
   * @return the response
   */
  @POST
  @Path(PATH_TRACKS)
  Response insertTrack(TrackInfo track);

  /**
   * update track.
   *
   * @param name the name
   * @param track the track
   * @return the response
   */
  @PUT
  @Path(PATH_TRACKS + SEP + "{name}")
  Response updateTrack(@PathParam("name") String name, TrackInfo track);

  /**
   * get user.
   *
   * @return the user
   */
  @GET
  @Path(PATH_USER)
  UserInfo getUser();

  /**
   * update user.
   *
   * @param user the user
   * @return the response
   */
  @PUT
  @Path(PATH_USER)
  Response updateUser(UserInfo user);

  /**
   * delete user.
   *
   * @return the response
   */
  @DELETE
  @Path(PATH_USER)
  Response deleteUser();

  /**
   * get image.
   *
   * @param name the track name
   * @param id the image id
   * @param type the type
   * @return the image
   */
  @GET
  @Produces({ MEDIA_TYPE_IMAGE_JPEG })
  @Path(PATH_TRACKS + SEP + "{name}" + SEP + PATH_IMAGES + "{id}")
  Response getImage(@PathParam("name") String name, @PathParam("id") Integer id,
      @QueryParam("thumbnail") ThumbnailType type);

  /**
   * insert image.
   *
   * @param name the track name
   * @param filename the filename
   * @param data the data
   * @return the response
   */
  @POST
  @Consumes({ MEDIA_TYPE_IMAGE_JPEG })
  @Path(PATH_TRACKS + SEP + "{name}" + SEP + PATH_IMAGES)
  Response insertImage(@PathParam("name") String name, @HeaderParam(HEADER_FILE_NAME) String filename, byte[] data);

  /**
   * update image.
   *
   * @param name the track name
   * @param filename the filename
   * @param id the image id
   * @param data the data
   * @return the response
   */
  @PUT
  @Consumes({ MEDIA_TYPE_IMAGE_JPEG })
  @Path(PATH_TRACKS + SEP + "{name}" + SEP + PATH_IMAGES + "{id}")
  Response updateImage(@PathParam("name") String name, @HeaderParam(HEADER_FILE_NAME) String filename,
      @PathParam("id") Integer id, byte[] data);

  /**
   * delete image.
   *
   * @param name the track name
   * @param id the image id
   * @return the response
   */
  @DELETE
  @Path(PATH_TRACKS + SEP + "{name}" + SEP + PATH_IMAGES + "{id}")
  Response deleteImage(@PathParam("name") String name, @PathParam("id") Integer id);

  /**
   * get track data.
   *
   * @param name the track name
   * @param id the kml id
   * @return the track data
   */
  @GET
  @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN, MEDIA_TYPE_VND_KML, MEDIA_TYPE_VND_KMZ })
  @Path(PATH_TRACKS + SEP + "{name}" + SEP + PATH_KML + "{id}")
  Response getTrackData(@PathParam("name") String name, @PathParam("id") Integer id);

  /**
   * insert track data.
   *
   * @param name the track name
   * @param filename the filename
   * @param incomingXML the incoming xml
   * @return the response
   */
  @POST
  @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.TEXT_PLAIN, MEDIA_TYPE_VND_KML, MEDIA_TYPE_VND_KMZ })
  @Produces({ MediaType.APPLICATION_XML })
  @Path(PATH_TRACKS + SEP + "{name}" + SEP + PATH_KML)
  Response insertTrackData(@PathParam("name") String name, @HeaderParam(HEADER_FILE_NAME) String filename,
      byte[] incomingXML);

  /**
   * update track data.
   *
   * @param name the track name
   * @param filename the filename
   * @param id the kml id
   * @param incomingXML the incoming xml
   * @return the response
   */
  @PUT
  @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.TEXT_PLAIN, MEDIA_TYPE_VND_KML, MEDIA_TYPE_VND_KMZ })
  @Produces({ MediaType.APPLICATION_XML })
  @Path(PATH_TRACKS + SEP + "{name}" + SEP + PATH_KML + "{id}")
  Response updateTrackData(@PathParam("name") String name, @HeaderParam(HEADER_FILE_NAME) String filename,
      @PathParam("id") Integer id, byte[] incomingXML);

  /**
   * delete track data.
   *
   * @param name the track name
   * @param id the kml id
   * @return the response
   */
  @DELETE
  @Path(PATH_TRACKS + SEP + "{name}" + SEP + PATH_KML + "{id}")
  Response deleteTrackData(@PathParam("name") String name, @PathParam("id") Integer id);

  @OPTIONS
  @Consumes("*/*")
  @Path("{path:.*}")
  Response options(@PathParam("path") String path);
}
