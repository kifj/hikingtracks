package x1.hiking.thumbnails;

/**
 * Thumbnail size.
 *
 * @author joe
 */
public class ThumbnailSize {
  
  /**
   * Instantiates a new thumbnail size.
   *
   * @param width the width
   * @param height the height
   * @param keepAspect the keep aspect
   * @param quality the quality
   */
  public ThumbnailSize(int width, int height, boolean keepAspect, float quality) {
    this.width = width;
    this.height = height;
    this.keepAspect = keepAspect;
    this.quality = quality;
  }

  /**
   * Gets the height.
   *
   * @return the height
   */
  public int getHeight() {
    return height;
  }

  /**
   * Gets the width.
   *
   * @return the width
   */
  public int getWidth() {
    return width;
  }

  /**
   * Checks if is keep aspect.
   *
   * @return true, if is keep aspect
   */
  public boolean isKeepAspect() {
    return keepAspect;
  }

  /**
   * Gets the quality.
   *
   * @return the quality
   */
  public float getQuality() {
    return quality;
  }

  private boolean keepAspect;
  private int height;
  private int width;
  private float quality;
}