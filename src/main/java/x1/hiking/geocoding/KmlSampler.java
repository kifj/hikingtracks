package x1.hiking.geocoding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.model.Coord;
import x1.hiking.model.TrackData;
import x1.hiking.representation.Representation;

/**
 * Sample of coordinates from KML file
 * 
 * @author joe
 */
public class KmlSampler {
  private static final Logger LOG = LoggerFactory.getLogger(KmlSampler.class);
  private static final Result EMPTY_RESULT = new Result();

  public KmlSampler(int samples, double offsetDistance) {
    this.samples = samples;
    this.offsetDistance = offsetDistance * 1000.0d;
  }

  public KmlSampler(double offsetDistance) {
    this.samples = 256;
    this.offsetDistance = offsetDistance * 1000.0d;
  }
  
  /**
   * Sampling result
   */
  public static class Result {

    public Result() {
      this.samples = new Waypoint[0];
      this.distance = 0;
    }

    public Result(List<Waypoint> samples, double distance) {
      this.samples = samples.toArray(new Waypoint[samples.size()]);
      this.distance = distance;
    }

    public double getDistance() {
      return distance;
    }

    public Waypoint[] getSamples() {
      return samples;
    }

    private double distance;
    private Waypoint[] samples;
  }

  public static Result parse(TrackData td) {
    return parse(td, 0);
  }
  
  public static Result parse(TrackData td, double offsetDistance) {
    try {
      if (td.getData() == null) {
        return EMPTY_RESULT;
      }
      KmlSampler parser = new KmlSampler(offsetDistance);
      LOG.debug("Parsing KML data for {}", td);
      if (td.getName().endsWith(Representation.FILE_EXTENSION_KML)) {
        return parser.parse(new String(td.getData(), Representation.ENC_UTF_8));
      } else if (td.getName().endsWith(Representation.FILE_EXTENSION_KMZ)) {
        return parser.unZipAndParse(td.getData());
      }
      return EMPTY_RESULT;
    } catch (XMLStreamException | IOException e) {
      LOG.warn(null, e);
      return EMPTY_RESULT;
    }
  }

  private Result parse(Reader reader) throws XMLStreamException {
    List<Coord> result = new ArrayList<>();
    XMLEventReader eventReader = XMLInputFactory.newInstance().createXMLEventReader(reader);
    inCoord = false;
    inCoordinates = false;
    inFolder = 0;

    while (eventReader.hasNext()) {
      XMLEvent event = eventReader.nextEvent();
      switch (event.getEventType()) {
      case XMLStreamConstants.START_ELEMENT:
        visitStartElement(event);
        break;
      case XMLStreamConstants.CHARACTERS:
        visitCharacters(result, event);
        break;
      case XMLStreamConstants.END_ELEMENT:
        visitEndElement(result, event);
        break;
      default:
        break;
      }
    }
    return reduce(result, Math.min(result.size(), samples), offsetDistance);
  }

  private void visitEndElement(List<Coord> result, XMLEvent event) {
    EndElement endElement = event.asEndElement();
    qName = endElement.getName().getLocalPart();
    if (qName.equalsIgnoreCase("coord")) {
      inCoord = false;
    } else if (qName.equalsIgnoreCase("coordinates")) {
      inCoordinates = false;
      String[] elems = StringUtils.split(buffer.toString(), " \n");
      for (String elem : elems) {
        String[] data = StringUtils.split(elem, ',');
        if (data.length == 3) {
          Coord coord = new Coord(Float.parseFloat(data[1]), Float.parseFloat(data[0]), Float.parseFloat(data[2]));
          result.add(coord);
        }
      }
    } else if (qName.equalsIgnoreCase("Folder")) {
      inFolder--;
    }
  }

  private void visitCharacters(List<Coord> result, XMLEvent event) {
    if (inCoord && inFolder <= 1) {
      Characters characters = event.asCharacters();
      String[] data = StringUtils.split(characters.getData(), " \n");
      if (data.length == 3) {
        Coord coord = new Coord(Float.parseFloat(data[1]), Float.parseFloat(data[0]), Float.parseFloat(data[2]));
        result.add(coord);
      }
    } else if (inCoordinates && inFolder <= 1) {
      Characters characters = event.asCharacters();
      buffer.append(characters.getData());
    }
  }

  private void visitStartElement(XMLEvent event) {
    StartElement startElement = event.asStartElement();
    qName = startElement.getName().getLocalPart();
    if (qName.equalsIgnoreCase("coord")) {
      inCoord = true;
    } else if (qName.equalsIgnoreCase("Folder")) {
      inFolder++;
    } else if (qName.equalsIgnoreCase("coordinates")) {
      inCoordinates = true;
      buffer = new StringBuilder();
    }
  }

  private Result parse(String content) throws XMLStreamException {
    try (StringReader reader = new StringReader(content)) {
      return parse(reader);
    }
  }

  private Result unZipAndParse(byte[] compressed) throws IOException, XMLStreamException {
    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(compressed))) {
      ZipEntry e = zis.getNextEntry();
      if (e != null) {
        try (Reader reader = new InputStreamReader(zis, Representation.ENC_UTF_8)) {
          return parse(reader);
        }
      }
      return EMPTY_RESULT;
    }
  }

  private Result reduce(List<Coord> data, int samples, double offsetDistance) {
    if (data.isEmpty()) {
      return EMPTY_RESULT;
    }
    double distance = offsetDistance;
    int step = data.size() / samples;
    int counter = 0;
    Coord l = null;
    List<Waypoint> result = new ArrayList<>();
    for (Coord c : data) {
      if (l != null) {
        distance += new DistanceCalculator(l, c).distance();
      }
      if (counter++ == step) {
        counter = 0;
        result.add(new Waypoint(c, distance / 1000.0d));
      }
      l = c;
    }
    return new Result(result, distance / 1000.0d);
  }

  private boolean inCoord = false;
  private boolean inCoordinates = false;
  private int inFolder = 0;
  private int samples;
  private double offsetDistance;
  private String qName;
  private StringBuilder buffer;
}
