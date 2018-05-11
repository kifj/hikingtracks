package x1.hiking.utils;

import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.infinispan.Cache;

/** CDI producer for Infinispan
 * 
 * @author joe
 *
 */
public class CacheProducer {
  
  @Resource(name = "java:comp/env/cache/feed-cache")  
  private Cache<String, Object> feedCache;  
  
  @Produces
  @Named("feed-cache")
  private Cache<String, Object> getFeedCache() {
    return feedCache; 
  }
}
