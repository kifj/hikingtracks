package x1.hiking.geocoding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x1.hiking.model.Coord;
import x1.hiking.model.TrackData;
import x1.hiking.representation.Representation;

public abstract class WaypointSampler {
  private static final Logger LOG = LoggerFactory.getLogger(WaypointSampler.class);
  
  protected static final Result EMPTY_RESULT = new Result();
    
  /**
   * Sampling result
   */
  public static class Result {

    public Result() {
      this.samples = new Waypoint[0];
      this.distance = 0;
    }

    public Result(List<Waypoint> samples, double distance) {
      this.samples = samples.toArray(new Waypoint[0]);
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
      if (td.getName().endsWith(Representation.FILE_EXTENSION_GPX)) {
        LOG.debug("Parsing GPX data for {}", td);
        return new GpxSampler(offsetDistance).parse(new String(td.getData(), StandardCharsets.UTF_8));
      }
      if (td.getName().endsWith(Representation.FILE_EXTENSION_KML)) {
        LOG.debug("Parsing KML data for {}", td);
        return new KmlSampler(offsetDistance).parse(new String(td.getData(), StandardCharsets.UTF_8));
      } else if (td.getName().endsWith(Representation.FILE_EXTENSION_KMZ)) {
        LOG.debug("Parsing KML data for {}", td);
        return new KmlSampler(offsetDistance).unZipAndParse(td.getData());
      }      
      
      return EMPTY_RESULT;
    } catch (XMLStreamException | IOException e) {
      LOG.warn(null, e);
      return EMPTY_RESULT;
    }
  }
  
  public static void updateTrackDataFields(TrackData td, String filename, byte[] incomingXML) {
    td.setName(filename);
    td.setData(incomingXML);
    Result result = parse(td);
    Coord[] coordinates = result.getSamples();
    td.setLocation(coordinates);
  }
  
  protected Result reduce(List<Coord> coordinates, int samples, double offsetDistance) {
    if (coordinates.isEmpty()) {
      return EMPTY_RESULT;
    }
    double distance = offsetDistance;
    int step = coordinates.size() / samples;
    int counter = 0;
    Optional<Coord> last = Optional.empty();
    List<Waypoint> result = new ArrayList<>();
    for (Coord current : coordinates) {
      if (last.isPresent()) {
        distance += new DistanceCalculator(last.get(), current).distance();
      }
      if (counter++ == step) {
        counter = 0;
        result.add(new Waypoint(current, distance / 1000.0d));
      }
      last = Optional.of(current);
    }
    return new Result(result, distance / 1000.0d);
  }
}
