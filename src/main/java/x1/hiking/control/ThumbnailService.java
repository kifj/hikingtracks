package x1.hiking.control;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.AbsoluteSize;
import net.coobird.thumbnailator.geometry.Coordinate;
import net.coobird.thumbnailator.geometry.Region;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.model.Image;
import x1.hiking.model.ImageData;
import x1.hiking.model.Thumbnail;
import x1.hiking.model.ThumbnailSize;
import x1.hiking.model.ThumbnailType;
import x1.hiking.model.User;

/**
 * Service for generating thumbnails
 */
@Stateless
public class ThumbnailService {
  private final Logger log = LoggerFactory.getLogger(ThumbnailService.class);
  private static final int MAX_PIXEL = 2400;
  private static final String OUTPUT_FORMAT_JPEG = "jpeg";
  private static final String PLACEHOLDER_IMAGE = "question.png";
  private static final String PARAM_ID = "id";
  private static final String PARAM_USER = "user";
  private static final String PARAM_NAME = "name";
  private static final String PARAM_TYPE = "type";
  private static final String PARAM_IMAGE = "image";

  @EJB
  private ImageService imageService;

  @PersistenceContext
  private EntityManager em;

  /**
   * Creates the thumbnail.
   *
   * @param in
   *          the in
   * @param out
   *          the out
   * @param size
   *          the size
   * @return the image
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public BufferedImage createThumbnail(InputStream in, OutputStream out, ThumbnailSize size) throws IOException {
    ImageInputStream iis = ImageIO.createImageInputStream(in);
    BufferedImage image = readInput(iis);
    if (image == null) {
      createThumbnail(getClass().getClassLoader().getResourceAsStream(PLACEHOLDER_IMAGE), out, size);
      return null;
    }
    int width = image.getWidth();
    int height = image.getHeight();
    if (size.isKeepAspect()) {
      log.info("Resizing image from {}x{} to {}x{}", width, height, size.getWidth(), size.getHeight());
      Thumbnails.of(image).size(size.getWidth(), size.getHeight()).keepAspectRatio(true)
          .outputQuality(size.getQuality()).outputFormat(OUTPUT_FORMAT_JPEG).toOutputStream(out);
    } else {
      int x = 0;
      int y = 0;
      int w = width;
      int h = height;
      float targetRatio = (float) size.getWidth() / (float) size.getHeight();
      float sourceRatio = (float) width / (float) height;
      if (sourceRatio > targetRatio) {
        w = (int) (h * targetRatio);
        x = (image.getWidth() - w) / 2;
      } else {
        h = (int) (w * targetRatio);
        y = (image.getHeight() - h) / 2;
      }
      log.info("Resizing image from {}x{} to {}x{}", width, height, size.getWidth(), size.getHeight());
      Region r = new Region(new Coordinate(x, y), new AbsoluteSize(w, h));
      Thumbnails.of(image).size(size.getWidth(), size.getHeight()).sourceRegion(r).outputQuality(size.getQuality())
          .outputFormat(OUTPUT_FORMAT_JPEG).toOutputStream(out);
    }
    return image;
  }

  private BufferedImage readInput(ImageInputStream in) throws IOException {
    Iterator<ImageReader> it = ImageIO.getImageReaders(in);
    if (!it.hasNext()) {
      return null;
    }
    ImageReader reader = it.next();
    reader.setInput(in);
    int width = reader.getWidth(0);
    int height = reader.getHeight(0);
    if (width * height > MAX_PIXEL * MAX_PIXEL) {
      log.info("Image too large: {}x{}", width, height);
      if (reader.hasThumbnails(0)) {
        return reader.readThumbnail(0, 0);
      }
      Rectangle r = cropToMaxBounds(reader);
      ImageReadParam p = reader.getDefaultReadParam();
      p.setSourceRegion(r);
      return reader.read(0, p);
    }
    return reader.read(0);
  }

  private Rectangle cropToMaxBounds(ImageReader reader) throws IOException {
    int width = reader.getWidth(0);
    int height = reader.getHeight(0);
    if (width > height) {
      int w = MAX_PIXEL;
      int h = (int) (w / reader.getAspectRatio(0));
      int x = (width - w) / 2;
      int y = (height - h) / 2;
      return new Rectangle(x, y, w, h);
    } else {
      int h = MAX_PIXEL;
      int w = (int) (h * reader.getAspectRatio(0));
      int x = (width - w) / 2;
      int y = (height - h) / 2;
      return new Rectangle(x, y, w, h);
    }
  }

  /**
   * Creates the thumbnail.
   *
   * @param in
   *          the input
   * @param size
   *          the size
   * @return the byte[]
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public byte[] createThumbnail(byte[] in, ThumbnailSize size) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(in);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    createThumbnail(bis, bos, size);
    return bos.toByteArray();
  }

  private byte[] createThumbnail(InputStream is, ThumbnailSize size) throws IOException {
    if (size == null) {
      return new byte[0];
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    createThumbnail(is, bos, size);
    return bos.toByteArray();
  }

  /**
   * Creates the thumbnail.
   *
   * @param image
   *          the image
   * @param type
   *          the type
   * @return the thumbnail
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public Optional<Thumbnail> createThumbnail(Image image, ThumbnailType type) throws IOException {
    Optional<ImageData> imageData = imageService.getImageData(image);
    if (!imageData.isPresent() || imageData.get().getData() == null) {
      if (image.getUrl() != null) {
        return createThumbnailFromUrl(image, type);
      } else {
        log.warn("No original Image for " + image + ", will be deleted.");
        imageService.delete(image);
      }
      return Optional.empty();
    }
    return createThumbnail(imageData.get(), type);
  }

  private Optional<Thumbnail> createThumbnailFromUrl(Image image, ThumbnailType type) throws IOException {
    HttpClient httpclient = HttpClientBuilder.create().build();
    HttpGet httpMethod = new HttpGet(image.getUrl());
    HttpResponse response = httpclient.execute(httpMethod);
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      try (InputStream in = entity.getContent()) {
        Thumbnail t = new Thumbnail();
        t.setImage(image);
        t.setType(type);
        t.setData(createThumbnail(in, type.getThumbnailSize()));
        return Optional.of(t);
      }
    }
    return Optional.empty();
  }

  /**
   * Creates the thumbnail.
   *
   * @param imageData
   *          the image
   * @param type
   *          the type
   * @return the thumbnail
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public Optional<Thumbnail> createThumbnail(ImageData imageData, ThumbnailType type) throws IOException {
    byte[] data = createThumbnail(imageData.getData(), type.getThumbnailSize());
    if (data == null || data.length == 0) {
      return Optional.empty();
    }
    Thumbnail t = new Thumbnail();
    t.setType(type);
    t.setData(data);
    t.setImage(imageData.getImage());
    return Optional.of(t);
  }

  /**
   * Insert.
   *
   * @param entity
   *          the thumbnail
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void insert(Thumbnail entity) {
    log.debug("insert thumbnail {}", entity);
    em.persist(entity);
  }

  /**
   * Delete.
   *
   * @param entity
   *          the thumbnail
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void delete(Thumbnail entity) {
    log.debug("delete thumbnail {}", entity);
    entity = em.merge(entity);
    entity.getImage().getThumbnails().remove(entity);
    em.remove(entity);
    em.flush();
  }

  /**
   * Update.
   *
   * @param entity
   *          the thumbnail
   * @return the thumbnail
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Thumbnail update(Thumbnail entity) {
    log.debug("update thumbnail {}", entity);
    return em.merge(entity);
  }

  /**
   * Find thumbnails.
   *
   * @param image
   *          the image
   * @param type
   *          the type
   * @return the list
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public List<Thumbnail> findThumbnails(final Image image, final ThumbnailType type) {
    TypedQuery<Thumbnail> q = em.createNamedQuery("Thumbnail.findThumbnailsByImageAndType", Thumbnail.class);
    q.setParameter(PARAM_IMAGE, image);
    q.setParameter(PARAM_TYPE, type);
    return q.getResultList();
  }

  /**
   * Find images to update.
   *
   * @return the list
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public List<Image> findImagesToUpdate() {
    return imageService.findImagesToUpdate();
  }

  /**
   * Find thumbnail.
   *
   * @param user
   *          the user
   * @param name
   *          the name
   * @param id
   *          the id
   * @param type
   *          the type
   * @return the thumbnail
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Optional<Thumbnail> findThumbnail(final User user, final String name, final Integer id,
      final ThumbnailType type) {
    TypedQuery<Thumbnail> q;
    if (user != null) {
      q = em.createNamedQuery("Thumbnail.findThumbnailByUserAndNameAndId", Thumbnail.class);
      q.setParameter(PARAM_NAME, name);
      q.setParameter(PARAM_USER, user);
      q.setParameter(PARAM_ID, id);
      q.setParameter(PARAM_TYPE, type);
    } else {
      q = em.createNamedQuery("Thumbnail.findPublicThumbnailByNameAndId", Thumbnail.class);
      q.setParameter(PARAM_NAME, name);
      q.setParameter(PARAM_ID, id);
      q.setParameter(PARAM_TYPE, type);
    }
    try {
      return Optional.of(q.getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }
}
