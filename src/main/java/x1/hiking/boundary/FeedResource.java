package x1.hiking.boundary;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.Cache;
import org.jboss.resteasy.plugins.providers.atom.Content;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.atom.Link;
import org.jboss.resteasy.plugins.providers.atom.Text;
import org.jboss.resteasy.plugins.providers.atom.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.control.ImageService;
import x1.hiking.control.QueryOptions;
import x1.hiking.control.TrackService;
import x1.hiking.model.Coord;
import x1.hiking.model.Track;
import x1.hiking.representation.ImageInfo;
import x1.hiking.representation.TrackDataInfo;
import x1.hiking.representation.TrackInfo;
import x1.hiking.representation.TrackInfoList;
import x1.hiking.representation.UserInfo;
import x1.hiking.utils.ServletHelper;

/**
 * ATOM Feed service
 *
 * @author joe
 */
public class FeedResource implements FeedService {
  private static final long serialVersionUID = 8443300431399389281L;
  private final Logger log = LoggerFactory.getLogger(FeedResource.class);
  private static final String PATH_TRACKS = PATH_SERVICE + "/tracks/";
  private static final String PREFIX_TAG = "tag:";
  private static final String DETAIL_PAGE_PATH = "/pages/detail.html";
  private static final String SUFFIX_THUMBNAIL = "?thumbnail=MEDIUM";

  @Inject
  @Named("feed-cache")
  private Cache<String, Object> cache;

  @Inject
  @ConfigProperty(name = "feed.author")
  private String author;

  @Inject
  @ConfigProperty(name = "feed.title")
  private String title;

  @Inject
  @ConfigProperty(name = "feed.id")
  private String id;

  @Inject
  @ConfigProperty(name = "feed.url")
  private String baseUrl;

  @EJB
  private TrackService service;

  @EJB
  private ImageService imageService;

  @Context
  private HttpServletRequest httpServletRequest;

  @Inject
  private Marshaller marshaller;

  @Inject
  private Unmarshaller unmarshaller;

  /*
   * (non-Javadoc)
   *
   * @see x1.hiking.rest.FeedService#getTracks()
   */
  @Override
  public Feed getTracks() {
    try {
      log.info("get feed");
      Optional<Feed> cached = getFromCache(cache);
      if (cached.isPresent()) {
        return cached.get();
      }
      Feed feed = new Feed();
      TrackInfoList list = getTrackInfoList();
      String self = getRequestURL();
      setFeedProperties(feed, list, self);
      for (TrackInfo trackInfo : list.getTrackInfos()) {
        Entry entry = createEntryFromObject(trackInfo, self + SEP + trackInfo.getId());
        feed.getEntries().add(entry);
      }
      putToCache(cache, feed);
      return feed;
    } catch (URISyntaxException | JAXBException e) {
      log.warn(null, e);
      throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
    }
  }

  private void putToCache(Cache<String, Object> cache, Feed feed) throws JAXBException {
    StringWriter out = new StringWriter();
    marshaller.marshal(feed, out);
    cache.put(SEP, out.toString());
  }

  private Optional<Feed> getFromCache(Cache<String, Object> cache) throws JAXBException {
    String value = (String) cache.get(SEP);
    if (value != null) {
      StringReader in = new StringReader(value);
      return Optional.of((Feed) unmarshaller.unmarshal(in));
    }
    return Optional.empty();
  }

  /*
   * (non-Javadoc)
   *
   * @see x1.hiking.rest.FeedService#getTrack(java.lang.Integer)
   */
  @Override
  public Entry getTrack(Integer id) {
    try {
      log.info("get feed entry for " + id);
      Entry entry = (Entry) cache.get(SEP + id);
      if (entry != null) {
        return entry;
      }
      Track track = service.findTrack(id);
      if (track == null) {
        throw new NotFoundException();
      }
      String path = UriBuilder.fromPath(baseUrl + PATH_TRACKS).path("{0}").build(track.getName()).toString();
      TrackInfo trackInfo = new TrackInfo(track, false, false, path, null);
      imageService.findFirstImage(track)
          .ifPresent(image -> trackInfo.addImage(new ImageInfo(image, path + SEP + PATH_IMAGES)));
      entry = createEntryFromObject(trackInfo, getRequestURL());
      cache.put(SEP + id, entry);
      return entry;
    } catch (URISyntaxException e) {
      log.warn(null, e);
      throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
    }
  }

  private TrackInfoList getTrackInfoList() {
    QueryOptions options = new QueryOptions(0, 5, null);
    List<Track> tracks = service.findTracks(null, options);
    List<TrackInfo> trackInfos = new ArrayList<>();
    for (Track track : tracks) {
      String path = UriBuilder.fromPath(baseUrl + PATH_TRACKS).path("{0}").build(track.getName()).toString();
      TrackInfo trackInfo = new TrackInfo(track, false, false, path, null);
      imageService.findFirstImage(track)
          .ifPresent(image -> trackInfo.addImage(new ImageInfo(image, path + SEP + PATH_IMAGES)));
      trackInfos.add(trackInfo);
    }
    return new TrackInfoList(trackInfos);
  }

