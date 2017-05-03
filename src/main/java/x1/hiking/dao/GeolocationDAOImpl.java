package x1.hiking.dao;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.TypedQuery;

import x1.hiking.model.Geolocation;
import x1.hiking.model.Track;

/**
 * The Geolocation DAO implementation.
 *
 * @author joe
 */
@Stateless
@Local
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class GeolocationDAOImpl extends AbstractJpaDAO<Geolocation> implements GeolocationDAO {

  /**
   * Instantiates a new geolocation DAO.
   */
  public GeolocationDAOImpl() {
    super.setClazz(Geolocation.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.dao.GeolocationDAO#findGeolocation(x1.hiking.model.Track)
   */
  @Override
  public List<Geolocation> findGeolocation(final Track track) {
    TypedQuery<Geolocation> q = createNamedQuery("Geolocation.findByTrack");
    q.setParameter("track", track);
    return q.getResultList();
  }

}
