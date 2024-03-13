package com.bigwillc.cfrpccore.consumer;

import com.bigwillc.cfrpccore.annotation.CFConsumer;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bigwillc on 2024/3/10
 */
@Data
public class ConsumerBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private Map<String, Object> stub = new HashMap<>();

    public void start(){
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
                       consumer = createConsumer(service);
                   }
                   // 可见性设置成true
                   f.setAccessible(true);
                   f.set(bean, consumer);
               }catch (Exception e){
                   e.printStackTrace();
                   throw new RuntimeException(e);
               }

           });
        }
    }

    private Object createConsumer(Class<?> service) {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new CFInvocationHandler(service));
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
