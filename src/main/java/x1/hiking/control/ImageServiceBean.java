package x1.hiking.control;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import x1.hiking.model.Image;
import x1.hiking.model.ImageData;
import x1.hiking.model.Track;
import x1.hiking.model.User;

/**
 * management of images and image data
 */
@Stateless
@Local
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ImageServiceBean implements ImageService {
  private static final String PARAM_ID = "id";
  private static final String PARAM_TRACK = "track";
  private static final String PARAM_NAME = "name";
  private static final String PARAM_USER = "user";
  private static final String PARAM_IMAGE = "image";
  
  @PersistenceContext
  private EntityManager em;

  /*
   * (non-Javadoc)
   * @see x1.hiking.service.ImageService#findImage(x1.hiking.model.User, java.lang.String, java.lang.Integer)
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Image findImage(User user, String name, Integer id) {
    TypedQuery<Image> q;
    if (user != null) {
      q = em.createNamedQuery("Image.findImageByUserAndNameAndId", Image.class);
      q.setParameter(PARAM_NAME, name);
      q.setParameter(PARAM_USER, user);
      q.setParameter(PARAM_ID, id);
    } else {
      q = em.createNamedQuery("Image.findPublicImageByNameAndId", Image.class);
      q.setParameter(PARAM_NAME, name);
      q.setParameter(PARAM_ID, id);
    }
    try {
      return q.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.service.ImageService#insert(x1.hiking.model.Image)
   */
  @Override
  public void insert(Image entity) {
    entity.setTrack(em.merge(entity.getTrack()));
    em.persist(entity);
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.service.ImageService#insert(x1.hiking.model.Image, byte[])
   */
  @Override
  public void insert(Image entity, byte[] data) {
    insert(entity);
    insertImageData(entity, data);
  }

  private ImageData insertImageData(Image entity, byte[] data) {
    if (data != null && data.length > 0) {
      ImageData imageData = new ImageData();
      imageData.setData(data);
      imageData.setImage(entity);
      em.persist(imageData);
      return imageData;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.service.ImageService#update(x1.hiking.model.Image)
   */
  @Override
  public Image update(Image entity) {
    return merge(entity);
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.service.ImageService#update(x1.hiking.model.Image, byte[])
   */
  @Override
  public Image update(Image entity, byte[] data) {
    ImageData imageData = getImageData(entity);
    if (imageData != null) {
      updateImageData(imageData, data);
    } else {
      insertImageData(entity, data);
    }
    return merge(entity);
  }

  private ImageData updateImageData(ImageData imageData, byte[] data) {
    if (data == null) {
      return null;
    } else {
      imageData.setData(data);
      return em.merge(imageData);
    }
  }

  private Image merge(Image entity) {
    Image image = em.merge(entity);
    if (image.getThumbnails() != null) {
      image.getThumbnails().clear();
    }
    return image;
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.service.ImageService#delete(x1.hiking.model.Image)
   */
  @Override
  public void delete(Image entity) {
    entity = merge(entity);
    em.remove(entity);
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.service.ImageService#getImageData(x1.hiking.model.Image)
   */
  @Override
  public ImageData getImageData(Image image) {
    if (image.getId() == null) {
      return null;
    }
    TypedQuery<ImageData> q = em.createNamedQuery("ImageData.getImage", ImageData.class);
    q.setParameter(PARAM_IMAGE, image);
    try {
      return q.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.service.ImageService#deleteImageData(x1.hiking.model.Image)
   */
  @Override
  public void deleteImageData(Image image) {
    if (image.getId() == null) {
      return;
    }
    Query q = em.createNamedQuery("ImageData.deleteImage");
    q.setParameter(PARAM_IMAGE, image);
    q.executeUpdate();
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.service.ImageService#findFirstImage(x1.hiking.model.Track)
   */
  @Override
  public Image findFirstImage(Track track) {
    TypedQuery<Image> q = em.createNamedQuery("Image.getImages", Image.class);
    q.setParameter(PARAM_TRACK, track);
    q.setMaxResults(1);
    List<Image> images = q.getResultList();
    if (images.isEmpty()) {
      return null;
    }
    return images.get(0);
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.service.ImageService#findImagesToUpdate()
   */
  @Override
  public List<Image> findImagesToUpdate() {
    TypedQuery<Image> q = em.createNamedQuery("Image.findMissingThumbnails", Image.class);
    return q.getResultList();
  }

}
