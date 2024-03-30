package com.bigwillc.cfrpccore.consumer;

import com.bigwillc.cfrpccore.api.*;
import com.bigwillc.cfrpccore.consumer.http.OkHttpInvoker;
import com.bigwillc.cfrpccore.governance.SlidingTimeWindow;
import com.bigwillc.cfrpccore.meta.InstanceMeta;
import com.bigwillc.cfrpccore.util.MethodUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.bigwillc.cfrpccore.util.TypeUtils.cast;

/**
 * @author bigwillc on 2024/3/10
 */
@Slf4j
public class CFInvocationHandler implements InvocationHandler {

    Class<?> service;
    RpcContext context;
    List<InstanceMeta> providers;

    List<InstanceMeta> isolatedProviders = new ArrayList<>();

    Map<String, SlidingTimeWindow> windows = new HashMap<>();

    HttpInvoker httpInvoker;

    List<InstanceMeta> halfOpenProviders = new ArrayList<>();

    ScheduledExecutorService executor;

    public CFInvocationHandler(Class<?> service, RpcContext context, List<InstanceMeta> providers) {
        this.service = service;
        this.context = context;
        this.providers = providers;
        int timeout = Integer.parseInt(context.getParameters().getOrDefault("app.timeout", "1000"));
        this.httpInvoker = new OkHttpInvoker(timeout);
        this.executor = Executors.newScheduledThreadPool(1);
        this.executor.scheduleWithFixedDelay(this::halfOpen, 10, 60, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void halfOpen() {
        log.debug(" ===> half open isolated providers: " + halfOpenProviders);
        halfOpenProviders.clear();
        halfOpenProviders.addAll(isolatedProviders);
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

                InstanceMeta instance;
                synchronized (halfOpenProviders) {
                    if (halfOpenProviders.isEmpty()) {
                        List<InstanceMeta> instances = context.getRouter().route(providers);
                        instance = context.getLoadBalancer().choose(instances);
                        log.debug(" ===> loadBalancer choose instance: {}", instance);
                    } else {
                        instance = halfOpenProviders.remove(0);
                        log.debug(" ===> check alive instance: {}", instance);
                    }
                }

                RpcResponse<?> rpcResponse;
                Object result;

                String url = instance.toUrl();
                try {
                    // 实现http请求
                    rpcResponse = httpInvoker.post(rpcRequest, instance.toUrl());
                    result = castReturnResult(method, rpcResponse);

                }catch (Exception e) {
                    // 故障的规则统计和隔离
                    // 每一次异常，记录一次，统计30s的异常数

                    // todo 可以加上 synchronized 关键字控制并发
                    SlidingTimeWindow window = windows.get(url);
                    if (window == null) {
                        window = new SlidingTimeWindow();
                        windows.put(url, window);
                    }

                    window.record(System.currentTimeMillis());
                    log.debug("===>instance {} in window with {}", url, window.getSum());
                    // 发生了10次，就做故障隔离
                    if ( window.getSum() > 10) {
                       isolate(instance);
                    }

                    throw e;
                }

                synchronized (providers) {
                    if (!providers.contains(instance)) {
                        isolatedProviders.remove(instance);
                        providers.add(instance);
                        log.debug(" ===> instance {} is recovered, isolatedProvider={}, providers={} ", instance, isolatedProviders, providers);
                    }
                }

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

    private void isolate(InstanceMeta instance) {
        log.debug(" ===> isolate instance: {}", instance);
        providers.remove(instance);
        log.debug(" ===> providers: {}", providers);
        isolatedProviders.add(instance);
        log.debug(" ===> isolatedProviders: {}", isolatedProviders);
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
