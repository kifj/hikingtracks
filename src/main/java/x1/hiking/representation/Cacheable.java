package x1.hiking.representation;

/**
 * ETag support
 * 
 * @author joe
 * 
 */
public interface Cacheable {
  /**
   * Compute Entity Tag part
   * 
   * @param buffer
   *          the buffer
   */
  void computeEntityTag(StringBuilder buffer);
}
