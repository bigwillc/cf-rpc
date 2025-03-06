package com.bigwillc.cfrpccore.provider;

import com.alibaba.fastjson.JSONObject;
import com.bigwillc.cfrpccore.api.RpcException;
import com.bigwillc.cfrpccore.api.RpcRequest;
import com.bigwillc.cfrpccore.api.RpcResponse;
import com.bigwillc.cfrpccore.meta.ProviderMeta;
import com.bigwillc.cfrpccore.ratelimiter.RateLimiter;
import com.bigwillc.cfrpccore.ratelimiter.RateLimiterFactory;
import com.bigwillc.cfrpccore.util.TypeUtils;
import jakarta.annotation.Resource;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * @author bigwillc on 2024/3/20
 */
public class ProviderInvoker {

    private MultiValueMap<String, ProviderMeta> skeleton;

//    private RateLimiterFactory rateLimiterFactory;


    public ProviderInvoker(ProviderBootstrap providerBootstrap) {
        this.skeleton = providerBootstrap.getSkeleton();
//        this.rateLimiterFactory = providerBootstrap.getRateLimiterFactory();
    }


    public RpcResponse<Object> invoke(RpcRequest request) {

        RpcResponse<Object> rpcResponse = new RpcResponse();
        rpcResponse.setId(request.getId());
        String service = request.getService();
        List<ProviderMeta> providerMetas = skeleton.get(service);

//        RateLimiter rateLimiter = rateLimiterFactory.getRateLimiter(service);
//        if (rateLimiter.isEnabled(service) && !rateLimiter.tryAcquire(service)) {
//            rpcResponse.setStatus(false);
//            rpcResponse.setEx(new RpcException("Rate limit exceeded for service: " + service));
//            return rpcResponse;
//        }

        try {

            ProviderMeta meta = findProviderMeta(providerMetas, request.getMethodSign());
            Method method = meta.getMethod();
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes(), method.getGenericParameterTypes());
//            Object[] args = processArgs(request.getArgs(), method.getParameterTypes(), method.getGenericParameterTypes());
            Object result = method.invoke(meta.getServiceImpl(), args);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (InvocationTargetException e) {
//            e.printStackTrace();
            rpcResponse.setEx(new RpcException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
//            e.printStackTrace();
            rpcResponse.setEx(new RpcException(e.getMessage()));
        }
        return rpcResponse;
    }

    // RpcRequest 里面的arg参数是object 对象，需要转换成对应的类型
    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes, Type[] genericParameterTypes) {

        if(args == null || args.length == 0){
            return args;
        }

        Object[] actuals = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
//            actuals[i] = TypeUtils.castGeneric(args[i], parameterTypes[i], genericParameterTypes[i]);

            // 使用fastjson 将
            actuals[i] = JSONObject.parseObject(JSONObject.toJSONString(args[i]), genericParameterTypes[i]);
        }

        return actuals;
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {

        Optional<ProviderMeta> optional = providerMetas.stream().filter(
                x -> x.getMethodSign().equals(methodSign)
        ).findFirst();
        return optional.orElse(null);
    }

}
