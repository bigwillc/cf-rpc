package com.bigwillc.cfrpccore.util;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author bigwillc on 2024/3/13
 */
public class TypeUtils {

    public static Object cast(Object origin, Class<?> type) {
        if(origin== null) {
            return null;
        }

        Class<?> aClass = origin.getClass();
        if (type.isAssignableFrom(aClass)) {
            return origin;
        }

        if (type.isArray()) {
            Object[] arr;
            if(origin instanceof List list) {
                origin = list.toArray();
            }
            int length = Array.getLength(origin);
            Class<?> componentType = type.getComponentType();
            Object resultArray = Array.newInstance(componentType, length);
            System.out.println("===> componentType = " + componentType.getCanonicalName());
            for (int i = 0; i < length; i++) {
                Array.set(resultArray, i, Array.get(origin, i));
            }
            return resultArray;
        }

        if(origin instanceof HashMap map) {
            JSONObject jsonObject = new JSONObject(map);
            return jsonObject.toJavaObject(type);
        }

        if(type.equals(Long.class) || type.equals(long.class)) {
            return Long.valueOf(origin.toString());
        } else if(type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.valueOf(origin.toString());
        } else if(type.equals(Double.class) || type.equals(double.class)) {
            return Double.valueOf(origin.toString());
        } else if(type.equals(Float.class) || type.equals(float.class)) {
            return Float.valueOf(origin.toString());
        } else if(type.equals(Short.class) || type.equals(short.class)) {
            return Short.valueOf(origin.toString());
        } else if(type.equals(Byte.class) || type.equals(byte.class)) {
            return Byte.valueOf(origin.toString());
        } else if(type.equals(Boolean.class) || type.equals(boolean.class)) {
            return Boolean.valueOf(origin.toString());
        } else if(type.equals(Character.class) || type.equals(char.class)) {
            return origin.toString().charAt(0);
        } else {
            return origin;
        }
    }
}
