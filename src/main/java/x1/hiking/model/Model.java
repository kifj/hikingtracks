package x1.hiking.model;

import java.io.Serializable;

/** Base interface for domain model objects
 * 
 * @author joe
 *
 */
public interface Model extends Serializable {
  /**
   * @return the id
   */
  Integer getId();

  /**
   * @param id
   *          the id to set
   */
  void setId(Integer id);

  /**
   * @return the version
   */
  Integer getVersion();
  
  /**
   * @param version
   *          the version to set
   */
  void setVersion(Integer version);
}
