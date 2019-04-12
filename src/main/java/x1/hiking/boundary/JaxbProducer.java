package x1.hiking.boundary;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;

/**
 * CDI Producer for JAXB
 */
@ApplicationScoped
public class JaxbProducer {
  
  @Produces
  private Marshaller createMarshaller() throws JAXBException {
    JAXBContext context = JAXBContext.newInstance(Feed.class, Entry.class);
    return context.createMarshaller();
  }
  
  @Produces
  private Unmarshaller createUnmarshaller() throws JAXBException {
    JAXBContext context = JAXBContext.newInstance(Feed.class, Entry.class);
    return context.createUnmarshaller();
  }  
}
