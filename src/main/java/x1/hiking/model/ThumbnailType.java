package x1.hiking.model;

/** Thumbnail types */
public enum ThumbnailType {
  SMALL(new ThumbnailSize(150, 150, false, 0.8f)), 
  MEDIUM(new ThumbnailSize(300, 200, true, 0.9f)), 
  LARGE(new ThumbnailSize(640, 480, false, 0.95f)), 
  NONE(null);
  
  ThumbnailType(ThumbnailSize thumbnailSize) {
    this.thumbnailSize = thumbnailSize;
  }
  
  public ThumbnailSize getThumbnailSize() {
    return thumbnailSize;
  }
  
  private ThumbnailSize thumbnailSize;
}