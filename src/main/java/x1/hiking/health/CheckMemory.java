package x1.hiking.health;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import javax.enterprise.context.ApplicationScoped;
import java.lang.management.MemoryMXBean;
import java.lang.management.ManagementFactory;

/**
 * Memory Health Check
 */
@ApplicationScoped
@Health
public class CheckMemory implements HealthCheck {
  private static final String HEALTH_CHECK_NAME = "heap-memory";

  /*
   * (non-Javadoc)
   * @see org.eclipse.microprofile.health.HealthCheck#call()
   */
  @Override
  public HealthCheckResponse call() {
    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    long memUsed = memoryBean.getHeapMemoryUsage().getUsed();
    long memMax = memoryBean.getHeapMemoryUsage().getMax();

    HealthCheckResponseBuilder builder = HealthCheckResponse.named(HEALTH_CHECK_NAME).withData("used", memUsed)
        .withData("max", memMax);
    // status is is down is used memory is greater than 90% of max memory.
    builder = (memUsed < memMax * 0.9) ? builder.up() : builder.down();
    return builder.build();
  }
}
