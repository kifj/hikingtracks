package x1.hiking.representation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import x1.hiking.model.ActivityType;
import x1.hiking.model.Bounds;

/**
 * Search parameters
 */
@XmlRootElement(name = "search")
public class Search {
  private String name;
  private Integer maxResults;
  private ActivityType activity;
  private Bounds bounds;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @XmlElement(name = "max")
  public Integer getMaxResults() {
    return maxResults;
  }

  public void setMaxResults(Integer maxResults) {
    this.maxResults = maxResults;
  }

  public ActivityType getActivity() {
    return activity;
  }

  public void setActivity(ActivityType activity) {
    this.activity = activity;
  }

  public Bounds getBounds() {
    return bounds;
  }

  public void setBounds(Bounds bounds) {
    this.bounds = bounds;
  }

  @Override
  public String toString() {
    return "Search[name=" + name + ", maxResults=" + maxResults + ", activity=" + activity + ", bounds=" + bounds + "]";
  }
}
