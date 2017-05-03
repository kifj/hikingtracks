/**
 * $Id: $
 */
package x1.hiking.dao;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import x1.hiking.model.Image;
import x1.hiking.model.Track;
import x1.hiking.model.User;

/**
 * The Class ImageDAO.
 *
 * @author joe
 */
@Stateless
@Local
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ImageDAOImpl extends AbstractJpaDAO<Image> implements ImageDAO {

  /**
   * Instantiates a new image dao.
   */
  public ImageDAOImpl() {
    super.setClazz(Image.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.ImageDAO#findImage(x1.hiking.model.User,
   * java.lang.String, java.lang.Integer)
   */
  @Override
  public Image findImage(final User user, final String name, final Integer id) {
    TypedQuery<Image> q;
    if (user != null) {
      q = createNamedQuery("Image.findImageByUserAndNameAndId");
      q.setParameter("name", name);
      q.setParameter("user", user);
      q.setParameter("id", id);
    } else {
      q = createNamedQuery("Image.findPublicImageByNameAndId");
      q.setParameter("name", name);
      q.setParameter("id", id);
    }
    try {
      return q.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.AbstractJpaDAO#persist(java.io.Serializable)
   */
  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.ImageDAO#persist(x1.hiking.model.Image)
   */
  @Override
  public void persist(Image entity) {
    entity.setTrack(getEntityManager().merge(entity.getTrack()));
    super.persist(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.AbstractJpaDAO#merge(java.io.Serializable)
   */
  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.ImageDAO#merge(x1.hiking.model.Image)
   */
  @Override
  public Image merge(Image entity) {
    Image image = super.merge(entity);
    if (image.getThumbnails() != null) {
      image.getThumbnails().clear();
    }
    return image;
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.service.AbstractJpaDAO#remove(java.io.Serializable)
   */
  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.ImageDAO#remove(x1.hiking.model.Image)
   */
  @Override
  public void remove(Image entity) {
    entity = merge(entity);
    super.remove(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.ImageDAO#findImagesToUpdate()
   */
  @Override
  public List<Image> findImagesToUpdate() {
    TypedQuery<Image> q = createNamedQuery("Image.findMissingThumbnails");
    return q.getResultList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.ImageDAO#find(x1.hiking.model.Track)
   */
  @Override
  public List<Image> find(Track track) {
    TypedQuery<Image> q = createNamedQuery("Image.getImages");
    q.setParameter("track", track);
    return q.getResultList();
  }
}
