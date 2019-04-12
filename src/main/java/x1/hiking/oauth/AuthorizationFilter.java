package x1.hiking.oauth;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.boundary.SessionValidator;
import x1.hiking.utils.AuthorizationConstants;
import x1.hiking.utils.ServletHelper;

/**
 * Servlet filter for Authentification & Authorization
 * 
 * @author joe
 * 
 */
public class AuthorizationFilter implements Filter, AuthorizationConstants {
  private final Logger log = LoggerFactory.getLogger(AuthorizationFilter.class);

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
      validator.validateToken(request, response);
      chain.doFilter(request, response);
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
    String url = ServletHelper.getBaseUrl(request).path(LOGIN_PAGE)
        .queryParam(PARAM_FROM, URLEncoder.encode(destinationURL, StandardCharsets.UTF_8.name())).build().toString();
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
  public void init(FilterConfig config) {
    // not needed
  }

  @Inject
  private SessionValidator validator;
}