  private Entry createEntryFromObject(TrackInfo trackInfo, String url) throws URISyntaxException {
    Entry entry = new Entry();
    Person p = getAuthor(trackInfo);
    if (p != null) {
      entry.getAuthors().add(p);
    }
    entry.setTitle(trackInfo.getName());
    entry.setId(new URI(PREFIX_TAG + url));
    Date published = trackInfo.getPublishDate();
    if (published != null) {
      entry.setPublished(published);
    }
    Link link = new Link(null, UriBuilder.fromPath(baseUrl + DETAIL_PAGE_PATH).fragment(trackInfo.getName()).build());
    entry.getLinks().add(link);
    link = new Link("self", UriBuilder.fromPath(url).build());
    entry.getLinks().add(link);

    for (ImageInfo imageInfo : trackInfo.getImages()) {
      addLink(entry, imageInfo);
    }

    for (TrackDataInfo trackDataInfo : trackInfo.getTrackData()) {
      addLink(entry, trackDataInfo);
    }

    entry.setUpdated(trackInfo.getLastChange());
    entry.setSummaryElement(new Text(createSummary(trackInfo), "html"));
    Content content = new Content();
    content.setText(createSummary(trackInfo));
    content.setType(MediaType.TEXT_HTML_TYPE);
    entry.setContent(content);
    return entry;
  }

  private String createSummary(TrackInfo trackInfo) {
    StringBuilder buffer = new StringBuilder();
    boolean hasLocation = StringUtils.isNotEmpty(trackInfo.getLocation())
        || (trackInfo.getLatitude() != null && trackInfo.getLongitude() != null);
    if (!trackInfo.getImages().isEmpty()) {
      ImageInfo imageInfo = trackInfo.getImages().get(0);
      buffer.append("<img src=\"").append(imageInfo.getUrl()).append(SUFFIX_THUMBNAIL).append("\"/><br/>\n");
    }
    if (hasLocation) {
      buffer.append("<strong>Location:</strong> ");
      if (StringUtils.isNotEmpty(trackInfo.getLocation())) {
        buffer.append(trackInfo.getLocation()).append(" ");
      }
      if (trackInfo.getLatitude() != null && trackInfo.getLongitude() != null) {
        Coord latLon = new Coord(trackInfo.getLatitude(), trackInfo.getLongitude());
        buffer.append("<small>[").append(latLon.simpleLocationString()).append("]</small>");
      }
      buffer.append("<br/><br/>");
    }
    if (StringUtils.isNotEmpty(trackInfo.getDescription())) {
      buffer.append(trackInfo.getDescription());
      buffer.append("<br/>");
    }
    buffer.append("</p>\n");
    buffer.append("<div id=\"track\"></div>");
    return buffer.toString();
  }

  private void addLink(Entry entry, TrackDataInfo trackDataInfo) throws URISyntaxException {
    Link l = new Link();
    l.setHref(new URI(trackDataInfo.getUrl()));
    String mediaType = TrackDataInfo.getMediaType(trackDataInfo.getName());
    if (mediaType == null) {
      mediaType = MediaType.APPLICATION_XML;
    }
    l.setType(MediaType.valueOf(mediaType));
    l.setRel("related");
    entry.getLinks().add(l);
  }

  private void addLink(Entry entry, ImageInfo imageInfo) throws URISyntaxException {
    Link l = new Link();
    l.setHref(new URI(imageInfo.getUrl() + SUFFIX_THUMBNAIL));
    l.setType(MediaType.valueOf(MEDIA_TYPE_IMAGE_JPEG));
    l.setRel("related");
    entry.getLinks().add(l);
  }

  private Person getAuthor(TrackInfo trackInfo) {
    UserInfo userInfo = trackInfo.getUser();
    if (userInfo == null) {
      return null;
    }
    Person person = new Person();
    person.setEmail(userInfo.getEmail());
    if (StringUtils.isNotEmpty(userInfo.getName())) {
      person.setName(userInfo.getName());
    }
    return person;
  }

  private void setFeedProperties(Feed feed, TrackInfoList list, String self) throws URISyntaxException {
    feed.getAuthors().add(new Person(author));
    feed.setTitle(title);
    feed.setId(new URI(id));
    Date lastChangeDate = lastChangeDate(list).orElse(new Date());
    feed.setUpdated(lastChangeDate);

    if (StringUtils.isNotEmpty(baseUrl)) {
      feed.getLinks().add(new Link(null, baseUrl));
    }
    feed.getLinks().add(new Link("self", self));
  }

  private Optional<Date> lastChangeDate(TrackInfoList list) {
    Optional<Date> lastChangeDate = Optional.empty();
    for (TrackInfo trackInfo : list.getTrackInfos()) {
      if (lastChangeDate.isEmpty()) {
        lastChangeDate = Optional.of(trackInfo.getLastChange());
      } else {
        lastChangeDate = (lastChangeDate.get().compareTo(trackInfo.getLastChange()) > 0)
            ? lastChangeDate : Optional.ofNullable(trackInfo.getLastChange());
      }
    }
    return lastChangeDate;
  }

  private String getRequestURL() {
    return ServletHelper.getRequestUrl(httpServletRequest).build().toString();
  }

}
