package com.bigwillc.cfrpccore.provider;


import com.bigwillc.cfrpccore.annotation.CFProvider;
import com.bigwillc.cfrpccore.api.RegistryCenter;
import com.bigwillc.cfrpccore.consumer.netty.server.NettyRpcServer;
import com.bigwillc.cfrpccore.meta.InstanceMeta;
import com.bigwillc.cfrpccore.meta.ProviderMeta;
import com.bigwillc.cfrpccore.meta.ServiceMeta;
import com.bigwillc.cfrpccore.registry.zk.ZkRegistryCenter;
import com.bigwillc.cfrpccore.util.MethodUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.*;

/**
 * 服务提供者启动类
 *
 */
@Data
@Slf4j
public class ProviderBootstrap implements ApplicationContextAware {


//    @Value("${registry.center}")
//    private String registryCenter;

    ApplicationContext applicationContext;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    RegistryCenter rc;

    private InstanceMeta instance;

    @Value("${cfrpc.protocol:http}")
    private String rpcProtocol;

    @Value("${server.port}")
    private String port;

    @Value("${netty.server.port}")
    private String nettyPort;

    @Value("${app.id}")
    private String app;

    @Value("${app.namespace}")
    private String namespace;

    @Value("${app.env}")
    private String env;

    @Value("#{${app.metas}}")
    Map<String, String> metas;

    @SneakyThrows
    @PostConstruct // 加载的时候，spring 还未初始化完成
    public void init() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(CFProvider.class);
        providers.forEach((x, y) -> log.info(x));
        rc = applicationContext.getBean(RegistryCenter.class);
//		skeleton.putAll(providers);
        // x 是bean 的名字，不是接口
        providers.values().forEach(this::genInterface);

    }

    // 使用bean 加载，延迟服务暴露
    @SneakyThrows
    public void start() {
        String ip = InetAddress.getLocalHost().getHostAddress();
        String instancePort = "http".equals(rpcProtocol) ? port : nettyPort;
        instance = InstanceMeta.http(ip, Integer.parseInt(instancePort));
        instance.getParameters().putAll(this.metas);
        rc.start();
        skeleton.keySet().forEach(this::registerService);
    }

    @PreDestroy
    public void stop() {
        log.info(" ====> stop all services");
        skeleton.keySet().forEach(this::unregisterService);
        rc.stop();
    }



    private void unregisterService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder().app(app).namespace(namespace).env(env).name(service).build();
        rc.unregister(serviceMeta, instance);
    }

    private void registerService(String service){
        ServiceMeta serviceMeta = ServiceMeta.builder().app(app).namespace(namespace).env(env).name(service).build();
        rc.register(serviceMeta, instance);
    }


    private void genInterface(Object impl) {

        Arrays.stream((impl.getClass().getInterfaces())).forEach(
                server -> {
                    Method[] methods = server.getMethods();
                    for (Method method : methods){
                        if(MethodUtils.checkLocalMethod(method.getName())){
                            continue;
                        }
                        createProvider(server, impl, method);
                    }
                }
        );

        // 获取接口 这里默认指支持一个接口
//        Class<?> itfer = x.getClass().getInterfaces()[0];
//        Method[] methods = itfer.getMethods();
//        for (Method method : methods) {
//            if(MethodUtils.checkLocalMethod(method)){
//                continue;
//            }
//
//            createProvider(itfer, x, method);
//
//            ProviderMeta providerMeta = new ProviderMeta();
//            providerMeta.setMethod(method);
//            providerMeta.setMethodSign(method.getName());
//            providerMeta.setServiceImpl(x);
//            skeleton.add(itfer.getCanonicalName(), providerMeta);
//        }
    }

    private void createProvider(Class<?> service, Object impl, Method method) {

        ProviderMeta meta = ProviderMeta.builder().method(method)
                .methodSign(MethodUtils.methodSign(method))
                .serviceImpl(impl).build();
        log.info(" create a provider: " + meta);
        skeleton.add(service.getCanonicalName(), meta);
    }



}
