package x1.hiking.representation;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Simple Link representation
 * 
 * @author joe
 * 
 */
@JsonInclude(Include.NON_NULL)
@XmlRootElement(name = "link", namespace = "http://www.w3.org/2005/Atom")
public class Link {

  /**
   * @return the href
   */
  @XmlAttribute(name = "href")
  public String getHref() {
    return href;
  }

  /**
   * @param href
   *          the href to set
   */
  public void setHref(String href) {
    this.href = href;
  }

  /**
   * @return the rel
   */
  @XmlAttribute(name = "rel")
  public String getRel() {
    return rel;
  }

  /**
   * @param rel
   *          the rel to set
   */
  public void setRel(String rel) {
    this.rel = rel;
  }

  /**
   * @return the title
   */
  @XmlAttribute(name = "title")
  public String getTitle() {
    return title;
  }

  /**
   * @param title
   *          the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the mimeType
   */
  @XmlAttribute(name = "type")
  public String getMimeType() {
    return mimeType;
  }

  /**
   * @param mimeType
   *          the mimeType to set
   */
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  /**
   * @return the text
   */
  @XmlValue
  public String getText() {
    return text;
  }

  /**
   * @param text
   *          the text to set
   */
  @JsonSetter("value")
  public void setText(String text) {
    this.text = text;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "<link ref=" + getHref() + ", rel=" + getRel() + ">";
  }

  private String href;
  private String rel;
  private String title;
  private String mimeType;
  private String text;
}
