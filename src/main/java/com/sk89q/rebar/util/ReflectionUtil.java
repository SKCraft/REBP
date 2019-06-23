/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ReflectionUtil {

    private ReflectionUtil() {
    }

    public static Class<?> cls(String name) throws ClassNotFoundException {
        return Class.forName(name);
    }

    public static boolean isOf(Object obj, String name) {
        return obj.getClass().getCanonicalName().equals(name);
    }

    public static Object field(Class<?> cls, Object obj, String name)
            throws IllegalArgumentException, IllegalAccessException,
            SecurityException, NoSuchFieldException {
        Field field = cls.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(obj);
    }

    public static Object field(Object obj, String name)
            throws IllegalArgumentException, IllegalAccessException,
            SecurityException, NoSuchFieldException {
        Field field = obj.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(obj);
    }

    public static Object invoke(Object obj, String name)
            throws IllegalArgumentException, IllegalAccessException,
            SecurityException, NoSuchMethodException, InvocationTargetException {
        Method method = obj.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(obj);
    }

    public static Object invokeStatic(Class<?> cls, String name)
            throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        Method method = cls.getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(null);
    }

    public static Class<?> searchHierarchyForClass(Object object,
            String className) {
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            if (clazz.getSimpleName().equals(className)) {
                return clazz;
            }

            clazz = clazz.getSuperclass();
        }

        return null;
    }

}
