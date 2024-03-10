package com.bigwillc.cfrpccore.provider;


import com.bigwillc.cfrpccore.annotation.CFProvider;
import com.bigwillc.cfrpccore.api.RpcRequest;
import com.bigwillc.cfrpccore.api.RpcResponse;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Data
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private Map<String, Object> skeleton = new HashMap<>();

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
        skeleton.put(itfer.getCanonicalName(), x);
    }


    public RpcResponse invoke(RpcRequest request) {

        Object bean = skeleton.get(request.getService());
        try {
//			Method method = bean.getClass().getDeclaredMethod(request.getMethod());
            Method method = findMethod(bean.getClass(), request.getMethod());
            Object result = method.invoke(bean, request.getArgs());
            return new RpcResponse(true, result);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
