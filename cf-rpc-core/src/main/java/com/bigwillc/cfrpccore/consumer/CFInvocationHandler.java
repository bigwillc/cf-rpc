package com.bigwillc.cfrpccore.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bigwillc.cfrpccore.api.*;
import com.bigwillc.cfrpccore.consumer.http.OkHttpInvoker;
import com.bigwillc.cfrpccore.meta.InstanceMeta;
import com.bigwillc.cfrpccore.util.MethodUtils;
import com.bigwillc.cfrpccore.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

        if (MethodUtils.checkLocalMethod(method.getName())) {
            return null;
        }

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.methodSign(method));
        rpcRequest.setArgs(args);

        for (Filter filter : this.context.getFilters()) {
            Object preResult = filter.preFilter(rpcRequest);
            if (preResult != null) {
//                log.info(filter.getClass().getName() + " ===> prefilter " + preResponse);
                return preResult;
            }
        }

        List<InstanceMeta> instances = context.getRouter().route(providers);
        InstanceMeta instance = context.getLoadBalancer().choose(instances);
        log.debug(" ===> loadBalancer choose instance: " + instance);

        // 实现http请求
        RpcResponse<?> rpcResponse = httpInvoker.post(rpcRequest, instance.toUrl());

        Object result = castReturnResult(method, rpcResponse);

        // 这里拿到的可能不是最终值，需要再设计一下
        for (Filter filter : this.context.getFilters()) {
            Object filterResult = filter.postFilter(rpcRequest, rpcResponse, result);
            if (filterResult != null) {

            }
        }


        return castReturnResult(method, rpcResponse);
    }

    @Nullable
    private static Object castReturnResult(Method method, RpcResponse<?> rpcResponse) {
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
