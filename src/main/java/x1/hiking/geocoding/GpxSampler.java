package x1.hiking.geocoding;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import x1.hiking.model.Coord;

/**
 * Sample of coordinates from GPX file
 * 
 * @author joe
 */
public class GpxSampler extends WaypointSampler {

  protected GpxSampler(int samples, double offsetDistance) {
    this.samples = samples;
    this.offsetDistance = offsetDistance * 1000.0d;
  }

  protected GpxSampler(double offsetDistance) {
    this.samples = 256;
    this.offsetDistance = offsetDistance * 1000.0d;
  }

  private Result parse(Reader reader) throws XMLStreamException {
    List<Coord> result = new ArrayList<>();
    XMLStreamReader streamReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);

    while (streamReader.hasNext()) {
      int type = streamReader.next();
      switch (type) {
      case XMLStreamReader.START_ELEMENT:
        visitStartElement(streamReader);
        break;
      case XMLStreamReader.END_ELEMENT:
        visitEndElement(result, streamReader);
        break;
      default:
        break;
      }
    }
    return reduce(result, Math.min(result.size(), samples), offsetDistance);
  }

  private void visitEndElement(List<Coord> result, XMLStreamReader streamReader) {
    qName = streamReader.getLocalName();
    if (qName.equalsIgnoreCase("trkpt")) {
      if (lat != null && lng != null && elevation != null) {
        Coord coord = new Coord(lat, lng, elevation);
        result.add(coord);
      }
      lat = null;
      lng = null;
      elevation = null;
    }
  }

  private void visitStartElement(XMLStreamReader streamReader) throws XMLStreamException {
    qName = streamReader.getLocalName();
    if (qName.equalsIgnoreCase("trkpt")) {
      
      for (int i = 0; i < streamReader.getAttributeCount(); i++) {
        String type = streamReader.getAttributeName(i).getLocalPart();
        String value = streamReader.getAttributeValue(i);
        switch (type) {
        case "lat":
          lat = Float.parseFloat(value);
          break;
        case "lon":
          lng = Float.parseFloat(value);
          break;
        default:
          break;
        }
      }      
    } else if (qName.equalsIgnoreCase("ele")) {
      String value = streamReader.getElementText();
      elevation = Float.parseFloat(value);
    }
  }

  protected Result parse(String content) throws XMLStreamException {
    try (StringReader reader = new StringReader(content)) {
      return parse(reader);
    }
  }

  private Float lat = null;
  private Float lng = null;
  private Float elevation = null;
  private int samples;
  private double offsetDistance;
  private String qName;  
}
