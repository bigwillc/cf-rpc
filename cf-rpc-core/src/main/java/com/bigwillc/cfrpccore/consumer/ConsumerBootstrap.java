package com.bigwillc.cfrpccore.consumer;

import com.bigwillc.cfrpccore.annotation.CFConsumer;
import com.bigwillc.cfrpccore.api.LoadBalancer;
import com.bigwillc.cfrpccore.api.RegistryCenter;
import com.bigwillc.cfrpccore.api.Router;
import com.bigwillc.cfrpccore.api.RpcContext;
import com.bigwillc.cfrpccore.registry.ChangeedListener;
import com.bigwillc.cfrpccore.registry.Event;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bigwillc on 2024/3/10
 */
@Data
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

    ApplicationContext applicationContext;
    Environment environment;

    private Map<String, Object> stub = new HashMap<>();

    public void start(){

        Router router = applicationContext.getBean(Router.class);
        LoadBalancer loadBalancer = applicationContext.getBean(LoadBalancer.class);
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);

        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);

//        String urls = environment.getProperty("cfrpc.providers", "");
//        if (Strings.isEmpty(urls)) {
//            System.out.println("cfrpc.providers is empty");
//            throw new RuntimeException("providers is empty");
//        }


        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
//            System.out.println("====> " + name);
            Object bean = applicationContext.getBean(name);

//            if (!name.contains("cfRpcDemoConsumerApplication")) {
//                return;
//            }
            List<Field> fields = findAnnotatedFields(bean.getClass());

           fields.stream().forEach(f->{
               System.out.println("====> " + f.getName());
               try {
                   Class<?> service = f.getType();
                   String serviceName = service.getCanonicalName();
                   Object consumer = stub.get(serviceName);
                   if (consumer == null) {
                       consumer = createConsumerFromRegisty(service, context, rc);
                   //createConsumer(service, context, List.of(providers));
                   }
                   // 可见性设置成true
                   f.setAccessible(true);
                   // 往bean 里面注入代理对象
                   f.set(bean, consumer);
               }catch (Exception e){
                   e.printStackTrace();
                   throw new RuntimeException(e);
               }

           });
        }
    }

    private Object createConsumerFromRegisty(Class<?> service, RpcContext context, RegistryCenter rc) {
        String serviceName = service.getCanonicalName();
        List<String> providers = rc.fetchAll(serviceName).stream()
                .map(x -> "http://" + x.replace('_', ':')).collect(Collectors.toList());
        System.out.println(" =====> map to providers: " + providers);
        providers.forEach(System.out::println);

        rc.subscribe(serviceName, event -> {
            providers.clear();
            providers.addAll(mapUrl(event.getData()));
        });

        return createConsumer(service, context, providers);
    }

    private List<String> mapUrl(List<String> urls) {
        return urls.stream().map(x -> "http://" + x.replace('_', ':')).collect(Collectors.toList());
    }

    private Object createConsumer(Class<?> service, RpcContext rpcContext, List<String> providers) {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new CFInvocationHandler(service, rpcContext, providers));
    }

    private List<Field> findAnnotatedFields(Class<?> aClass) {
        List<Field> result = new ArrayList<>();
//        Field[] fields = aClass.getDeclaredFields(); 这个类是被代理过的, 增强的子类
        while (aClass != null) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(CFConsumer.class)) {
                    result.add(field);
                }
            }
            aClass = aClass.getSuperclass();
        }
        return result;
    }


}
