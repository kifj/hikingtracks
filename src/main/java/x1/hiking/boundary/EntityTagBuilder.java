package x1.hiking.boundary;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.EntityTag;

import org.apache.commons.codec.binary.Base64;

import x1.hiking.model.Model;
import x1.hiking.model.Track;
import x1.hiking.model.User;
import x1.hiking.representation.Cacheable;

/**
 * Builder for Entity Tags
 *
 * @author joe
 */
 public class EntityTagBuilder {
  private static final String HEADER_ACCEPT = "Accept";

  public EntityTagBuilder(HttpServletRequest httpServletRequest) {
    this.httpServletRequest = httpServletRequest;
  }
  
  public EntityTag buildEntityTag(Model model) {
    StringBuilder buffer = new StringBuilder();
    buildEntityTag(buffer, model);
    return buildEntityTag(buffer);
  }

  public EntityTag buildEntityTag(User user, List<Model> models, Track track) {
    models.addAll(track.getImages());
    models.addAll(track.getTrackData());
    models.add(track.getUser());
    return buildEntityTag(user, models, (Cacheable) null);
  }

  public EntityTag buildEntityTag(User user, List<? extends Model> models, Cacheable container) {
    StringBuilder buffer = new StringBuilder();
    models.forEach(model -> buildEntityTag(buffer, model));
    if (user != null) {
      buildEntityTag(buffer, user);
    }
    if (container != null) {
      container.computeEntityTag(buffer);
    }
    return buildEntityTag(buffer);
  }

  public void buildEntityTag(StringBuilder buffer, Model model) {
    buffer.append(model.getId()).append("=").append(model.getVersion());
  }

  public EntityTag buildEntityTag(StringBuilder buffer) {
    String acceptHeader = httpServletRequest.getHeader(HEADER_ACCEPT);
    buffer.append(";").append(acceptHeader);
    String encoded = Base64.encodeBase64String(buffer.toString().getBytes(StandardCharsets.UTF_8));
    return new EntityTag(encoded);
  }
  
  private HttpServletRequest httpServletRequest;
}
