package x1.hiking.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;

import x1.hiking.representation.Representation;

/**
 * ATOM Feed service
 * 
 * @author joe
 * 
 */
@Path("/feed")
public interface FeedService extends Representation {

  /**
   * get newest tracks
   */
  @GET
  @Path("/")
  @Produces({ MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_ATOM_XML + ";type=feed", MediaType.APPLICATION_JSON })
  Feed getTracks();

  /**
   * get newest tracks
   * 
   * @param id the id
   */
  @GET
  @Path("/{id}")
  @Produces({ MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_ATOM_XML + ";type=entry",
      MediaType.APPLICATION_JSON })
  Entry getTrack(@PathParam("id") Integer id);
}