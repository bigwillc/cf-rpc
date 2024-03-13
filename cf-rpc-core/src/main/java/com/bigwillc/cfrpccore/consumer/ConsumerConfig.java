package com.bigwillc.cfrpccore.consumer;

import com.bigwillc.cfrpccore.provider.ProviderBootstrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author bigwillc on 2024/3/10
 */
@Configuration
public class ConsumerConfig {

    @Bean
    ConsumerBootstrap consumerBootstrap() {
        return new ConsumerBootstrap();
    }

    // spring 上下文全部启动完了以后
    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumerBootstrap_runner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> {
            System.out.println("start consumer");
            consumerBootstrap.start();
            System.out.println("consumer started");
        };
    }

}
