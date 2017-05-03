package x1.hiking.representation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Date adapter for JAXB
 * 
 * @author joe
 */
public class DateAdapter extends XmlAdapter<String, Date> {
  private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
   */
  @Override
  public Date unmarshal(String value) {
    if (value == null) {
      return null;
    }
    return DatatypeConverter.parseDate(value).getTime();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
   */
  @Override
  public String marshal(Date value) {
    if (value == null) {
      return null;
    }
    return df.format(value);
  }
}