package x1.hiking.boundary;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import static javax.ws.rs.core.Response.Status.*;
import static x1.hiking.representation.ErrorMessageBuilder.*;

import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.control.ImageService;
import x1.hiking.control.QueryOptions;
import x1.hiking.control.ThumbnailService;
import x1.hiking.control.TrackService;
import x1.hiking.control.UserManagement;
import x1.hiking.geocoding.WaypointSampler;
import x1.hiking.model.*;
import x1.hiking.oauth.SessionValidator;
import x1.hiking.representation.*;
import x1.hiking.utils.ServletHelper;

/**
 * REST Web service frontend for Hiking Tracks Service
 * 
 * @author joe
 */
public class HikingTracksResource implements HikingTracksService {
  private static final long serialVersionUID = 8237702332418932465L;
  private final Logger log = LoggerFactory.getLogger(HikingTracksResource.class);
  private static final String IMG_PLACEHOLDER = "placeholder.jpg";

  @Context
  private Request request;

  @Context
  private HttpServletRequest httpServletRequest;

  @Context
  private HttpServletResponse httpServletResponse;

  @EJB
  private TrackService trackService;

  @EJB
  private ImageService imageService;

  @EJB
  private ThumbnailService thumbnailService;

  @Inject
  private SessionValidator sessionValidator;

  @EJB
  private UserManagement userManagement;

  @Inject
  private Validator validator;

  @Inject
  @ConfigProperty(name = "feed.url")
  private String top;

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#getTracks()
   */
  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  @Timed(name = "get-tracks", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response getTracks(String name, Integer startPosition, Integer maxResults, boolean onlyPublished,
      ThumbnailType thumbnail, ActivityType activity) {
    log.info("get tracks [name={}, activity={}, startPosition={}, maxResults={}, onlyPublished={}, thumbnail={}]", name,
        activity, startPosition, maxResults, onlyPublished, thumbnail);
    User user = findUser(true);
    QueryOptions options = buildQueryOptions(startPosition, maxResults, activity);
    List<Track> tracks = findTracks(name, onlyPublished, user, options);
    TrackInfoList trackInfoList = createTrackInfoList(tracks, thumbnail, user, false, false);
    if (options != null) {
      enrichTrackInfoListFromQueryOptions(name, startPosition, onlyPublished, activity, user, trackInfoList);
    }
    EntityTag eTag = new EntityTagBuilder(httpServletRequest).buildEntityTag(user, tracks, trackInfoList);
    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder != null) {
      return builder.build();
    }
    return Response.status(OK).entity(trackInfoList).tag(eTag).build();
  }

  private void enrichTrackInfoListFromQueryOptions(String name, Integer startPosition, boolean onlyPublished,
      ActivityType activity, User user, TrackInfoList trackInfoList) {
    trackInfoList.setStartPosition((startPosition == null) ? Integer.valueOf(0) : startPosition);
    Long total;
    if (user == null || onlyPublished) {
      total = trackService.countTracks(name, activity);
    } else {
      total = trackService.countTracks(user, name, activity);
    }
    trackInfoList.setTotal(total);
  }

  private List<Track> findTracks(String name, boolean onlyPublished, User user, QueryOptions options) {
    List<Track> tracks;
    if (user == null || onlyPublished) {
      tracks = trackService.findTracks(name, options);
    } else {
      tracks = trackService.findTracks(user, name, options);
    }
    return tracks;
  }

