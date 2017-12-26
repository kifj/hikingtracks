package x1.hiking.utils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;

/**
 * Servlet helper class.
 * 
 * @author joe
 */
public final class ServletHelper implements AuthorizationConstants {

  private ServletHelper() {
  }

  /**
   * Gets the base url.
   * 
   * @param request
   *          the request
   * @return the base url
   */
  public static String getBaseUrl(HttpServletRequest request) {
    UriBuilder b = getRequestUrl(request);
    ServletContext ctx = request.getServletContext();
    b.replacePath(ctx.getContextPath());
    String baseUrl = b.build().toString();
    if (!baseUrl.endsWith("/")) {
      baseUrl += "/";
    }
    return baseUrl;
  }

  /**
   * get the request url including proxy server
   * 
   * @param request
   *          the request
   * @return the request url
   */
  public static UriBuilder getRequestUrl(HttpServletRequest request) {
    UriBuilder url = UriBuilder.fromUri(request.getRequestURL().toString());
    String forwardedHost = request.getHeader("X-Forwarded-Host");
    String forwardedPort = request.getHeader("X-Forwarded-Port");
    String forwardedProto = request.getHeader("X-Forwarded-Proto");
    if (StringUtils.isNotEmpty(forwardedHost)) {
      url.host(forwardedHost);
    }
    if (StringUtils.isNotEmpty(forwardedPort)) {
      url.port(Integer.parseInt(forwardedPort));
    } else if (StringUtils.isNotEmpty(forwardedHost)) {
      url.port(-1);
    }
    if (StringUtils.isNotEmpty(forwardedProto)) {
      url.scheme(forwardedProto);
    }
    return url;
  }

  /**
   * reset the session cookie
   * 
   * @param response
   *          the response
   */
  public static void revokeSessionCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie(PARAM_AUTH_TOKEN, "");
    cookie.setMaxAge(0);
    cookie.setPath("/");
    cookie.setSecure(false);
    response.addCookie(cookie);
  }

  /**
   * inject session token
   * 
   * @param response
   *          the response
   * @param token
   *          the token
   */
  public static void injectSessionCookie(HttpServletResponse response, String token) {
    try {
      Cookie cookie = new Cookie(PARAM_AUTH_TOKEN, URLEncoder.encode(token, UTF_8));
      cookie.setPath("/");
      cookie.setSecure(false);
      response.addCookie(cookie);
    } catch (UnsupportedEncodingException e) {
      // ignore
    }
  }

  /**
   * retrieve session token
   * 
   * @param request
   *          the request
   * @param name
   *          the name
   */
  public static String getSessionCookieValue(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(name)) {
          try {
            return URLDecoder.decode(cookie.getValue(), UTF_8);
          } catch (UnsupportedEncodingException e) {
            // ignore
          }
        }
      }
    }
    return null;
  }

  /**
   * parses the query part from an URL
   * 
   * @param url the URL
   */
  public static Map<String, String> parseQueryString(URL url) {
    String queryString = url.getQuery();
    Map<String, String> map = new HashMap<>();
    if (queryString == null) {
      return map;
    }
    for (String parameter : queryString.split("&")) {
      String[] entry = parameter.split("=");
      if (entry.length != 2) {
        continue;
      }
      String key = entry[0];
      String value = entry[1];
      map.put(key, value);
    }
    return map;
  }
}
