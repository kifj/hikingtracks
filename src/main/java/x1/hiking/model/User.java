package x1.hiking.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import org.apache.commons.lang3.StringUtils;

/**
 * User model class
 * 
 * @author joe
 *
 */
@Entity
@Table(name = "user_account", indexes = { 
  @Index(name = "idx_user_name", columnList = User.ATTR_NAME, unique = false),
  @Index(name = "idx_user_token", columnList = User.ATTR_TOKEN, unique = true),
  @Index(name = "idx_user_email", columnList = User.ATTR_EMAIL, unique = true) })
@NamedQueries({ 
  @NamedQuery(name = "User.findUserByEmail", query = "SELECT u FROM User u WHERE u.email = :email"),
  @NamedQuery(name = "User.findUserByToken", query = "SELECT u FROM User u WHERE u.token= :token") })
@Cacheable
public class User implements Model {
  private static final long serialVersionUID = 7944299851758333310L;
  public static final String ATTR_NAME = "name";
  public static final String ATTR_TOKEN = "token";
  public static final String ATTR_EMAIL = "email";
  public static final String ATTR_PUBLISHED = "published";
  public static final String ATTR_EXPIRES = "expires";
  
  /**
   * @return the name
   */
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
  public String getEmail() {
    return email;
  }

  /**
   * @param email
   *          the email to set
   */
  public void setEmail(String email) {
    this.email = email;
  }

  public List<Track> getTracks() {
    return tracks;
  }

  /**
   * @param tracks
   *          the tracks to set
   */
  public void setTracks(List<Track> tracks) {
    this.tracks = tracks;
  }

  /**
   * add track
   * 
   * @param element
   *          the track
   */
  public void addTrack(Track element) {
    if (this.tracks == null) {
      this.tracks = new ArrayList<>();
    }
    this.tracks.add(element);
  }

  /**
   * remove track
   * 
   * @param element
   *          the track
   */
  public boolean removeTrack(Track element) {
    if (this.tracks == null) {
      return false;
    }
    return this.tracks.remove(element);
  }

  /**
   * @return the token
   */
  public String getToken() {
    return token;
  }
  
  /**
   * set the token
   * 
   * @param token the token
   */
  public void setToken(String token) {
    this.token = token;
  }

  /**
   * @return token expiration date
   */
  public Date getExpires() {
    return expires;
  }

  /**
   * set token expiration date
   * @param expires the expiration date
   */
  public void setExpires(Date expires) {
    this.expires = expires;
  }
  
  /**
   * @return is published
   */
  public Boolean isPublished() {
    return published;
  }

  /**
   * @param published is published
   */
  public void setPublished(Boolean published) {
    this.published = published;    
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.model.Model#getId()
   */
  @Override
  public Integer getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.model.Model#setId(java.lang.Integer)
   */
  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.model.Model#getVersion()
   */
  @Override
  public Integer getVersion() {
    return version;
  }

  /*
   * (non-Javadoc)
   * 
   * @see x1.hiking.model.Model#setVersion(java.lang.Integer)
   */
  @Override
  public void setVersion(Integer version) {
    this.version = version;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "<user id=" + getId() + ": name=" + getName() + " email=" + getEmail() + " published=" + published + ">";
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof User)) {
      return false;
    }
    User other = (User) obj;
    return StringUtils.equals(name, other.getName()) && StringUtils.equals(email, other.getEmail());
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Column(name = User.ATTR_NAME, nullable = true, length = 100)
  private String name;
  @Column(name = User.ATTR_EMAIL, nullable = false, length = 100)
  private String email;
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = Track.ATTR_USER)
  private List<Track> tracks;
  @Column(name = User.ATTR_TOKEN, nullable = true, length = 255)
  private String token;
  @Column(name = ATTR_EXPIRES, nullable = true)
  @Temporal(TemporalType.TIMESTAMP)
  private Date expires;
  @Column(name = ATTR_PUBLISHED) 
  private Boolean published;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = ATTR_ID)
  private Integer id;
  @Version
  @Column(name = ATTR_VERSION)
  private Integer version;
}
