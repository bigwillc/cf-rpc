package com.bigwillc.cfrpccore.provider;

import com.bigwillc.cfrpccore.api.RegistryCenter;
import com.bigwillc.cfrpccore.consumer.ConsumerBootstrap;
import com.bigwillc.cfrpccore.registry.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
public class ProviderConfig {

    @Bean
    ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
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
    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter provider_rc() {
        return new ZkRegistryCenter();
    }
}
