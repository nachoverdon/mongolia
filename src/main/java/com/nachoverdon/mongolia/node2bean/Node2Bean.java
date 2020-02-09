package com.nachoverdon.mongolia.node2bean;

import com.nachoverdon.mongolia.annotations.Children;
import com.nachoverdon.mongolia.annotations.Translatable;
import com.nachoverdon.mongolia.utils.ReflectionUtil;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.context.MgnlContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Slf4j
public class Node2Bean {
    private static final String DEFAULT_LANG = "en";

    /**
     * Transforms a Node into an object of the given class. Optionally, if the Node has properties annotated as
     * translatable it will get the corresponding property if a language is given.
     *
     * @param <T> The type of the JavaBean
     * @param node The Node to get the data from.
     * @param clazz The class of the JavaBean
     * @param lang Optional. The language to get the properties from.
     * @return An object of the given class type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T toBean(Node node, Class<T> clazz, String lang) {
        if (lang == null || lang.equals(StringUtils.EMPTY))
            lang = MgnlContext.getAggregationState().getLocale().getLanguage();

        try {
           Constructor<?> constructor = ReflectionUtil.getEmptyConstructor(clazz);

            if (constructor != null)  {
                Object object = constructor.newInstance();
                Collection<String> objectFieldsNames = ReflectionUtil.getFieldsNames(object);

                /* Loop through node properties */
                PropertyIterator propertyIterator = node.getProperties();

                while (propertyIterator.hasNext()) {
                    Property property = propertyIterator.nextProperty();

                    /* Check if object has that property */
                    if (objectFieldsNames.contains(property.getName())) {
                        try {
                            Field field = clazz.getField(property.getName());
                            String defaultLang = MgnlContext.isWebContext()
                                    ? I18nContentSupportFactory.getI18nSupport().getFallbackLocale().getLanguage()
                                    : DEFAULT_LANG;

                            /* Check i18n properties to return it correctly */
                            if (!lang.equals(defaultLang) && field.getDeclaredAnnotation(Translatable.class) != null ) {

                                //Fill object with lang values
                                String propertyNameI18n = property.getName() + "_" + lang;

                                if (node.hasProperty(propertyNameI18n)){
                                    Property propertyI18n = node.getProperty(propertyNameI18n);

                                    if (propertyI18n.getValue() != null){
                                        field.set(object, getPropertyByType(propertyI18n));
                                    } else {
                                        field.set(object, getPropertyByType(property));
                                    }
                                }
                            } else {
                                //Fill object with default lang values
                                field.set(object, getPropertyByType(property));
                            }

                        } catch (NoSuchFieldException e ) {
                            log.error("Field '" + property.getName() +"' not found in '" + clazz.getName() +"' class");
                        }
                    }
                }

                /* Loop through node children */
                NodeIterator nodeIterator = node.getNodes();

                while (nodeIterator.hasNext()) {
                    Node children = nodeIterator.nextNode();

                    if (objectFieldsNames.contains(children.getName())) {
                        try {
                            Field field = clazz.getField(children.getName());
                            Class class_ = field.getDeclaredAnnotation(Children.class).typeOf();
                            boolean isCollection = field.getType().getName().equals(Collection.class.getName());

                            if (field.getDeclaredAnnotation(Children.class) != null && isCollection) {
                                Collection<Object> childrenNodeList = new ArrayList<>();
                                NodeIterator nodeListIterator = children.getNodes();

                                while (nodeListIterator.hasNext()) {
                                    Node item = nodeListIterator.nextNode();

                                    childrenNodeList.add(toBean(item, class_, lang));
                                }

                                /* Add children to the object */
                                field.set(object, childrenNodeList);
                            }
                        } catch(NoSuchFieldException e) {
                            log.error("Field '" + node.getName() +"' not found in '" + clazz.getName() +"' class");
                        }
                    }
                }

                return (T) object;
            }
        } catch (InstantiationException e) {
            log.error("Cannot instantiate object", e.getMessage());
        } catch (InvocationTargetException | IllegalAccessException | RepositoryException e) {
            log.error(e.getMessage());
        }

        return null;
    }

    /**
     * Refer to {@link #toBean(Node, Class, String)}
     *
     * @param <T> The type of the JavaBean
     * @param node The Node to get the data from.
     * @param clazz The class of the JavaBean
     */
    public static <T> T toBean(Node node, Class<T> clazz) {
        String currentLang = MgnlContext.getAggregationState().getLocale().getLanguage();

        return toBean(node, clazz, currentLang);
    }

    // @TODO: Add documentation
    public static String setSearchableFields(Class className, String queryString) {
        String currentLang = MgnlContext.getAggregationState().getLocale().getLanguage();
        Constructor<?> constructor = ReflectionUtil.getEmptyConstructor(className);
        StringBuilder addedQuerySB = new StringBuilder();

        try {
            if (constructor != null)  {
                Object object = constructor.newInstance();
                List<Field> fieldList = ReflectionUtil.getAllFields(object.getClass());

                for (Field field : fieldList) {
                    String fieldName = field.getName();

                    /* Check if is a translatable field */
                    if (field.getDeclaredAnnotation(Translatable.class) != null) {
                        String defaultLang = MgnlContext.isWebContext()
                                ? I18nContentSupportFactory.getI18nSupport().getFallbackLocale().getLanguage()
                                : "en";

                        fieldName += ((!currentLang.equals(defaultLang)) ? "_" + currentLang : "");
                    }

                    /* Prevent children to destroy the query */
                    if (field.getDeclaredAnnotation(Children.class) == null) {
                        addedQuerySB.append("LOWER(t.")
                            .append(fieldName)
                            .append(") LIKE '%%")
                            .append(StringUtils.replace(queryString.toLowerCase(), "'", "''"))
                            .append("%%' OR ");
                    }
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error(e.getMessage());
        } catch (InstantiationException e) {
            log.error("Cannot instantiate object", e.getMessage());
        }

        String addedQuery = addedQuerySB.toString();

        // Remove last 2 characters if they are 'OR'
        if (addedQuery.substring((addedQuery.length() - 3)).equals("OR ")) {
            addedQuery = addedQuery.substring(0, addedQuery.length() - 3);
        }

        return addedQuery;
    }

    /**
     * Gets a single value from a property by type
     *
     * @param property A Node's property
     * @return The value
     */
    private static Object getPropertyByType(Property property) {
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
            }
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }
}
