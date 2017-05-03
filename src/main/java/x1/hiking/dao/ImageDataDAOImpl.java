package x1.hiking.dao;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import x1.hiking.model.Image;
import x1.hiking.model.ImageData;

/**
 * The Class ImageDataDAO
 * 
 * @author joe
 */
@Stateless
@Local
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ImageDataDAOImpl extends AbstractJpaDAO<ImageData> implements ImageDataDAO {
  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.ImageDataDAO#getImageData(x1.hiking.model.Image)
   */
  @Override
  public ImageData getImageData(Image image) {
    if (image.getId() == null) {
      return null;
    }
    TypedQuery<ImageData> q = createNamedQuery("ImageData.getImage");
    q.setParameter("image", image);
    try {
      return q.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }
  
  /*
   * (non-Javadoc)
   * @see x1.hiking.dao.ImageDataDAO#removeImageData(x1.hiking.model.Image)
   */
  @Override
  public void removeImageData(Image image) {
    if (image.getId() == null) {
      return;
    }
    Query q = getEntityManager().createNamedQuery("ImageData.deleteImage");
    q.setParameter("image", image);
    q.executeUpdate();
  }
}
