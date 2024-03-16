package com.bigwillc.cfrpccore.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bigwillc.cfrpccore.api.*;
import com.bigwillc.cfrpccore.util.MethodUtils;
import com.bigwillc.cfrpccore.util.TypeUtils;
import okhttp3.*;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author bigwillc on 2024/3/10
 */
public class CFInvocationHandler implements InvocationHandler {

    final static MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

    Class<?> service;

    RpcContext context;

    List<String> providers;


    public CFInvocationHandler(Class<?> service, RpcContext context, List<String> providers) {
        this.service = service;
        this.context = context;
        this.providers = providers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String name = method.getName();
        if (name.equals("toString") || name.equals("hashCode") || name.equals("equals")) {
            return null;
        }

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.methodSign(method));
        rpcRequest.setArgs(args);

        List<String> urls = context.getRouter().route(this.providers);
        String url = (String) context.getLoadBalancer().choose(urls);
        System.out.println("loadBalancer choose url: " + url);

        // 实现http请求
        RpcResponse rpcResponse = post(rpcRequest, url);

        if (rpcResponse.isStatus()) {

            Object data = rpcResponse.getData();

            if (data instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) data;
                return jsonObject.toJavaObject(method.getReturnType());
            } else if (data instanceof JSONArray jsonArray) {
                Object[] array = jsonArray.toArray();
                Class<?> componentType = method.getReturnType().getComponentType();
                Object resultArray = Array.newInstance(componentType, array.length);
                System.out.println("===> componentType = " + componentType.getCanonicalName());
                for (int i = 0; i < array.length; i++) {
                    Array.set(resultArray, i, TypeUtils.cast(array[i], componentType));
                }
                return resultArray;
            } else {
                return TypeUtils.cast(data, method.getReturnType());
//                return data;
            }

        } else {
            Exception ex = rpcResponse.getEx();
//            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();
    // url connection 也可以

    private RpcResponse post(RpcRequest rpcRequest, String url) {
        String reqJson = JSON.toJSONString(rpcRequest);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSONTYPE, reqJson))
                .build();
        try {
            System.out.println("===> reqJson = " + reqJson);
            String respJson = client.newCall(request).execute().body().string();
            System.out.println("===> respJson = " + respJson);
            RpcResponse rpcResponse = JSON.parseObject(respJson, RpcResponse.class);
            return rpcResponse;

        }catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
