package x1.hiking.representation;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Email;

import x1.hiking.model.User;
import x1.hiking.service.GravatarHelper;

/**
 * @author joe
 * 
 */
@XmlRootElement(name = "user", namespace = Representation.NS_HIKING_TRACKS)
public class UserInfo implements Representation {
  private static final long serialVersionUID = 7428076816009079364L;

  /**
   * Default constructor
   */
  public UserInfo() {
  }

  /** 
   * Constructor with user 
   */
  public UserInfo(User user) {
    setName(user.getName());
    setEmail(user.getEmail());
    setPublished(BooleanUtils.isTrue(user.isPublished()));
  }

  /**
   * @return the name
   */
  @XmlElement(name = "name")
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the email
   */
  @XmlElement(name = "email", required = true)
  public String getEmail() {
    return email;
  }

  /**
   * @param email
   *          the email to set
   */
  public void setEmail(String email) {
    this.email = email;
    setLink(calculateLink(email));
  }

  /**
   * @return is published
   */
  @XmlElement(name = "published")
  public boolean isPublished() {
    return published;
  }
  
  /**
   * @param published is published
   */
  public void setPublished(boolean published) {
    this.published = published;
  }
  
  /**
   * @return the link
   */
  @XmlElement(name = "link")
  public Link getLink() {
    return link;
  }
  
  private void setLink(Link link) {
    this.link = link;
  }
  
  private Link calculateLink(String email) {
    if (StringUtils.isNotEmpty(email)) {
      Link l = new Link();
      l.setMimeType(MEDIA_TYPE_IMAGE_JPEG);
      l.setHref(GravatarHelper.getUrl(email));
      return l;
    } else {
      return null;
    }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "<userinfo " + getName() + "(" + getEmail() + ")>";
  }

  @Size(max = 100)
  private String name;
  @NotNull(message = "Email address may not be empty")
  @Size(max = 100)
  @Email
  private String email;
  private Link link;
  private boolean published;
}
