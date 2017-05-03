package x1.hiking.utils;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * Configuration Value as CDI
 * 
 * @author joe
 */
@Qualifier
@Retention(RUNTIME)
@Target({ FIELD, METHOD, PARAMETER })
public @interface ConfigurationValue {
  /**
   * Bundle key: a valid bundle key or ""
   */
  @Nonbinding
  String key() default "";

  /**
   * Is it a mandatory property
   */
  @Nonbinding
  boolean mandatory() default false;

  /**
   * Default value if not provided
   */
  @Nonbinding
  String defaultValue() default "";
}