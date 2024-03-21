package com.bigwillc.cfrpccore.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bigwillc.cfrpccore.annotation.CFConsumer;
import com.bigwillc.cfrpccore.api.RpcResponse;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static com.bigwillc.cfrpccore.util.TypeUtils.cast;

/**
 * @author bigwillc on 2024/3/13
 */
public class MethodUtils {

//    public static boolean checkLocalMethod(Method method) {
//        return false;
//    }

    public static String methodSign(Method method) {
        StringBuilder sb = new StringBuilder(method.getName());
        sb.append("@").append(method.getParameterCount());
        Arrays.stream(method.getParameterTypes()).forEach(
                c -> sb.append("_").append(c.getCanonicalName())
        );

        return sb.toString();
    }

    public static boolean checkLocalMethod(Method method) {
        return false;
    }

//    public static String methodSign(Method method, Class cls) {
//        return null;
//    }
//
//    public static void main(String[] args) {
//
//        Arrays.stream(MethodUtils.class.getMethods()).forEach(
//                m -> System.out.println(methodSign(m)));
//
//    }


    public static List<Field> findAnnotatedFields(Class<?> aClass, Class<? extends Annotation> annotationClass) {
        List<Field> result = new ArrayList<>();
//        Field[] fields = aClass.getDeclaredFields(); 这个类是被代理过的, 增强的子类
        while (aClass != null) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(annotationClass)) {
                    result.add(field);
                }
            }
            aClass = aClass.getSuperclass();
        }
        return result;
    }



    @Nullable
    public static Object castMethodResult(Method method, Object data) {
        Class<?> type = method.getReturnType();
        System.out.println("method.getReturnType() = " + type);

        // json
        if (data instanceof JSONObject jsonResult) {
            if(Map.class.isAssignableFrom(type)) {
                Map resultMap = new HashMap();
                Type genericReturnType = method.getGenericReturnType();
                System.out.println(genericReturnType);

                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                    Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
                    System.out.println("keyType = " + keyType);
                    System.out.println("valueType = " + valueType);
                    jsonResult.entrySet().stream().forEach(
                            e->{
                                Object key = cast(e.getKey(), keyType);
                                Object value = cast(e.getValue(), valueType);
                                resultMap.put(key, value);
                            }
                    );
                }
            }

            return jsonResult.toJavaObject(method.getReturnType());
        } else if (data instanceof JSONArray jsonArray) {   // array
            Object[] array = jsonArray.toArray();

            if (type.isArray()) {
                Class<?> componentType = type.getComponentType();
                Object resultArray = Array.newInstance(componentType, array.length);
                for(int i = 0; i < array.length; i++) {
                    Array.set(resultArray, i, cast(array[i], componentType));
                }
                return resultArray;
            } else if (List.class.isAssignableFrom(type)) {
                List<Object> resultList = new ArrayList<>(array.length);
                Type genericReturnType = method.getGenericReturnType();
                System.out.println(genericReturnType);
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Type actualType = parameterizedType.getActualTypeArguments()[0];
                    System.out.println(actualType);
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
            return cast(data, method.getReturnType());
//                return data;
        }
    }

}
