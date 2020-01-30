package com.nachoverdon.mongolia.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class ReflectionUtil {

    public static List<Field> getAllFields(Class clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }

        List<Field> result = new ArrayList<>(getAllFields(clazz.getSuperclass()));
        List<Field> filteredFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> Modifier.isPublic(f.getModifiers()) || Modifier.isProtected(f.getModifiers()))
                .collect(Collectors.toList());
        result.addAll(filteredFields);
        return result;
    }

    public static Constructor getEmptyConstructor(Class clazz) {
        Constructor constructor = null;
        for (Constructor ctr: clazz.getConstructors()) {
            if (ctr.getParameterCount() == 0) {
                constructor = ctr;
                break;
            }
        }

        return constructor;
    }


    public static Collection<String> getFieldsNames(Object object) {

        Collection<String> fieldNames = new ArrayList<>();
        List<Field> objectFields = getAllFields(object.getClass());

        for(Field f: objectFields) {
            fieldNames.add(f.getName());
        }

        return fieldNames;
    }

}