  private QueryOptions buildQueryOptions(Integer startPosition, Integer maxResults, ActivityType activity) {
    QueryOptions options = null;
    if (startPosition != null || maxResults != null || activity != null) {
      options = new QueryOptions(startPosition, maxResults, activity);
    }
    return options;
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  @Timed(name = "search-tracks", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response getTracks(Search search) {
    log.info("get tracks [search={}]", search);

    QueryOptions options = buildQueryOptions(search);
    List<Track> tracks = trackService.findTracks(search.getName(), search.getBounds(), options);
    TrackInfoList trackInfoList = createTrackInfoList(tracks, ThumbnailType.SMALL, null, false, true);
    log.debug("found {} tracks", trackInfoList.getTrackInfos().size());
    EntityTag eTag = new EntityTagBuilder(httpServletRequest).buildEntityTag(null, tracks, trackInfoList);
    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder != null) {
      return builder.build();
    }
    return Response.status(OK).entity(trackInfoList).tag(eTag).build();
  }

  private QueryOptions buildQueryOptions(Search search) {
    QueryOptions options = null;
    if (search.getActivity() != null || search.getMaxResults() != null) {
      options = new QueryOptions(null, search.getMaxResults(), search.getActivity());
    }
    return options;
  }

  private TrackInfoList createTrackInfoList(List<Track> tracks, ThumbnailType thumbnail, User user, boolean withImages,
      boolean withTrackData) {
    TrackInfo[] result = new TrackInfo[tracks.size()];
    for (int i = 0; i < result.length; i++) {
      Track track = tracks.get(i);
      TrackInfo trackInfo = new TrackInfo(track, withImages, withTrackData, null, user);
      addImage(thumbnail, track, trackInfo);
      result[i] = trackInfo;
    }
    return new TrackInfoList(result);
  }

  private void addImage(ThumbnailType thumbnail, Track track, TrackInfo trackInfo) {
    if (thumbnail != ThumbnailType.NONE) {
      imageService.findFirstImage(track).ifPresent(image -> {
        URI location = pathTracks().path(track.getName()).path(PATH_IMAGES).path(String.valueOf(image.getId()))
            .queryParam(THUMBNAIL, thumbnail).build();
        ImageInfo imageInfo = new ImageInfo(image, null);
        imageInfo.setUrl(location.toString());
        trackInfo.addImage(imageInfo);
      });
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#getTrack(java.lang.String, boolean)
   */
  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  @Timed(name = "get-track", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response getTrack(String name, boolean includePublished) {
    try {
      log.info("get track [name={}, includePublished={}]", name, includePublished);
      User user = findUser(true);
      Track track = getTrack(name, user, includePublished);
      TrackInfo trackInfo = new TrackInfo(track, pathTracks().path(name).build().toString(), user);
      List<Model> models = addLinks(track, trackInfo, user);
      EntityTag eTag = new EntityTagBuilder(httpServletRequest).buildEntityTag(user, models, track);
      Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
      if (builder != null) {
        return createResponse(builder);
      }
      return createResponse(Response.status(OK), trackInfo, eTag);
    } catch (NotFoundException e) {
      log.info(e.getMessage());
      throw e;
    }
  }

  private Track getTrack(String name, User user, boolean includePublished) {
    Track track = null;
    if (includePublished || user == null) {
      track = trackService.findTrack(name, true);
    }
    if (track == null && user != null) {
      track = trackService.findTrack(user, name, true);
    }
    if (track == null) {
      throw notFound(MSG_TRACK_MISSING, name);
    }
    return track;
  }

  private List<Model> addLinks(Track track, TrackInfo trackInfo, User user) {
    List<Model> result = new ArrayList<>();
    result.add(track);
    Track next = trackService.nextTrack(track, user);
    if (next != null) {
      result.add(next);
      if (trackInfo != null) {
        trackInfo.addLink(createTrackInfoLink(next.getName(), "next"));
      }
    }
    Track prev = trackService.previousTrack(track, user);
    if (prev != null) {
      result.add(prev);
      if (trackInfo != null) {
        trackInfo.addLink(createTrackInfoLink(prev.getName(), "previous"));
      }
    }
    return result;
  }

  private Link createTrackInfoLink(String name, String rel) {
    String path = UriBuilder.fromPath(top + PATH_SERVICE + PATH_TRACKS).path("{0}").build(name).toString();
    Link l = new Link();
    l.setHref(path);
    l.setRel(rel);
    l.setText(name);
    return l;
  }

  private Response createResponse(Response.ResponseBuilder builder, Object model, EntityTag eTag) {
    return createResponse(builder.entity(model), eTag);
  }

  private Response createResponse(Response.ResponseBuilder builder, EntityTag eTag) {
    return createResponse(builder.tag(eTag));
  }

  private Response createResponse(Response.ResponseBuilder builder) {
    return builder.header(HttpHeaders.VARY, HEADER_VARY_ACCEPT).header(ACCESS_CONTROL_ALLOW_ORIGIN, HEADER_CORS_ALL)
        .build();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#deleteTrack(java.lang.String)
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @Timed(name = "delete-track", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response deleteTrack(String name) {
    try {
      User user = findUser(false);
      Track track = trackService.findTrack(user, name, false);
      if (track != null) {
        log.info("delete track [{}]", name);
        trackService.delete(track);
        return Response.status(NO_CONTENT).build();
      } else {
        throw notFound(MSG_TRACK_MISSING, name);
      }
    } catch (NotFoundException e) {
      log.info(e.getMessage());
      throw e;
    } catch (WebApplicationException e) {
      log.error(null, e);
      throw e;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#insertTrack(x1.hiking.model.Track)
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @Timed(name = "insert-track", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response insertTrack(TrackInfo trackInfo) {
    try {
      User user = findUser(false);
      validateForInsert(trackInfo);
      Track oldTrack = trackService.findTrack(user, trackInfo.getName(), false);
      if (oldTrack != null) {
        throw conflict(MSG_TRACK_ALREADY_EXISTS, trackInfo.getName());
      }
      Track track = new Track();
      updateTrack(trackInfo, track);
      track.setUser(user);
      track.getTrackData().forEach(td -> td.setTrack(track));
      track.getImages().forEach(img -> img.setTrack(track));
      log.info("insert track [{}]", track);
      trackService.insert(track);
      URI location = UriBuilder.fromPath(track.getName()).build();
      List<Model> models = addLinks(track, null, user);
      EntityTag eTag = new EntityTagBuilder(httpServletRequest).buildEntityTag(user, models, track);
      return createResponse(Response.status(CREATED).location(location),
          new TrackInfo(track, pathTracks().path(track.getName()).build().toString(), user), eTag);
    } catch (ConflictException e) {
      log.info(e.getMessage());
      throw e;
    }
  }

  private void validateForInsert(TrackInfo track) {
    track.getTrackData().removeIf(trackDataInfo -> !trackDataInfo.validateFilename());
    track.getImages().removeIf(imageInfo -> !imageInfo.validateFilename());
    validate(track);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#updateTrack(java.lang.String,
   * x1.hiking.model.Track)
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @Timed(name = "update-track", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response updateTrack(String name, TrackInfo track) {
    try {
      User user = findUser(false);
      Track oldTrack = trackService.findTrack(user, name, true);
      if (oldTrack == null) {
        return insertTrack(track);
      }
      List<Model> models = addLinks(oldTrack, null, user);
      EntityTag eTag = new EntityTagBuilder(httpServletRequest).buildEntityTag(user, models, oldTrack);
      Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
      if (builder != null) {
        log.info("ETag verification failed for ETag [{}]", eTag);
        return createResponse(builder);
      }
      updateTrack(track, oldTrack);
      validate(track);
      log.info("update track [{}]", track);
      Track updated = trackService.update(oldTrack);
      models = addLinks(updated, null, user);
      eTag = new EntityTagBuilder(httpServletRequest).buildEntityTag(user, models, updated);
      return createResponse(Response.status(OK),
          new TrackInfo(updated, pathTracks().path(name).build().toString(), user), eTag);
    } catch (NotFoundException e) {
      log.info(e.getMessage());
      throw e;
    }
  }

  private void updateTrack(TrackInfo track, Track trackToUpdate) {
    trackToUpdate.setDate(track.getDate());
    trackToUpdate.setName(track.getName());
    trackToUpdate.setDescription(track.getDescription());
    trackToUpdate.setLocation(track.getLocation());
    trackToUpdate.setPublished(track.isPublished());
    if (track.hasChanged(trackToUpdate.getLatitude(), track.getLatitude())
        || track.hasChanged(trackToUpdate.getLongitude(), track.getLongitude())) {
      trackToUpdate.setGeolocationAvailable(null);
    }
    trackToUpdate.setLatitude(track.getLatitude());
    trackToUpdate.setLongitude(track.getLongitude());
    trackToUpdate.setActivity(ActivityType.fromSymbol(track.getActivity()));
    updateTrackData(track, trackToUpdate);
    updateImages(track, trackToUpdate);
  }

  private void updateTrackData(TrackInfo track, Track trackToUpdate) {
    Map<Integer, TrackData> foundTd = new HashMap<>();
    for (TrackDataInfo td : track.getTrackData()) {
      if (td.validateFilename()) {
        updateTrackData(foundTd, td, trackToUpdate);
      }
    }
    trackToUpdate.getTrackData().removeIf(td -> td.getId() != null && !foundTd.containsKey(td.getId()));
  }

  private void updateImages(TrackInfo track, Track trackToUpdate) {
    Map<Integer, Image> foundImg = new HashMap<>();
    int number = 0;
    for (ImageInfo img : track.getImages()) {
      if (img.validateFilename()) {
        updateImage(foundImg, img, trackToUpdate, number);
      }
      number++;
    }
    trackToUpdate.getImages().removeIf(img -> img.getId() != null && !foundImg.containsKey(img.getId()));
  }

  private void updateTrackData(Map<Integer, TrackData> foundTd, TrackDataInfo trackData, Track trackToUpdate) {
    if (trackData.getId() == null) {
      TrackData newTrackData = newTrackDataForUpdate(trackData, trackToUpdate);
      trackToUpdate.addTrackData(newTrackData);
      return;
    }
    for (TrackData td : trackToUpdate.getTrackData()) {
      if (td.getId().equals(trackData.getId())) {
        existingTrackDataForUpdate(trackData, td);
        foundTd.put(td.getId(), td);
        break;
      }
    }
  }

  private void existingTrackDataForUpdate(TrackDataInfo trackData, TrackData td) {
    td.setName(trackData.getName());
    if (StringUtils.isNotEmpty(td.getUrl())) {
      td.setUrl(trackData.getUrl());
      td.setData(null);
    }
  }

  private TrackData newTrackDataForUpdate(TrackDataInfo trackData, Track trackToUpdate) {
    TrackData newTrackData = new TrackData();
    newTrackData.setTrack(trackToUpdate);
    newTrackData.setName(trackData.getName());
    newTrackData.setUrl(trackData.getUrl());
    return newTrackData;
  }

  private void updateImage(Map<Integer, Image> foundImg, ImageInfo image, Track trackToUpdate, int number) {
    if (image.getId() == null) {
      Image newImage = newImageForUpdate(image, trackToUpdate, number);
      trackToUpdate.addImage(newImage);
    } else {
      for (Image img : trackToUpdate.getImages()) {
        if (img.getId().equals(image.getId())) {
          existingImageForUpdate(image, number, img);
          foundImg.put(img.getId(), img);
          break;
        }
      }
    }
  }

  private void existingImageForUpdate(ImageInfo image, int number, Image img) {
    img.setName(image.getName());
    img.setNumber(number);
    if (StringUtils.isNotEmpty(img.getUrl())) {
      img.setUrl(image.getUrl());
      imageService.deleteImageData(img);
    }
  }

  private Image newImageForUpdate(ImageInfo image, Track trackToUpdate, int number) {
    Image newImage = new Image();
    newImage.setName(image.getName());
    newImage.setUrl(image.getUrl());
    newImage.setLatitude(image.getLatitude());
    newImage.setLongitude(image.getLongitude());
    newImage.setTrack(trackToUpdate);
    newImage.setNumber(number);
    return newImage;
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#getUser()
   */
  @Override
  @Timed(name = "get-user", absolute = true, unit = MetricUnits.MILLISECONDS)
  public UserInfo getUser() {
    User user = findUser(false);
    return new UserInfo(user);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#updateUser(x1.hiking.model.User)
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @Timed(name = "update-user", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response updateUser(UserInfo user) {
    validate(user);
    User oldUser = findUser(false);
    oldUser.setName(user.getName());
    oldUser.setPublished(user.isPublished());
    log.info("update user [{}]", oldUser);
    User updatedUser = userManagement.update(oldUser);
    return Response.status(OK).entity(new UserInfo(updatedUser)).build();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#deleteUser()
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public Response deleteUser() {
    User user = findUser(false);
    log.info("delete user [{}]", user);
    userManagement.delete(user);
    HttpSession session = httpServletRequest.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    return Response.status(NO_CONTENT).build();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#getImage(java.lang.String,
   * java.lang.Integer)
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @Timed(name = "get-image", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response getImage(final String name, final Integer id, ThumbnailType type) {
    User user = findUser(true);
    try {
      if (type != null) {
        return getImage(name, id, user, type);
      }
      return getImage(name, id, user);
    } catch (IOException | URISyntaxException e) {
      log.warn(null, e);
      throw new WebApplicationException(e.getMessage(), INTERNAL_SERVER_ERROR);
    }
  }

  private Response getImage(final String name, final Integer id, User user) throws URISyntaxException {
    Optional<Image> candidate = imageService.findImage(user, name, id);
    if (!candidate.isPresent() && user != null) {
      candidate = imageService.findImage(null, name, id);
    }
    if (!candidate.isPresent()) {
      throw notFound(MSG_IMAGE_IN_TRACK_MISSING, name, id);
    }
    Image image = candidate.get();
    EntityTag eTag = new EntityTagBuilder(httpServletRequest).buildEntityTag(image);
    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder != null) {
      return createResponse(builder);
    }
    Optional<ImageData> imageData = imageService.getImageData(image);
    if (!imageData.isPresent()) {
      if (image.getUrl() == null) {
        URI uri = pathTracks().path(name).path(PATH_IMAGES).path(String.valueOf(id))
            .queryParam(THUMBNAIL, ThumbnailType.LARGE.name()).build();
        return Response.status(MOVED_PERMANENTLY).location(uri).build();
      }
      return Response.status(MOVED_PERMANENTLY).location(new URI(image.getUrl())).build();
    }
    return createResponse(Response.status(OK), new BinaryStreamingOutput(imageData.get().getData()), eTag);
  }

  private Response getImage(final String name, final Integer id, User user, ThumbnailType type) throws IOException {
    Optional<Thumbnail> result = thumbnailService.findThumbnail(user, name, id, type);
    if (!result.isPresent() && user != null) {
      result = thumbnailService.findThumbnail(null, name, id, type);
    }
    if (!result.isPresent()) {
      CacheControl cc = new CacheControl();
      cc.setMaxAge(15);
      byte[] placeholder = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(IMG_PLACEHOLDER));
      return Response.status(OK).cacheControl(cc).entity(new BinaryStreamingOutput(placeholder)).build();
    }
    Thumbnail thumbnail = result.get();
    EntityTag eTag = new EntityTagBuilder(httpServletRequest).buildEntityTag(thumbnail);
    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder != null) {
      return createResponse(builder);
    }
    return createResponse(Response.status(OK), new BinaryStreamingOutput(thumbnail.getData()), eTag);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#insertImage(java.lang.String)
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @Timed(name = "insert-image", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response insertImage(final String name, final String filename, final byte[] data) {
    User user = findUser(false);
    Track track = trackService.findTrack(user, name, true);
    if (track == null) {
      throw notFound(MSG_TRACK_MISSING, name);
    }
    log.info("insert image {} for track [{}]", filename, track);
    int number = track.getImages().size();
    Image img = new Image();
    img.setName(filename);
    img.setTrack(track);
    img.setNumber(number);
    imageService.insert(img, data);
    URI location = UriBuilder.fromPath(track.getName() + SEP + PATH_IMAGES + img.getId()).build();
    return Response.status(CREATED).location(location).build();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#updateImage(java.lang.String,
   * java.lang.Integer)
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @Timed(name = "update-image", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response updateImage(final String name, final String filename, final Integer id, final byte[] data) {
    User user = findUser(false);
    Track track = trackService.findTrack(user, name, true);
    if (track == null) {
      throw notFound(MSG_TRACK_MISSING, name);
    }
    log.info("update image {} for track [{}]", filename, track);
    Image img = findImage(track, id).orElseThrow(() -> notFound(MSG_IMAGE_MISSING, id));
    EntityTag eTag = new EntityTagBuilder(httpServletRequest).buildEntityTag(img);
    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder != null) {
      return createResponse(builder);
    }
    img.setName(filename);
    img = imageService.update(img, data);
    eTag = new EntityTagBuilder(httpServletRequest).buildEntityTag(img);
    return createResponse(Response.status(NO_CONTENT), eTag);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#deleteImage(java.lang.String,
   * java.lang.Integer)
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @Timed(name = "delete-image", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response deleteImage(final String name, final Integer id) {
    User user = findUser(false);
    Image image = imageService.findImage(user, name, id)
        .orElseThrow(() -> notFound(MSG_IMAGE_IN_TRACK_MISSING, name, id));
    log.info("Delete image {} in track [{}]", id, name);
    imageService.delete(image);
    return Response.status(NO_CONTENT).build();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#getTrackData(java.lang.String,
   * java.lang.Integer)
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @Timed(name = "get-trackdata", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response getTrackData(final String name, final Integer id) {
    User user = findUser(true);
    Optional<TrackData> result = trackService.findTrackData(user, name, id);
    if (!result.isPresent() && user != null) {
      result = trackService.findTrackData(null, name, id);
    }
    if (!result.isPresent()) {
      throw notFound(MSG_TRACK_DATA_IN_TRACK_MISSING, name, id);
    }
    TrackData td = result.get();
    EntityTag eTag = new EntityTagBuilder(httpServletRequest).buildEntityTag(td);
    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder != null) {
      return createResponse(builder);
    }
    try {
      if (td.getData() == null) {
        return Response.status(SEE_OTHER).location(new URI(td.getUrl())).build();
      }
    } catch (URISyntaxException e) {
      log.warn(null, e);
      throw new WebApplicationException(e, INTERNAL_SERVER_ERROR);
    }
    StreamingOutput output = new BinaryStreamingOutput(td.getData());
    ResponseBuilder response = Response.status(OK).type(TrackDataInfo.getMediaType(td.getName()));
    return createResponse(response, output, eTag);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#insertTrackData(java.lang.String,
   * java.lang.String)
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @Timed(name = "insert-trackdata", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response insertTrackData(final String name, final String filename, final byte[] data) {
    User user = findUser(false);
    Track track = trackService.findTrack(user, name, false);
    if (track == null) {
      throw notFound(MSG_TRACK_MISSING, name);
    }
    log.info("insert track data {} for track [{}]", filename, track);
    TrackData td = new TrackData();
    WaypointSampler.updateTrackDataFields(td, filename, data);
    td.setTrack(track);
    trackService.insert(td);
    URI location = UriBuilder.fromPath(track.getName() + SEP + PATH_KML + td.getId()).build();
    return Response.status(CREATED).location(location).build();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#updateTrackData(java.lang.String,
   * java.lang.Integer)
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @Timed(name = "update-trackdata", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response updateTrackData(final String name, final String filename, final Integer id, byte[] data) {
    User user = findUser(false);
    Track track = trackService.findTrack(user, name, true);
    if (track == null) {
      throw notFound(MSG_TRACK_MISSING, name);
    }
    log.info("update track data {} for track [{}]", filename, track);
    TrackData td = findTrackData(track, id).orElseThrow(() -> notFound(MSG_TRACK_DATA_MISSING, id));
    EntityTag eTag = new EntityTagBuilder(httpServletRequest).buildEntityTag(td);
    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder != null) {
      return createResponse(builder);
    }
    WaypointSampler.updateTrackDataFields(td, filename, data);
    td.setTrack(track);
    td = trackService.update(td);

    track = trackService.findTrack(track.getId());
    resetGeolocationAvailable(track);

    eTag = new EntityTagBuilder(httpServletRequest).buildEntityTag(td);
    return createResponse(Response.status(NO_CONTENT), eTag);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#options(java.lang.String)
   */
  @Override
  public Response options(String path) {
    return Response.status(NO_CONTENT).header(ACCESS_CONTROL_ALLOW_ORIGIN, HEADER_CORS_ALL)
        .header(ACCESS_CONTROL_ALLOW_HEADERS, HEADER_CORS_ALLOWED_HEADERS).build();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.rest.HikingTracksService#deleteTrackData(java.lang.String,
   * java.lang.Integer)
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @Timed(name = "delete-trackdata", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response deleteTrackData(final String name, final Integer id) {
    User user = findUser(false);

    TrackData td = trackService.findTrackData(user, name, id)
        .orElseThrow(() -> notFound(MSG_TRACK_DATA_IN_TRACK_MISSING, name, id));
    log.info("Delete track data {} in track [{}]", id, name);
    trackService.delete(td);
    Track track = trackService.findTrack(user, name);
    resetGeolocationAvailable(track);

    return Response.status(NO_CONTENT).build();
  }

  private Optional<TrackData> findTrackData(Track track, Integer id) {
    if (id == null) {
      return Optional.empty();
    }
    for (TrackData td : track.getTrackData()) {
      if (td.getId().equals(id)) {
        return Optional.of(td);
      }
    }
    return Optional.empty();
  }

  private Optional<Image> findImage(Track track, Integer id) {
    if (id == null) {
      return Optional.empty();
    }
    for (Image i : track.getImages()) {
      if (i.getId().equals(id)) {
        return Optional.of(i);
      }
    }
    return Optional.empty();
  }

  private void resetGeolocationAvailable(Track track) {
    track.setGeolocationAvailable(null);
    trackService.update(track);
  }

  private User findUser(boolean allowPublic) {
    return sessionValidator.validateUser(allowPublic, httpServletRequest, httpServletResponse);
  }

  private UriBuilder pathTracks() {
    return ServletHelper.getBaseUrl(httpServletRequest).path(PATH_SERVICE).path(PATH_TRACKS);
  }

  /** Streaming output for bytes */
  private static final class BinaryStreamingOutput implements StreamingOutput {
    private BinaryStreamingOutput(byte[] data) {
      this.data = data;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ws.rs.core.StreamingOutput#write(java.io.OutputStream)
     */
    @Override
    public void write(OutputStream output) throws IOException {
      output.write(data);
    }

    private byte[] data;
  }

  private void validate(Representation r) {
    Set<ConstraintViolation<Representation>> violations = validator.validate(r);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
    }
  }
}
