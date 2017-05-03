package x1.hiking.dao;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import x1.hiking.model.Image;
import x1.hiking.model.Thumbnail;
import x1.hiking.model.ThumbnailType;
import x1.hiking.model.User;

/**
 * The Class ThumbnailDAO.
 * 
 * @author joe
 */
@Stateless
@Local
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ThumbnailDAOImpl extends AbstractJpaDAO<Thumbnail> implements ThumbnailDAO {

  private static final String PARAM_ID = "id";
  private static final String PARAM_USER = "user";
  private static final String PARAM_NAME = "name";
  private static final String PARAM_TYPE = "type";
  private static final String PARAM_IMAGE = "image";

  /**
   * Instantiates a new thumbnail dao.
   */
  public ThumbnailDAOImpl() {
    setClazz(Thumbnail.class);
  }

  /*
   * (non-Javadoc)
   * @see x1.hiking.dao.AbstractJpaDAO#remove(java.io.Serializable)
   */
  @Override
  public void remove(Thumbnail entity) {
    entity = merge(entity);
    super.remove(entity);
    flush(entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.ThumbnailDAO#findThumbnails(x1.hiking.model.Image,
   * x1.hiking.model.ThumbnailType)
   */
  @Override
  public List<Thumbnail> findThumbnails(final Image image, final ThumbnailType type) {
    TypedQuery<Thumbnail> q = createNamedQuery("Thumbnail.findThumbnailsByImageAndType");
    q.setParameter(PARAM_IMAGE, image);
    q.setParameter(PARAM_TYPE, type);
    return q.getResultList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.ThumbnailDAO#findThumbnail(x1.hiking.model.User,
   * java.lang.String, java.lang.Integer, x1.hiking.model.ThumbnailType)
   */
  @Override
  public Thumbnail findThumbnail(final User user, final String name, final Integer id, final ThumbnailType type) {
    TypedQuery<Thumbnail> q;
    if (user != null) {
      q = createNamedQuery("Thumbnail.findThumbnailByUserAndNameAndId");
      q.setParameter(PARAM_NAME, name);
      q.setParameter(PARAM_USER, user);
      q.setParameter(PARAM_ID, id);
      q.setParameter(PARAM_TYPE, type);
    } else {
      q = createNamedQuery("Thumbnail.findPublicThumbnailByNameAndId");
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
