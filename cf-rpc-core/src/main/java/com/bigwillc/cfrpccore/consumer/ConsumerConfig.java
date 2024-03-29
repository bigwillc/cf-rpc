package com.bigwillc.cfrpccore.consumer;

import com.bigwillc.cfrpccore.api.Filter;
import com.bigwillc.cfrpccore.api.LoadBalancer;
import com.bigwillc.cfrpccore.api.RegistryCenter;
import com.bigwillc.cfrpccore.api.Router;
import com.bigwillc.cfrpccore.cluster.RoundRibbonLoadBalancer;
import com.bigwillc.cfrpccore.filter.CacheFilter;
import com.bigwillc.cfrpccore.filter.MockFilter;
import com.bigwillc.cfrpccore.meta.InstanceMeta;
import com.bigwillc.cfrpccore.registry.zk.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author bigwillc on 2024/3/10
 */
@Configuration
@Slf4j
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
            log.info("start consumer");
            consumerBootstrap.start();
            log.info("consumer started");
        };
    }

    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
//        return LoadBalancer.Default;
//        return new RandomLoadBalancer();
        return new RoundRibbonLoadBalancer<>();
    }


    @Bean
    public Router<InstanceMeta> router() {
        return Router.Default;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter consumer_rc() {
        return new ZkRegistryCenter();
    }

    @Bean
    public Filter defaultFilter() {
        return Filter.Default;
    }

//    @Bean
//    public Filter filter1() {
//        return new CacheFilter();
//    }

//    @Bean
//    public Filter filter2() {
//        return new MockFilter();
//    }
}
