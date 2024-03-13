package com.bigwillc.cfrpccore.provider;


import com.bigwillc.cfrpccore.annotation.CFProvider;
import com.bigwillc.cfrpccore.api.RpcRequest;
import com.bigwillc.cfrpccore.api.RpcResponse;
import com.bigwillc.cfrpccore.meta.ProviderMeta;
import com.bigwillc.cfrpccore.util.MethodUtils;
import com.bigwillc.cfrpccore.util.TypeUtils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Data
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    @PostConstruct
    public void buildProviders() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(CFProvider.class);
        providers.forEach((x, y) -> System.out.println(x));
//		skeleton.putAll(providers);

        // x 是bean 的名字，不是接口
        providers.values().forEach(
                x -> genInterface(x)
        );
    }

    private void genInterface(Object x) {
        // 获取接口 这里默认指支持一个接口
        // todo 支持多个接口
        Class<?> itfer = x.getClass().getInterfaces()[0];
        Method[] methods = itfer.getMethods();
        for (Method method : methods) {
            if(MethodUtils.checkLocalMethod(method)){
                continue;
            }

            createProvider(itfer, x, method);

            ProviderMeta providerMeta = new ProviderMeta();
            providerMeta.setMethod(method);
            providerMeta.setMethodSign(method.getName());
            providerMeta.setServiceImpl(x);
            skeleton.add(itfer.getCanonicalName(), providerMeta);
        }
//        skeleton.put(itfer.getCanonicalName(), providerMetaList(methods, x));
    }

    private void createProvider(Class<?> itfer, Object x, Method method) {

        ProviderMeta meta = new ProviderMeta();
        meta.setMethod(method);
        meta.setMethodSign(MethodUtils.methodSign(method));
        meta.setServiceImpl(x);
        System.out.println(" create a provider: " + meta);
        skeleton.add(itfer.getCanonicalName(), meta);
    }


    public RpcResponse invoke(RpcRequest request) {

        String methodName = request.getMethodSign();
        if(methodName.equals("toString") || methodName.equals("hashCode") || methodName.equals("equals")){
            throw new RuntimeException("不支持的方法调用");
        }

        RpcResponse rpcResponse = new RpcResponse();
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());

        try {

            ProviderMeta meta = findProviderMeta(providerMetas, request.getMethodSign());

//			Method method = bean.getClass().getDeclaredMethod(request.getMethod());
            Method method = meta.getMethod();
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes());
            Object result = method.invoke(meta.getServiceImpl(), args);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (InvocationTargetException e) {
            rpcResponse.setEx(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            rpcResponse.setEx(new RuntimeException(e.getMessage()));
        }
        return rpcResponse;
    }

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

    private Method findMethod(Class<?> aClass, String methodName) {
        for (Method method : aClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

}
