package x1.hiking.health;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

/**
 * Database Health Check
 */
@ApplicationScoped
@Health
public class DatabaseCheck implements HealthCheck {
  private static final String HEALTH_CHECK_NAME = "hikingtracks-database";
  
  @PersistenceContext
  private EntityManager em;

  /*
   * (non-Javadoc)
   * @see org.eclipse.microprofile.health.HealthCheck#call()
   */
  @Override
  public HealthCheckResponse call() {
    try {
      Long count = em.createNamedQuery("Track.countTracks", Long.class).getSingleResult();
      return HealthCheckResponse.named(HEALTH_CHECK_NAME).withData("track", count).up().build();
    } catch (Exception e) {
      return HealthCheckResponse.named(HEALTH_CHECK_NAME).withData("error", e.getMessage()).down().build();
    }
  }
}
