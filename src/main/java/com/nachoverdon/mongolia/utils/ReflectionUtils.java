package com.nachoverdon.mongolia.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class ReflectionUtils {

    /**
     * Gets all the public and protected fields from the given class
     *
     * @param clazz The class to read fields from
     * @return A list for Fields
     */
    public static List<Field> getAllPublicAndProtectedFields(Class clazz) {
        if (clazz == null)
            return Collections.emptyList();

        List<Field> result = new ArrayList<>(getAllPublicAndProtectedFields(clazz.getSuperclass()));
        List<Field> filteredFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> Modifier.isPublic(f.getModifiers()) || Modifier.isProtected(f.getModifiers()))
                .collect(Collectors.toList());

        result.addAll(filteredFields);

        return result;
    }

    /**
     * Gets a constructor that takes no parameters from the given class
     *
     * @param clazz The class to get the constructor from
     * @return The empty constructor or null
     */
    public static Constructor getEmptyConstructor(Class clazz) {

        for (Constructor ctr: clazz.getConstructors()) {
            if (ctr.getParameterCount() == 0)
                return ctr;
        }

        return null;
    }

    /**
     * Gets a list of all the public and protected field names of a given object
     *
     * @param object The object to get the field names from
     * @return A list of field names
     */
    public static Collection<String> getFieldsNames(Object object) {
        Collection<String> fieldNames = new ArrayList<>();
        List<Field> objectFields = getAllPublicAndProtectedFields(object.getClass());

        for(Field f: objectFields) {
            fieldNames.add(f.getName());
        }

        return fieldNames;
    }

}
