package x1.hiking.thumbnails;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
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

import x1.hiking.control.ImageService;
import x1.hiking.model.Image;
import x1.hiking.model.ImageData;
import x1.hiking.model.Thumbnail;
import x1.hiking.model.ThumbnailType;
import x1.hiking.model.User;

/**
 * Service for generating thumbnails
 * 
 * @author joe
 * 
 */
@Stateless
public class ThumbnailServiceImpl implements ThumbnailService {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private static final int MAX_PIXEL = 2400;
  private static final String OUTPUT_FORMAT_JPEG = "jpeg";
  private static final String PLACEHOLDER_IMAGE = "question.png";
  private static final String PARAM_ID = "id";
  private static final String PARAM_USER = "user";
  private static final String PARAM_NAME = "name";
  private static final String PARAM_TYPE = "type";
  private static final String PARAM_IMAGE = "image";

  private final Map<ThumbnailType, ThumbnailSize> tumbnailSizeMap = new HashMap<>();

  @EJB
  private ImageService imageService;

  @PersistenceContext
  private EntityManager em;

  @PostConstruct
  private void setup() {
    tumbnailSizeMap.put(ThumbnailType.SMALL, new ThumbnailSize(150, 150, false, 0.8f));
    tumbnailSizeMap.put(ThumbnailType.MEDIUM, new ThumbnailSize(300, 200, true, 0.9f));
    tumbnailSizeMap.put(ThumbnailType.LARGE, new ThumbnailSize(640, 480, false, 0.95f));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * x1.hiking.thumbnails.ThumbnailService#createThumbnail(java.io.InputStream,
   * x1.hiking.thumbnails.ThumbnailService.ThumbnailSize)
   */
  @Override
  public void createThumbnail(InputStream in, OutputStream out, ThumbnailSize size) throws IOException {
    ImageInputStream iis = ImageIO.createImageInputStream(in);
    BufferedImage image = readInput(iis);
    if (image == null) {
      createThumbnail(getClass().getClassLoader().getResourceAsStream(PLACEHOLDER_IMAGE), out, size);
      return;
    }
    int width = image.getWidth();
    int height = image.getHeight();
    if (size.isKeepAspect()) {
      log.info("Resizing image from {}x{} to {}x{}", new Object[] { width, height, size.getWidth(), size.getHeight() });
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
      log.info("Resizing image from {}x{} to {}x{}", new Object[] { width, height, size.getWidth(), size.getHeight() });
      Region r = new Region(new Coordinate(x, y), new AbsoluteSize(w, h));
      Thumbnails.of(image).size(size.getWidth(), size.getHeight()).sourceRegion(r).outputQuality(size.getQuality())
          .outputFormat(OUTPUT_FORMAT_JPEG).toOutputStream(out);
    }
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

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.thumbnails.ThumbnailService#createThumbnail(byte[],
   * x1.hiking.thumbnails.ThumbnailService.ThumbnailSize)
   */
  @Override
  public byte[] createThumbnail(byte[] in, ThumbnailSize size) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(in);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    createThumbnail(bis, bos, size);
    return bos.toByteArray();
  }

  private byte[] createThumbnail(InputStream is, ThumbnailSize size) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    createThumbnail(is, bos, size);
    return bos.toByteArray();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * x1.hiking.thumbnails.ThumbnailService#createThumbnail(x1.hiking.model.Image ,
   * x1.hiking.model.ThumbnailType)
   */
  @Override
  public Thumbnail createThumbnail(Image image, ThumbnailType type) throws IOException {
    ImageData imageData = imageService.getImageData(image);
    if (imageData == null || imageData.getData() == null) {
      if (image.getUrl() != null) {
        return createThumbnailFromUrl(image, type);
      } else {
        log.warn("No original Image for " + image + ", will be deleted.");
        imageService.delete(image);
      }
      return null;
    }
    return createThumbnail(imageData, type);
  }

  private Thumbnail createThumbnailFromUrl(Image image, ThumbnailType type) throws IOException {
    HttpClient httpclient = HttpClientBuilder.create().build();
    HttpGet httpMethod = new HttpGet(image.getUrl());
    HttpResponse response = httpclient.execute(httpMethod);
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      InputStream in = entity.getContent();
      Thumbnail t = new Thumbnail();
      t.setImage(image);
      t.setType(type);
      t.setData(createThumbnail(in, tumbnailSizeMap.get(type)));
      in.close();
      return t;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.thumbnails.ThumbnailService#createThumbnail(x1.hiking.model.
   * ImageData , x1.hiking.model.ThumbnailType)
   */
  @Override
  public Thumbnail createThumbnail(ImageData imageData, ThumbnailType type) throws IOException {
    byte[] data = createThumbnail(imageData.getData(), tumbnailSizeMap.get(type));
    if (data == null || data.length == 0) {
      return null;
    }
    Thumbnail t = new Thumbnail();
    t.setType(type);
    t.setData(data);
    t.setImage(imageData.getImage());
    return t;
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.thumbnails.ThumbnailService#insert(x1.hiking.model.Thumbnail)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void insert(Thumbnail entity) {
    log.debug("insert thumbnail {}", entity);
    em.persist(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.thumbnails.ThumbnailService#delete(x1.hiking.model.Thumbnail)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void delete(Thumbnail entity) {
    log.debug("delete thumbnail {}", entity);
    entity = em.merge(entity);
    entity.getImage().getThumbnails().remove(entity);
    em.remove(entity);
    em.flush();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.thumbnails.ThumbnailService#update(x1.hiking.model.Thumbnail)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Thumbnail update(Thumbnail entity) {
    log.debug("update thumbnail {}", entity);
    return em.merge(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * x1.hiking.thumbnails.ThumbnailService#findThumbnails(x1.hiking.model.Image,
   * x1.hiking.model.ThumbnailType)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public List<Thumbnail> findThumbnails(final Image image, final ThumbnailType type) {
    TypedQuery<Thumbnail> q = em.createNamedQuery("Thumbnail.findThumbnailsByImageAndType", Thumbnail.class);
    q.setParameter(PARAM_IMAGE, image);
    q.setParameter(PARAM_TYPE, type);
    return q.getResultList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.thumbnails.ThumbnailService#findImagesToUpdate()
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public List<Image> findImagesToUpdate() {
    return imageService.findImagesToUpdate();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.thumbnails.ThumbnailService#findImage(x1.hiking.model.User,
   * java.lang.String, java.lang.Integer, x1.hiking.model.ThumbnailType)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Thumbnail findThumbnail(final User user, final String name, final Integer id, final ThumbnailType type) {
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
      return q.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

}
