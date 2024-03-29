package com.bigwillc.cfrpccore.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bigwillc.cfrpccore.api.*;
import com.bigwillc.cfrpccore.consumer.http.OkHttpInvoker;
import com.bigwillc.cfrpccore.meta.InstanceMeta;
import com.bigwillc.cfrpccore.util.MethodUtils;
import com.bigwillc.cfrpccore.util.TypeUtils;
import okhttp3.*;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.bigwillc.cfrpccore.util.MethodUtils.castMethodResult;
import static com.bigwillc.cfrpccore.util.TypeUtils.cast;

/**
 * @author bigwillc on 2024/3/10
 */
public class CFInvocationHandler implements InvocationHandler {

    Class<?> service;
    RpcContext context;
    List<InstanceMeta> providers;

    HttpInvoker httpInvoker = new OkHttpInvoker();


    public CFInvocationHandler(Class<?> service, RpcContext context, List<InstanceMeta> providers) {
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

        List<InstanceMeta> instances = context.getRouter().route(providers);
        InstanceMeta instance = context.getLoadBalancer().choose(instances);
        System.out.println(" ===> loadBalancer choose instance: " + instance);

        // 实现http请求
        RpcResponse<?> rpcResponse = httpInvoker.post(rpcRequest, instance.toUrl());


        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return MethodUtils.castMethodResult(method, data);

        } else {
            Exception ex = rpcResponse.getEx();
//            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

}
