package x1.hiking.control;

import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import x1.hiking.model.Image;
import x1.hiking.model.ImageData;
import x1.hiking.model.Track;
import x1.hiking.model.User;

/**
 * management of images and image data
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ImageService {
  private static final String PARAM_ID = "id";
  private static final String PARAM_TRACK = "track";
  private static final String PARAM_NAME = "name";
  private static final String PARAM_USER = "user";
  private static final String PARAM_IMAGE = "image";
  
  @PersistenceContext
  private EntityManager em;

  /**
   * Find image.
   *
   * @param user the user
   * @param name the name
   * @param id the id
   * @return the image
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Optional<Image> findImage(User user, String name, Integer id) {
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
      return Optional.of(q.getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  /**
   * Insert.
   *
   * @param entity the entity
   */
  public void insert(@Valid @NotNull Image entity) {
    entity.setTrack(em.merge(entity.getTrack()));
    em.persist(entity);
  }

  /**
   * Insert.
   *
   * @param entity the entity
   * @param data the data
   */
  public void insert(@Valid @NotNull Image entity, byte[] data) {
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

  /**
   * Update.
   *
   * @param entity the entity
   * @return the image
   */
  public Image update(@Valid @NotNull Image entity) {
    return merge(entity);
  }

  /**
   * Update.
   *
   * @param entity the entity
   * @param data the data
   * @return the image
   */
  public Image update(@Valid @NotNull Image entity, byte[] data) {
    Optional<ImageData> imageData = getImageData(entity);
    if (imageData.isPresent()) {
      updateImageData(imageData.get(), data);
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

  /**
   * Delete.
   *
   * @param entity the entity
   */
  public void delete(@NotNull Image entity) {
    entity = merge(entity);
    em.remove(entity);
  }

  /**
   * get Image data
   *
   * @param image the image
   * @return the image data
   */
  public Optional<ImageData> getImageData(@NotNull Image image) {
    if (image.getId() == null) {
      return Optional.empty();
    }
    TypedQuery<ImageData> q = em.createNamedQuery("ImageData.getImage", ImageData.class);
    q.setParameter(PARAM_IMAGE, image);
    try {
      return Optional.of(q.getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  /**
   * delete Image data
   *
   * @param image the image
   */
  public void deleteImageData(@NotNull Image image) {
    if (image.getId() == null) {
      return;
    }
    Query q = em.createNamedQuery("ImageData.deleteImage");
    q.setParameter(PARAM_IMAGE, image);
    q.executeUpdate();
  }

  /**
   * Find first image.
   *
   * @param track the track
   * @return the image
   */
  public Optional<Image> findFirstImage(@NotNull Track track) {
    TypedQuery<Image> q = em.createNamedQuery("Image.getImages", Image.class);
    q.setParameter(PARAM_TRACK, track);
    q.setMaxResults(1);
    List<Image> images = q.getResultList();
    if (images.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(images.get(0));
  }

  /**
   * find Images to update.
   *
   * @return the list of images
   */
  public List<Image> findImagesToUpdate() {
    TypedQuery<Image> q = em.createNamedQuery("Image.findMissingThumbnails", Image.class);
    return q.getResultList();
  }

}
