package com.bigwillc.cfrpccore.consumer;

import com.bigwillc.cfrpccore.annotation.CFConsumer;
import com.bigwillc.cfrpccore.api.*;
import com.bigwillc.cfrpccore.meta.InstanceMeta;
import com.bigwillc.cfrpccore.meta.ServiceMeta;
import com.bigwillc.cfrpccore.registry.ChangeedListener;
import com.bigwillc.cfrpccore.registry.Event;
import com.bigwillc.cfrpccore.util.MethodUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

    ApplicationContext applicationContext;
    Environment environment;

    @Value("${app.id}")
    private String app;

    @Value("${app.namespace}")
    private String namespace;

    @Value("${app.env}")
    private String env;

    @Value("${app.retries}")
    private String retries;

    @Value("${app.timeout}")
    private String timeout;


    private Map<String, Object> stub = new HashMap<>();

    public void start(){

        Router<InstanceMeta> router = applicationContext.getBean(Router.class);
        LoadBalancer<InstanceMeta> loadBalancer = applicationContext.getBean(LoadBalancer.class);
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        List<Filter> filters = applicationContext.getBeansOfType(Filter.class).values().stream().collect(Collectors.toList());

        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        context.getParameters().put("app.retries", retries);
        context.getParameters().put("app.timeout", timeout);

//        String urls = environment.getProperty("cfrpc.providers", "");
//        if (Strings.isEmpty(urls)) {
//            log.info("cfrpc.providers is empty");
//            throw new RuntimeException("providers is empty");
//        }


        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
//            log.info("====> " + name);
            Object bean = applicationContext.getBean(name);

//            if (!name.contains("cfRpcDemoConsumerApplication")) {
//                return;
//            }
            List<Field> fields = MethodUtils.findAnnotatedFields(bean.getClass(), CFConsumer.class);

           fields.stream().forEach(f->{
               log.info("====> " + f.getName());
               try {
                   Class<?> service = f.getType();
                   String serviceName = service.getCanonicalName();
                   Object consumer = stub.get(serviceName);
                   if (consumer == null) {
                       consumer = createConsumerFromRegisty(service, context, rc);
                       stub.put(serviceName, consumer);
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
        ServiceMeta serviceName = ServiceMeta.builder()
                .app(app).namespace(namespace).env(env).name(service.getCanonicalName()).build();
        List<InstanceMeta> providers = rc.fetchAll(serviceName);
        log.info(" =====> map to providers: " + providers);
        providers.forEach(System.out::println);

        rc.subscribe(serviceName, event -> {
            providers.clear();
            providers.addAll(event.getData());
        });

        return createConsumer(service, context, providers);
    }

    private Object createConsumer(Class<?> service, RpcContext rpcContext, List<InstanceMeta> providers) {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new CFInvocationHandler(service, rpcContext, providers));
    }




}
