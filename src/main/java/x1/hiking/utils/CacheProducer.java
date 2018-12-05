package x1.hiking.utils;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;

/**
 * CDI producer for Infinispan
 * 
 * @author joe
 *
 */
@ApplicationScoped
public class CacheProducer {

  @Resource(name = "java:comp/env/cache/hikingtracks")
  private EmbeddedCacheManager cacheManager;

  @Produces
  @Named("feed-cache")
  private Cache<String, Object> getFeedCache() {
    return cacheManager.getCache("feed-cache");
  }

  @Produces
  @ApplicationScoped
  public EmbeddedCacheManager getCacheManager() {
    return cacheManager;
  }
}
