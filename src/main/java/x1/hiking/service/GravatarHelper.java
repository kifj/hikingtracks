package x1.hiking.service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for Gravatar.
 * 
 * @author joe
 */
public final class GravatarHelper {
  private static final Logger LOG = LoggerFactory.getLogger(GravatarHelper.class);

  private GravatarHelper() {
  }

  /**
   * byte to hex.
   *
   * @param array the array
   * @return the string
   */
  private static String hex(byte[] array) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < array.length; ++i) {
      sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
    }
    return sb.toString();
  }

  /**
   * String to MD5.
   *
   * @param message the message
   * @return the string
   */
  public static String md5Hex(String message) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      return hex(md.digest(message.getBytes("UTF-8")));
    } catch (NoSuchAlgorithmException e) {
      LOG.error(null, e);
    } catch (UnsupportedEncodingException e) {
      LOG.error(null, e);
    }
    return null;
  }

  
  /**
   * Gets the url to the Gravatar image.
   *
   * @param email the email
   * @return the url
   */
  public static String getUrl(String email) {
    String hash = md5Hex(email);
    return "https://www.gravatar.com/avatar/" + hash + "?s=40&d=mm";
  }
}
