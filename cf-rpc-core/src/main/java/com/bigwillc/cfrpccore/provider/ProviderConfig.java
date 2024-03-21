package com.bigwillc.cfrpccore.provider;

import com.bigwillc.cfrpccore.api.RegistryCenter;
import com.bigwillc.cfrpccore.registry.zk.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ProviderConfig {

    @Bean
    ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
    }

    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }

    // 延迟服务暴露
    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerBootstrap_runner(@Autowired ProviderBootstrap providerBootstrap) {
        return x -> {
            System.out.println("start consumer");
            providerBootstrap.start();
            System.out.println("consumer started");
        };
    }

    // 启动的时候会调用start 方法启动，销毁的时候会调用stop
//    @Bean(initMethod = "start", destroyMethod = "stop")
    @Bean
    public RegistryCenter provider_rc() {
        return new ZkRegistryCenter();
    }


}
