package x1.hiking.representation;

import static x1.hiking.representation.ErrorMessageBuilder.badRequest;

import org.apache.commons.lang3.StringUtils;

/**
 * Representation of data based on a file
 */
public interface FilenameInfo {
  /**
   * @return the name
   */
  String getName();

  /**
   * @return the URL
   */
  String getUrl();

  /**
   * @return the ID
   */
  Integer getId();

  /**
   * set name
   * 
   * @param name
   *          the name
   */
  void setName(String name);

  /**
   * set URL
   * 
   * @param url
   *          the URL
   */
  void setUrl(String url);

  /**
   * @return true if it passed consistency check
   */
  default boolean validateFilename() {
    if (getId() != null && StringUtils.isNotEmpty(getUrl())) {
      return true;
    }
    if (StringUtils.isEmpty(getUrl())) {
      if (getId() == null && StringUtils.isEmpty(getName())) {
        return false;
      }
      throw badRequest("x1.hiking.representation.FilenameInfo.url_missing.message", getName());
    }
    if (StringUtils.isEmpty(getName()) && StringUtils.isNotEmpty(getUrl())) {
      int idx = getUrl().lastIndexOf('/');
      setName(getUrl().substring(idx + 1));
    }
    return true;
  }
}
