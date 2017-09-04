package com.github.davidmarquis.redisq.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GenericsUtils {

    /**
     * Fetches the declared type on a specific interface implemented by the provided class.
     * @param clazz the class on which implemented interfaces will be looked upon
     * @param specificInterface the interface to look for
     * @return the generic type implemented for the provided interface, or null if not found.
     */
    public static Class<?> getGenericTypeOfInterface(Class<?> clazz, Class<?> specificInterface) {
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        if (genericInterfaces != null) {
            for (Type genericType : genericInterfaces) {
                if (genericType instanceof ParameterizedType) {
                    Type rawType = ((ParameterizedType) genericType).getRawType();
                    if (rawType.equals(specificInterface)) {
                        ParameterizedType paramType = (ParameterizedType) genericType;
                        return (Class<?>) paramType.getActualTypeArguments()[0];
                    }
                }
            }
        }
        return null;
    }
}
