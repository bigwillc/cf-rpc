package com.bigwillc.cfrpccore.util;

import java.lang.reflect.Method;
import java.util.Arrays;

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



}
