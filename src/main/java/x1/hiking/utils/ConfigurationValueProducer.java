package x1.hiking.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration Value Producer for CDI
 * 
 * @author joe
 */
@ApplicationScoped
public class ConfigurationValueProducer {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private static final String MANDATORY_PARAM_MISSING = "No definition found for a mandatory configuration parameter : '{0}'";
  private static final String PROPERTIES_FILE_NAME = "hikingtracks.properties";
  private static final String BUNDLE_FILE_NAME = "application";
  private ResourceBundle bundle = null;
  private Properties properties = new Properties();

  public ConfigurationValueProducer() {
    init();
  }

  private void init() {
    bundle = ResourceBundle.getBundle(BUNDLE_FILE_NAME);
    String directory = StringUtils.defaultIfEmpty(System.getenv("HIKINGTRACKS_CONFIG_DIR"),
        System.getProperty("jboss.server.config.dir"));
    File file = new File(directory, PROPERTIES_FILE_NAME);
    if (file.exists()) {
      try (FileInputStream fis = new FileInputStream(file)) {
        properties.load(fis);
        log.info("Read configuration from {} with {}", file.getAbsolutePath(), properties);        
      } catch (IOException e) {
        log.warn(e.getMessage());
      }
    }
  }

  private String getValue(String key) {
    return properties.getProperty(key, bundle.getString(key));
  }

  @Produces
  @ConfigurationValue
  public String injectConfiguration(InjectionPoint ip) {
    ConfigurationValue param = ip.getAnnotated().getAnnotation(ConfigurationValue.class);
    if (param.key() == null || param.key().length() == 0) {
      return param.defaultValue();
    }

    try {
      String value = getValue(param.key());
      if (value == null || value.trim().length() == 0) {
        checkMandatory(param);
        return param.defaultValue();
      }
      return value;
    } catch (MissingResourceException e) {
      checkMandatory(param);
      return param.defaultValue();
    }
  }

  private void checkMandatory(ConfigurationValue param) {
    if (param.mandatory()) {
      throw new IllegalStateException(MessageFormat.format(MANDATORY_PARAM_MISSING, new Object[] { param.key() }));
    }
  }
}