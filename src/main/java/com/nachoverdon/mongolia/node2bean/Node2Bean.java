package com.nachoverdon.mongolia.node2bean;

import com.nachoverdon.mongolia.annotations.Children;
import com.nachoverdon.mongolia.annotations.Translatable;
import com.nachoverdon.mongolia.utils.ReflectionUtil;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Node2Bean {

    private static Logger log = LoggerFactory.getLogger(Node2Bean.class);

    public static Object toBean(Node node, Class className) throws RepositoryException {
        String currentLang = MgnlContext.getAggregationState().getLocale().getLanguage();

        return toBean(node, className, currentLang);
    }

    public static Object toBean(Node node, Class className, String lang) throws RepositoryException {
        // @TODO: if lang is null, empty or is not one of the available languages, get current language
        // if (StringUtil.EmptyOrNull(lang) || !getAvailableImportLanguagesArray().contains(lang))
        if (lang == null || lang.equals(StringUtils.EMPTY))
            lang = MgnlContext.getAggregationState().getLocale().getLanguage();

        try {
           Constructor<?> constructor = ReflectionUtil.getEmptyConstructor(className);

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
                            Field f = className.getField(property.getName());
                            String defaultLang = MgnlContext.isWebContext()
                                    ? I18nContentSupportFactory.getI18nSupport().getFallbackLocale().getLanguage()
                                    : "en";

                            /* Check i18n properties to return it correctly */
                            if (!lang.equals(defaultLang) && f.getDeclaredAnnotation(Translatable.class) != null ) {

                                //Fill object with lang values
                                String propertyNameI18n = property.getName() + "_" + lang;

                                if (node.hasProperty(propertyNameI18n)){
                                    Property propertyI18n = node.getProperty(propertyNameI18n);

                                    if (propertyI18n.getValue() != null){
                                        f.set(object, getPropertyByType(propertyI18n));
                                    } else {
                                        f.set(object, getPropertyByType(property));
                                    }
                                }
                            } else {
                                //Fill object with default lang values
                                f.set(object, getPropertyByType(property));
                            }

                        } catch (NoSuchFieldException e ) {
                            log.error("Field '" + property.getName() +"' not found in '" + className.getName() +"' class");
                        }
                    }
                }

                /* Loop through node children */
                NodeIterator nodeIterator = node.getNodes();

                while (nodeIterator.hasNext()) {
                    Node children = nodeIterator.nextNode();

                    if (objectFieldsNames.contains(children.getName())) {
                        try {
                            Field f = className.getField(children.getName());
                            Class clazz = f.getDeclaredAnnotation(Children.class).typeOf();
                            boolean isCollection = f.getType().getName().equals(Collection.class.getName());

                            if (f.getDeclaredAnnotation(Children.class) != null && isCollection) {
                                Collection<Object> childrenNodeList = new ArrayList<>();
                                NodeIterator nodeListIterator = children.getNodes();

                                while (nodeListIterator.hasNext()) {
                                    Node item = nodeListIterator.nextNode();

                                    childrenNodeList.add(toBean(item, clazz));
                                }

                                /* Add children to the object */
                                f.set(object, childrenNodeList);
                            }
                        } catch(NoSuchFieldException e) {
                            log.error("Field '" + node.getName() +"' not found in '" + className.getName() +"' class");
                        }
                    }
                }

                return object;
            }

            return null;

        } catch (InstantiationException e) {
            log.error("Cannot instantiate object", e.getMessage());
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error(e.getMessage());
        }

        return null;
    }

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

    private static Object getPropertyByType(Property property) throws RepositoryException {
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
                return null;
        }
    }
}
