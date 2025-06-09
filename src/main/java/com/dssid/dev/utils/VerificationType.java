package com.dssid.dev.utils;


import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
public class VerificationType {

    public static boolean isNumberTypeInteger(Class<?> type) {
        return type == int.class || type == Integer.class || type == long.class || type == Long.class;
    }

    public static boolean isNumberTypeFloat(Class<?> type) {
        return type == float.class || type == Float.class || type == double.class
                || type == Double.class || type == BigDecimal.class;
    }

    public static boolean isTypeBoolean(Class<?> type) {
        return type == boolean.class || type == Boolean.class;
    }

    public static boolean isTypeDateOrLocalDate(Class<?> type) {
        return type == Date.class || type == LocalDate.class;
    }

    public static boolean isTypeLocalDateTime(Class<?> type) {
        return type == LocalDateTime.class;
    }

    public static boolean isTypeEnum(Class<?> type) {
        return type.isEnum();
    }

    public static boolean isCollectionOrArray(Class<?> type) {
        return Collection.class.isAssignableFrom(type) || type.isArray();
    }

    public static boolean valueIsBoolean(Object value) {
        var clazz = value.getClass();
        return isTypeBoolean(clazz);
    }

    public static boolean isNumberFloatDouble(Object value) {
        var clazz = value.getClass();
        return isNumberTypeFloat(clazz);
    }

    public static boolean stringHasContent(String str) {
        return StringUtils.isNotBlank(str);
    }
    public static boolean stringNotHasContent(String str) {
        return !stringHasContent(str);
    }

    public static boolean containsDiamoent(String str) {
        return str.contains("<") && str.contains(">");
    }
}
