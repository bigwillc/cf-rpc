package com.bigwillc.cfrpccore.consumer;

import com.bigwillc.cfrpccore.api.*;
import com.bigwillc.cfrpccore.consumer.http.OkHttpInvoker;
import com.bigwillc.cfrpccore.meta.InstanceMeta;
import com.bigwillc.cfrpccore.util.MethodUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.net.SocketTimeoutException;
import java.util.*;

import static com.bigwillc.cfrpccore.util.TypeUtils.cast;

/**
 * @author bigwillc on 2024/3/10
 */
@Slf4j
public class CFInvocationHandler implements InvocationHandler {

    Class<?> service;
    RpcContext context;
    List<InstanceMeta> providers;

    HttpInvoker httpInvoker;


    public CFInvocationHandler(Class<?> service, RpcContext context, List<InstanceMeta> providers) {
        this.service = service;
        this.context = context;
        this.providers = providers;
        int timeout = Integer.parseInt(context.getParameters().getOrDefault("app.timeout", "1000"));
        this.httpInvoker = new OkHttpInvoker(timeout);
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



        // 添加超时重试机制
        int retries = Integer.parseInt(context.getParameters().getOrDefault("app.retries", "1"));
        while (retries-- > 0) {

            log.debug(" ===> retries : " + retries);

            try {
                // [
                for (Filter filter : this.context.getFilters()) {
                    Object preResult = filter.preFilter(rpcRequest);
                    if (preResult != null) {
                        log.debug(filter.getClass().getName() + " ===> prefilter " + preResult);
                        return preResult;
                    }
                }

                // [[
                List<InstanceMeta> instances = context.getRouter().route(providers);
                InstanceMeta instance = context.getLoadBalancer().choose(instances);
                log.debug(" ===> loadBalancer choose instance: " + instance);

                // 实现http请求
                RpcResponse<?> rpcResponse = httpInvoker.post(rpcRequest, instance.toUrl());
                Object result = castReturnResult(method, rpcResponse);
                // ]]

                // 这里拿到的可能不是最终值，需要再设计一下
                for (Filter filter : this.context.getFilters()) {
                    Object filterResult = filter.postFilter(rpcRequest, rpcResponse, result);
                    if (filterResult != null) {
                        return filterResult;
                    }
                }
            } catch (Exception ex) {
                if (!(ex.getCause() instanceof SocketTimeoutException)) {
                    throw ex;
                }
            }
        }
        //  ]
        return null;
    }

    @Nullable
    private static Object castReturnResult(Method method, RpcResponse<?> rpcResponse) {
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return MethodUtils.castMethodResult(method, data);

        } else {
            Exception ex = rpcResponse.getEx();
//            ex.printStackTrace();
//            throw new RuntimeException(ex);
            if (ex instanceof RpcException exception) {
                throw exception;
            } else {
                throw new RpcException(ex, RpcException.NoSuchMethodEx);
            }
        }
    }

}
