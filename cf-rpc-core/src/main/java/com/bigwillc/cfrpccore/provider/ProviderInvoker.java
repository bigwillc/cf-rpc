package com.bigwillc.cfrpccore.provider;

import com.bigwillc.cfrpccore.api.RpcException;
import com.bigwillc.cfrpccore.api.RpcRequest;
import com.bigwillc.cfrpccore.api.RpcResponse;
import com.bigwillc.cfrpccore.meta.ProviderMeta;
import com.bigwillc.cfrpccore.util.TypeUtils;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * @author bigwillc on 2024/3/20
 */
public class ProviderInvoker {

    private MultiValueMap<String, ProviderMeta> skeleton;

    public ProviderInvoker(ProviderBootstrap providerBootstrap) {
        this.skeleton = providerBootstrap.getSkeleton();
    }


    public RpcResponse<Object> invoke(RpcRequest request) {

        RpcResponse<Object> rpcResponse = new RpcResponse();
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());

        try {

            ProviderMeta meta = findProviderMeta(providerMetas, request.getMethodSign());
            Method method = meta.getMethod();
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes());
            Object result = method.invoke(meta.getServiceImpl(), args);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (InvocationTargetException e) {
            rpcResponse.setEx(new RpcException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            rpcResponse.setEx(new RpcException(e.getMessage()));
        }
        return rpcResponse;
    }

    // RpcRequest 里面的arg参数是object 对象，需要转换成对应的类型
    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes) {

        if(args == null || args.length == 0){
            return args;
        }

        Object[] actuals = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            actuals[i] = TypeUtils.cast(args[i], parameterTypes[i]);
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
