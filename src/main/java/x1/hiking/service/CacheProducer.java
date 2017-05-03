package x1.hiking.service;

import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;

/** CDI producer for Infinispan
 * 
 * @author joe
 *
 */
public class CacheProducer {

  @Produces
  @Resource(lookup = "java:jboss/infinispan/container/hikingtracks")
  private EmbeddedCacheManager defaultCacheManager;
  
  @Produces
  @Named("feed-cache")
  private Cache<String, Object> getFeedCache() {
    return defaultCacheManager.getCache("feed-cache");
  }
}
