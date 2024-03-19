package com.bigwillc.cfrpccore.consumer;

import com.bigwillc.cfrpccore.api.LoadBalancer;
import com.bigwillc.cfrpccore.api.RegistryCenter;
import com.bigwillc.cfrpccore.api.Router;
import com.bigwillc.cfrpccore.cluster.RandomLoadBalancer;
import com.bigwillc.cfrpccore.cluster.RoundRibbonLoadBalancer;
import com.bigwillc.cfrpccore.provider.ProviderBootstrap;
import com.bigwillc.cfrpccore.registry.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * @author bigwillc on 2024/3/10
 */
@Configuration
public class ConsumerConfig {

    @Value("${cfrpc.services}")
    String services;

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

    @Bean
    public LoadBalancer loadBalancer() {
//        return LoadBalancer.Default;
//        return new RandomLoadBalancer();
        return new RoundRibbonLoadBalancer();
    }


    @Bean
    public Router router() {
        return Router.Default;
    }


    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter consumer_rc() {
        return new ZkRegistryCenter();
    }

}
