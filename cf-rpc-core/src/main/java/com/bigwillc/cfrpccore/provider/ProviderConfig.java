package com.bigwillc.cfrpccore.provider;

import com.bigwillc.cfrpccore.api.RegistryCenter;
import com.bigwillc.cfrpccore.registry.zk.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Slf4j
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
            log.info("start consumer");
            providerBootstrap.start();
            log.info("consumer started");
        };
    }

    // 启动的时候会调用start 方法启动，销毁的时候会调用stop
//    @Bean(initMethod = "start", destroyMethod = "stop")
    @Bean
    public RegistryCenter provider_rc() {
        return new ZkRegistryCenter();
    }


}
