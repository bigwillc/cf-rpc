package com.bigwillc.cfrpccore.consumer;

import com.bigwillc.cfrpccore.api.*;
import com.bigwillc.cfrpccore.governance.SlidingTimeWindow;
import com.bigwillc.cfrpccore.meta.InstanceMeta;
import com.bigwillc.cfrpccore.protocol.RpcInvoker;
import com.bigwillc.cfrpccore.protocol.InvokerFactory;
import com.bigwillc.cfrpccore.util.MethodUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.bigwillc.cfrpccore.util.TypeUtils.cast;
import static com.bigwillc.cfrpccore.util.TypeUtils.castMethodResult;

/**
 * @author bigwillc on 2024/3/10
 */
@Slf4j
public class CFInvocationHandler implements InvocationHandler {

    private final Class<?> service;
    private final RpcContext context;
    private final List<InstanceMeta> providers;

    private final CopyOnWriteArrayList<InstanceMeta> isolatedProviders = new CopyOnWriteArrayList<>();

    private final ConcurrentHashMap<String, SlidingTimeWindow> windows = new ConcurrentHashMap<>();

    private final RpcInvoker rpcInvoker;

    private final CopyOnWriteArrayList<InstanceMeta> halfOpenProviders = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService executor;

    private final ReentrantReadWriteLock instancesLock = new ReentrantReadWriteLock();

    private final AtomicInteger currentHalfOpenChecks = new AtomicInteger(0);

    private final int maxConcurrentHalfOpenChecks = 3;

    private final int failureThreshold;

    public CFInvocationHandler(Class<?> service, RpcContext context, List<InstanceMeta> providers, String protocol) {
        this.service = service;
        this.context = context;
        this.providers = providers;

        Map<String, String> parameters = context.getParameters();
        int timeout = Integer.parseInt(context.getParameters().getOrDefault("app.timeout", "5000"));
        this.failureThreshold = Integer.parseInt(context.getParameters().getOrDefault("app.failure.threshold", "50"));

        this.rpcInvoker = InvokerFactory.createInvoker(protocol, timeout);

        this.executor = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r, "half-open-scheduler");
            thread.setDaemon(true);
            return thread;
        });

        this.executor.scheduleWithFixedDelay(this::halfOpen, 10, 60, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void halfOpen() {
        if (isolatedProviders.isEmpty()) {
            return;
        }
        log.debug(" ===> half open isolated providers: " + halfOpenProviders);

        List<InstanceMeta> candidates = new ArrayList<>(isolatedProviders);
        halfOpenProviders.clear();
        halfOpenProviders.addAll(candidates);
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

        Map<String, String> parameters = context.getParameters();
        // 添加超时重试机制
        int retries = Integer.parseInt(parameters.getOrDefault("app.retries", "1"));

        for (Filter filter : this.context.getFilters()) {
            Object preResult = filter.preFilter(rpcRequest);
            if (preResult != null) {
                log.debug(filter.getClass().getName() + " ===> prefilter " + preResult);
                return preResult;
            }
        }

        Exception lastException = null;
        while (retries-- > 0) {
            log.debug(" ===> retries remaining: {} ", retries);

            try {
                InstanceMeta instance = selectInstance();
                if (instance == null) {
                    throw new RpcException("No available instance", RpcException.NoProviderEx);
                }

                try {
                    RpcResponse<?> rpcResponse = rpcInvoker.post(rpcRequest, instance.toUrl());
                    Object result = castReturnResult(method, rpcResponse);

                    // If we get here, the call succeeded, so recover the instance if needed
                    recoverInstanceIfNeeded(instance);

                    // Apply post filters
                    for (Filter filter : this.context.getFilters()) {
                        Object filterResult = filter.postFilter(rpcRequest, rpcResponse, result);
                        if (filterResult != null) {
                            return filterResult;
                        }
                    }

                    return result;
                } catch(Exception e) {
                    log.error("RPC call failed", e);
                    lastException = e;
                    recordFailure(instance, e);

                    // Only retry on timeout exceptions
                    if (!(e.getCause() instanceof SocketTimeoutException)) {
                        throw e;
                    }
                }


            } catch (Exception ex) {
                lastException = ex;
                if (!(ex.getCause() instanceof SocketTimeoutException)) {
                    throw ex;
                }
            }
        }

        // If we get here, all retries failed
        if (lastException != null) {
            throw lastException;
        }

        return null;
    }

    /**
     * Select an instance for the RPC call with non-blocking approach
     */
    private InstanceMeta selectInstance() {
        // First try to get a half-open instance for testing
        InstanceMeta halfOpenInstance = getHalfOpenInstance();
        if (halfOpenInstance != null) {
            return halfOpenInstance;
        }

        // Otherwise, use regular routing and load balancing
        if (providers.isEmpty()) {
            return null;
        }

        List<InstanceMeta> instances = context.getRouter().route(providers);
        if (instances.isEmpty()) {
            return null;
        }

        return context.getLoadBalancer().choose(instances);
    }

    private InstanceMeta getHalfOpenInstance() {
        // Limit the number of concurrent half-open checks
        if (currentHalfOpenChecks.get() >= maxConcurrentHalfOpenChecks) {
            return null;
        }

        // Try to get a half-open instance
        InstanceMeta instance = null;
        if (!halfOpenProviders.isEmpty()) {
            // Non-blocking removal from the list
            int size = halfOpenProviders.size();
            if (size > 0 && currentHalfOpenChecks.incrementAndGet() <= maxConcurrentHalfOpenChecks) {
                try {
                    if (!halfOpenProviders.isEmpty()) {
                        instance = halfOpenProviders.remove(0);
                        log.debug(" ===> check alive instance: {}", instance);
                    }
                } catch (IndexOutOfBoundsException e) {
                    // List might have been modified concurrently, just ignore and continue
                } finally {
                    if (instance == null) {
                        currentHalfOpenChecks.decrementAndGet();
                    }
                }
            } else {
                currentHalfOpenChecks.decrementAndGet();
            }
        }

        return instance;
    }


    /**
     * Record a failure for an instance and isolate if necessary
     */
    private void recordFailure(InstanceMeta instance, Exception exception) {
        if(exception != null && exception.getCause() instanceof RpcException){
            RpcException rpcException = (RpcException) exception.getCause();
            if(rpcException.getErrcode().equals(RpcException.RateLimiterEx)){
                return;
            }
        }

        String url = instance.toUrl();

        // Get or create the sliding window for this URL
        SlidingTimeWindow window = windows.computeIfAbsent(url, k -> new SlidingTimeWindow());

        // Record the failure
        window.record(System.currentTimeMillis());
        log.debug("===>instance {} in window with {}", url, window.getSum());

        // Check if we need to isolate this instance
        if (window.getSum() > failureThreshold) {
            isolate(instance);
        }
    }


    /**
     * Recover an instance if it was previously isolated
     */
    private void recoverInstanceIfNeeded(InstanceMeta instance) {
        // Update instance health information
        if (halfOpenProviders.remove(instance)) {
            currentHalfOpenChecks.decrementAndGet();
        }

        // Check if this instance needs to be recovered
        if (!providers.contains(instance)) {
            // Reset the window counter for this instance
            windows.remove(instance.toUrl());

            // Remove from isolated and add to active
            isolatedProviders.remove(instance);
            providers.add(instance);

            log.debug(" ===> instance {} is recovered, isolatedProvider={}, providers={} ",
                    instance, isolatedProviders, providers);
        }
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
            return castMethodResult(method, data);
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

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

}
