package com.bigwillc.cfrpccore.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author bigwillc on 2024/3/13
 */
@Slf4j
public class TypeUtils {

    // 假设我们能够以某种方式传递元素类型
    public static <T> Object cast(Object source, Class<?> targetType, Class<?>... elementTypes) {
        // 如果源对象为null，直接返回null
        if (source == null) {
            return null;
        }

        // 如果目标类型是List且我们知道元素类型
        if (List.class.isAssignableFrom(targetType) && elementTypes.length > 0) {
            Class<?> elementType = elementTypes[0]; // 取第一个元素类型为List的元素类型
            return convertToList(source, elementType);
        }

        // 对于其他类型，可以在这里扩展逻辑，例如基本类型转换等
        // ...

        // 如果无法识别类型，返回原对象
        return source;
    }

    // 转换Object到List<T>
    private static <T> List<T> convertToList(Object source, Class<T> elementType) {
        // 如果源对象已经是JSON字符串，则直接转换
        if (source instanceof String) {
            return JSONArray.parseArray((String) source, elementType);
        }

        // 如果源对象是List类型，尝试转换每个元素
        if (source instanceof List) {
            List<?> sourceList = (List<?>) source;
            List<T> targetList = new ArrayList<>(sourceList.size());
            for (Object item : sourceList) {
                T targetItem = JSON.parseObject(JSON.toJSONString(item), elementType);
                targetList.add(targetItem);
            }
            return targetList;
        }

        // 其他情况，直接使用JSON转换
        String jsonString = JSON.toJSONString(source);
        return JSONArray.parseArray(jsonString, elementType);
    }

    public static Object cast(Object origin, Class<?> type) {
        if(origin== null) {
            return null;
        }

        Class<?> aClass = origin.getClass();
        if (type.isAssignableFrom(aClass)) {
            return type.cast(origin);
        }

        if (type.isArray()) {
            if(origin instanceof List list) {
                origin = list.toArray();
            }
            int length = Array.getLength(origin);
            Class<?> componentType = type.getComponentType();
            Object resultArray = Array.newInstance(componentType, length);
            log.debug("===> componentType = " + componentType.getCanonicalName());
            for (int i = 0; i < length; i++) {
                // 如果不是基本类型，也不是jdk 类型，递归处理
                if(componentType.isPrimitive() || componentType.getPackageName().startsWith("java")) {
                    Array.set(resultArray, i, Array.get(origin, i));
                } else {
                    Object castObject = cast(Array.get(origin, i), componentType);
                    Array.set(resultArray, i, castObject);
                }
            }
            return resultArray;
        }
//
        if(origin instanceof HashMap map) {
            JSONObject jsonObject = new JSONObject(map);
            return jsonObject.toJavaObject(type);
        }

        if(origin instanceof JSONObject jsonObject) {
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

    public static Object castMethodResult(Method method, Object data) {
        Class<?> type = method.getReturnType();
        log.debug("method.getReturnType() = " + type);
        if (data instanceof JSONObject jsonResult) {
            if (Map.class.isAssignableFrom(type)) {
                Map resultMap = new HashMap();
                Type genericReturnType = method.getGenericReturnType();
                log.debug(genericReturnType.toString());
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Class<?> keyType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
                    Class<?> valueType = (Class<?>)parameterizedType.getActualTypeArguments()[1];
                    log.debug("keyType  : " + keyType);
                    log.debug("valueType: " + valueType);
                    jsonResult.entrySet().stream().forEach(
                            e -> {
                                Object key = cast(e.getKey(), keyType);
                                Object value = cast(e.getValue(), valueType);
                                resultMap.put(key, value);
                            }
                    );
                }
                return resultMap;
            }
            return jsonResult.toJavaObject(type);
        } else if (data instanceof JSONArray jsonArray) {
            Object[] array = jsonArray.toArray();
            if (type.isArray()) {
                Class<?> componentType = type.getComponentType();
                Object resultArray = Array.newInstance(componentType, array.length);
                for (int i = 0; i < array.length; i++) {
                    if (componentType.isPrimitive() || componentType.getPackageName().startsWith("java")) {
                        Array.set(resultArray, i, array[i]);
                    } else {
                        Object castObject = cast(array[i], componentType);
                        Array.set(resultArray, i, castObject);
                    }
                }
                return resultArray;
            } else if (List.class.isAssignableFrom(type)) {
                List<Object> resultList = new ArrayList<>(array.length);
                Type genericReturnType = method.getGenericReturnType();
                log.debug(genericReturnType.toString());
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Type actualType = parameterizedType.getActualTypeArguments()[0];
                    log.debug(actualType.toString());
                    for (Object o : array) {
                        resultList.add(cast(o, (Class<?>) actualType));
                    }
                } else {
                    resultList.addAll(Arrays.asList(array));
                }
                return resultList;
            } else {
                return null;
            }
        } else {
            return cast(data, type);
        }
    }
}
