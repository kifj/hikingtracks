package x1.hiking.model;

/**
 * UTF-8 Symbols of sport activities
 */
public enum ActivityType {
  BICYCLE("\u21e0"),
  HIKING("\u219a"),
  MOUNTAINEER("\u21a2"),
  ALPIN_SKI("\u215e"),
  SNOWBOARD("\u2193"),
  NORDIC_SKI("\u2182"),
  SWIMMING("\u2183"),
  CANOE("\u2180"),
  ROWING("\u2209"),
  BOAT("\u219c"),
  DIVER("\u21d1"),
  WINDSURFER("\u21d9"),
  HORSE("\u21bf"),
  SKATER("\u21de"),
  INLINE_SKATER("\u21df"),
  PARAGLIDER("\u21d4");
  
  ActivityType(String symbol) {
    this.symbol = symbol;
  }
  
  private String symbol;
  
  public String getSymbol() {
    return symbol;
  }

  public static ActivityType fromSymbol(String activity) {
    for (ActivityType type : ActivityType.values()) {
      if (type.getSymbol().equals(activity)) {
        return type;
      }
    }
    return null;
  }
}
