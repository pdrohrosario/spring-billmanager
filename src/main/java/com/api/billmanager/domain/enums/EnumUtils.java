package com.api.billmanager.domain.enums;

public class EnumUtils {

    public static <E extends Enum<E>> E parseEnum(Class<E> enumType, String value) {
        try {
            return Enum.valueOf(enumType, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Value invalid to enum " + enumType.getSimpleName() + ": " + value, e);
        }
    }
}
