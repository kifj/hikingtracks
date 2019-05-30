package x1.hiking.geocoding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
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
import x1.hiking.model.Coord;

/**
 * Sample of coordinates from KML file
 * 
 * @author joe
 */
public class KmlSampler extends WaypointSampler {

  protected KmlSampler(int samples, double offsetDistance) {
    this.samples = samples;
    this.offsetDistance = offsetDistance * 1000.0d;
  }

  protected KmlSampler(double offsetDistance) {
    this.samples = 256;
    this.offsetDistance = offsetDistance * 1000.0d;
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

  protected Result parse(String content) throws XMLStreamException {
    try (StringReader reader = new StringReader(content)) {
      return parse(reader);
    }
  }

  protected Result unZipAndParse(byte[] compressed) throws IOException, XMLStreamException {
    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(compressed))) {
      ZipEntry e = zis.getNextEntry();
      if (e != null) {
        try (Reader reader = new InputStreamReader(zis, StandardCharsets.UTF_8)) {
          return parse(reader);
        }
      }
      return EMPTY_RESULT;
    }
  }

  private boolean inCoord = false;
  private boolean inCoordinates = false;
  private int inFolder = 0;
  private int samples;
  private double offsetDistance;
  private String qName;
  private StringBuilder buffer;
}
