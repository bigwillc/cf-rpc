package com.bigwillc.cfrpccore.util;

import lombok.SneakyThrows;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Random;

/**
 * @author bigwillc on 2024/3/26
 */
public class MockUtils {


    public static Object mock(Class type) {
        if (type == int.class || type == Integer.class) {
            return 0;
        } else if (type == long.class || type == Long.class) {
            return 0L;
        } else if (type == double.class || type == Double.class) {
            return 0.0;
        } else if (type == float.class || type == Float.class) {
            return 0.0f;
        } else if (type == boolean.class || type == Boolean.class) {
            return false;
        } else if (type == char.class || type == Character.class) {
            return '0';
        } else if (type == byte.class || type == Byte.class) {
            return (byte) 0;
        } else if (type == short.class || type == Short.class) {
            return (short) 0;
        }

        if (type.equals(String.class)) {
            return "this_is_a_mock_string";
        }
        return mockPojo(type);
    }


    @SneakyThrows
    private static Object mockPojo(Class type) {
        Object result = type.getDeclaredConstructor().newInstance();
        Field[] fields = type.getDeclaredFields();
        for(Field f : fields) {
            f.setAccessible(true);
            Class<?> fType = f.getType();
            Object fValue = mock(fType);
            f.set(result, fValue);
        }
        return result;
    }

//    public static void main(String[] args) {
//        System.out.println(mock(UserDto.class).toString());
//    }
//
//    public static class UserDto{
//        private String name;
//        private Integer age;
//
//        public String toString() {
//            return "UserDto(name=" + this.name + ", age=" + this.age + ")";
//        }
//    }


}
