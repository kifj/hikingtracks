package x1.oauth;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.model.User;
import x1.hiking.rest.SessionValidator;
import x1.hiking.utils.AuthorizationConstants;
import x1.hiking.utils.ServletHelper;

/**
 * Servlet filter for Authentification & Authorization
 * 
 * @author joe
 * 
 */
public class AuthorizationFilter implements Filter, AuthorizationConstants {
  private final Logger log = LoggerFactory.getLogger(getClass());

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#destroy()
   */
  @Override
  public void destroy() {
    // not needed
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
   * javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
  }

  private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      User user = validator.validateUser(false, request, response);
      Date expiration = user.getExpires();
      if (expiration != null && expiration.compareTo(new Date()) < 0) {
        log.info("Token {} has expired {}", user.getToken(), expiration);
        user = null;
      } else {
        log.debug("found={}", user);
      }
      if (user != null) {
        chain.doFilter(request, response);
      } else {
        redirectToLogin(request, response);
      }
    } catch (ForbiddenException | NotAuthorizedException e) {
      redirectToLogin(request, response);
    }
  }

  private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String destinationURL = ServletHelper.getRequestUrl(request).build().toString();
    String queryString = request.getQueryString();
    if (StringUtils.isNotEmpty(queryString)) {
      destinationURL += "?" + queryString;
    }
    UriBuilder b = UriBuilder.fromUri(ServletHelper.getBaseUrl(request) + LOGIN_PAGE);
    b.queryParam(PARAM_FROM, URLEncoder.encode(destinationURL, UTF_8));
    String url = b.build().toString();
    log.info("Authentification required, redirecting to login: {}", url);
    ServletHelper.revokeSessionCookie(response);
    response.sendRedirect(url);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(FilterConfig config) throws ServletException {
    // not needed
  }

  @Inject
  private SessionValidator validator;
}
