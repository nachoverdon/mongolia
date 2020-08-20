package com.nachoverdon.mongolia.utils;

import info.magnolia.jcr.util.PropertyUtil;
import java.util.Date;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertyUtils extends PropertyUtil {

  /**
   * Gets a single value from a property by type.
   *
   * @param property A Node's property
   * @return The value of the property
   */
  @Nullable
  public static Object getPropertyByType(Property property) {
    try {
      Value value = property.getValue();

      switch (value.getType()) {
        case PropertyType.STRING:
          return value.getString();
        case PropertyType.LONG:
          return (int) value.getLong();
        case PropertyType.DATE:
          return Date.from(value.getDate().toInstant());
        case PropertyType.BOOLEAN:
          return value.getBoolean();
        default:
          log.error("Property type not implemented yet.");
      }
    } catch (RepositoryException e) {
      log.error(e.getMessage(), e);
    }

    return null;
  }

  /**
   * Performs an action for every property.
   *
   * @param properties A PropertyIterator
   * @param action The action to perform
   */
  public static void forEach(PropertyIterator properties, Consumer<Property> action) {
    while (properties.hasNext()) {
      action.accept(properties.nextProperty());
    }
  }
}
