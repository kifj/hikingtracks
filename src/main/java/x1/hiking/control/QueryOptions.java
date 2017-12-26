package x1.hiking.control;

import x1.hiking.model.ActivityType;

/**
 * Query parameters
 * 
 * @author joe
 * 
 */
public class QueryOptions {
  private static final int DEFAULT_MAX_RESULTS = 10;
  
  /**
   * Constructor with arguments
   * 
   * @param startPosition
   *          the starting position
   * @param maxResults
   *          the maximum number
   * @param activity 
   *          limit query to a type of activities
   */
  public QueryOptions(Integer startPosition, Integer maxResults, ActivityType activity) {
    this.startPosition = (startPosition != null) ? startPosition : 0;
    this.maxResults = (maxResults != null) ? maxResults : DEFAULT_MAX_RESULTS;
    this.activity = activity;
  }

  /**
   * @return the startPosition
   */
  public int getStartPosition() {
    return startPosition;
  }

  /**
   * @return the maxResults
   */
  public int getMaxResults() {
    return maxResults;
  }
  
  /**
   * @return the activity type
   */
  public ActivityType getActivity() {
    return activity;
  }

  private int startPosition;
  private int maxResults;
  private ActivityType activity;
}
