package com.nachoverdon.mongolia.node2bean;

import com.nachoverdon.mongolia.annotations.Children;
import com.nachoverdon.mongolia.annotations.Translatable;
import com.nachoverdon.mongolia.utils.LangUtils;
import com.nachoverdon.mongolia.utils.NodeUtils;
import com.nachoverdon.mongolia.utils.PropertyUtils;
import com.nachoverdon.mongolia.utils.ReflectionUtils;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class Node2Bean {

  /**
   * Transforms a Node into an object of the given class. Optionally, if the Node has properties
   * annotated as translatable it will get the corresponding property if a language is given.
   * It requires that the given class has an empty constructor.
   *
   * @param <T> The type of the JavaBean
   * @param node The Node to get the data from.
   * @param clazz The class of the JavaBean
   * @param lang Optional. The language to get the properties from.
   * @return An object of the given class type.
   */
  @Nullable
  public static <T> T toBean(Node node, Class<T> clazz, @Nullable String lang) {
    if (StringUtils.isEmpty(lang)) {
      lang = LangUtils.getLanguage();
    }

    try {
      Constructor<T> constructor = ReflectionUtils.getEmptyConstructor(clazz);

      if (constructor == null) {
        return null;
      }

      T object = constructor.newInstance();
      Collection<String> objectFieldNames = ReflectionUtils.getFieldsNames(object);

      // Loop through node properties
      addProperties(node, objectFieldNames, clazz, lang, object);
      // Loop through node children
      addChildrenNodes(node, objectFieldNames, clazz, lang, object);

      return object;

    } catch (InstantiationException e) {
      log.error("Cannot instantiate object", e);
    } catch (InvocationTargetException | IllegalAccessException | RepositoryException e) {
      log.error(e.getMessage(), e);
    }

    return null;
  }

  /**
   * Refer to {@link #toBean(Node, Class, String)}.
   *
   * @param <T> The type of the JavaBean
   * @param node The Node to get the data from.
   * @param clazz The class of the JavaBean
   * @return An object of the given class type.
   */
  public static <T> T toBean(Node node, Class<T> clazz) {
    return toBean(node, clazz, LangUtils.getLanguage());
  }

  /**
   * Gets the properties from the Node and sets the fields, translated if possible, of the object of
   * the given class.
   *
   * @param node Node to get the properties from
   * @param objectFieldNames The list of field names of the class
   * @param clazz The class of the object
   * @param lang The language to use for the translatable fields
   * @param object The object that will receive the data
   * @param <T> The type of the object
   * @throws RepositoryException If the properties or nodes cannot be accessed.
   * @throws IllegalAccessException If the field cannot be set.
   */
  public static <T> void addProperties(Node node, Collection<String> objectFieldNames,
                                       Class<T> clazz, String lang, Object object)
      throws RepositoryException, IllegalAccessException {
    PropertyIterator propertyIterator = node.getProperties();

    while (propertyIterator.hasNext()) {
      Property property = propertyIterator.nextProperty();

      // Check if object has that property
      if (!objectFieldNames.contains(property.getName())) {
        continue;
      }

      try {
        I18nContentSupport i18nContentSupport = Components.getComponent(I18nContentSupport.class);
        Field field = clazz.getField(property.getName());
        String defaultLang = MgnlContext.isWebContext()
            ? i18nContentSupport.getFallbackLocale().getLanguage()
            : LangUtils.DEFAULT_LANG;

        // Check i18n properties to return it correctly
        if (!lang.equals(defaultLang) && field.getDeclaredAnnotation(Translatable.class) != null) {

          //Fill object with lang values
          String propertyNameI18n = property.getName() + "_" + lang;

          if (!node.hasProperty(propertyNameI18n)) {
            continue;
          }

          Property propertyI18n = node.getProperty(propertyNameI18n);

          if (propertyI18n.getValue() != null) {
            field.set(object, PropertyUtils.getPropertyByType(propertyI18n));
          } else {
            field.set(object, PropertyUtils.getPropertyByType(property));
          }

          //Fill object with default lang values
        } else {
          field.set(object, PropertyUtils.getPropertyByType(property));
        }

      } catch (NoSuchFieldException e) {
        log.error("Field '" + property.getName() + "' not found in '" + clazz.getName()
            + "' class");
      }
    }
  }

  /**
   * Gets the children nodes from the node and sets the their fields, translated if possible, of the
   * object of the given class.
   *
   * @param node Node to get the nodes from
   * @param objectFieldNames The list of field names of the class
   * @param clazz The class of the object
   * @param lang The language to use for the translatable fields
   * @param object The object that will receive the data
   * @param <T> The type of the object
   * @throws RepositoryException If the properties or nodes cannot be accessed.
   * @throws IllegalAccessException If the field cannot be set.
   */
  public static <T> void addChildrenNodes(Node node, Collection<String> objectFieldNames,
                                          Class<T> clazz, String lang, Object object)
      throws RepositoryException, IllegalAccessException {
    NodeIterator nodes = node.getNodes();

    while (nodes.hasNext()) {
      Node children = nodes.nextNode();

      if (!objectFieldNames.contains(children.getName())) {
        continue;
      }

      try {
        Field field = clazz.getField(children.getName());
        Class<?> childrenClass = field.getDeclaredAnnotation(Children.class).typeOf();
        boolean isCollection = field.getType().getName().equals(Collection.class.getName());

        if (field.getDeclaredAnnotation(Children.class) == null || !isCollection) {
          continue;
        }

        List<Object> childrenNodes = new ArrayList<>();

        NodeUtils.forEach(children.getNodes(),
            item -> childrenNodes.add(toBean(item, childrenClass, lang))
        );
        // Add children to the object
        field.set(object, childrenNodes);

      } catch (NoSuchFieldException e) {
        log.error("Field '" + node.getName() + "' not found in '" + clazz.getName() + "' class");
      }
    }
  }

}
