package x1.hiking.utils;

import java.lang.annotation.Annotation;

import javax.cache.annotation.CacheInvocationParameter;
import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.CacheKeyInvocationContext;
import javax.cache.annotation.GeneratedCacheKey;

import x1.hiking.model.User;

/**
 * Cache key generator for User object
 *
 * @author joe
 */
public class UserCacheKeyGenerator implements CacheKeyGenerator {
  /*
   * (non-Javadoc)
   * @see javax.cache.annotation.CacheKeyGenerator#generateCacheKey(javax.cache.annotation.CacheKeyInvocationContext)
   */
  @Override
  public GeneratedCacheKey generateCacheKey(CacheKeyInvocationContext<? extends Annotation> cacheKeyInvocationContext) {
    CacheInvocationParameter[] keyParameters = cacheKeyInvocationContext.getKeyParameters();

    String email = null;
    for (int i = 0; i < keyParameters.length; i++) {
      Object value = keyParameters[i].getValue();
      if (value instanceof User) {
        email = ((User) value).getEmail();
        return new UserCacheKey(email);
      }
      if (value instanceof String) {
        return new UserCacheKey((String) value);
      }
    }
    return new UserCacheKey();
  }

  /**
   * Cache key for User
   * 
   * @author joe
   *
   */
  private static final class UserCacheKey implements GeneratedCacheKey {
    private static final long serialVersionUID = 403916448060586269L;

    private UserCacheKey() {
    }

    private UserCacheKey(String email) {
      this.email = email;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      return email.equals(((UserCacheKey) obj).email);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      if (email == null) {
        return 0;
      }
      return email.hashCode();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "<UserCacheKey email=" + email + ">";
    }

    private String email;
  }
}
