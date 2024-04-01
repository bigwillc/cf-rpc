package com.bigwillc.cfrpccore.registry.zk;

import com.alibaba.fastjson.JSON;
import com.bigwillc.cfrpccore.api.RpcException;
import com.bigwillc.cfrpccore.api.RegistryCenter;
import com.bigwillc.cfrpccore.meta.InstanceMeta;
import com.bigwillc.cfrpccore.meta.ServiceMeta;
import com.bigwillc.cfrpccore.registry.ChangeedListener;
import com.bigwillc.cfrpccore.registry.Event;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bigwillc on 2024/3/16
 */
@Slf4j
public class ZkRegistryCenter implements RegistryCenter {

    @Value("${cfrpc.zkServer}")
    String servers;

    @Value("${cfrpc.zkRoot}")
    String root;

    private CuratorFramework client = null;

    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(servers)
                .namespace(root)
                .retryPolicy(retryPolicy)
                .build();
        log.info(" =====> zk client started to server[" + servers + "/"+ root + "]");
        client.start();
    }

    @Override
    public void stop() {
        log.info(" =====> zk client stopped...");
        client.close();
        // todo cache close
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            // 创建服务的持久化节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, service.toMetas().getBytes());
            }
            // 创建实例的临时性节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info(" =====> register to zk：" + instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, instance.toMetas().getBytes());
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            // 判断服务是否存在
            if(client.checkExists().forPath(servicePath) == null) {
                return;
            }
            String instancePath = servicePath + "/" + instance.toPath();
            log.info(" =====> unregister from zk：" + instancePath);
            client.delete().quietly().forPath(instancePath);
        }catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        String servicePath = "/" + service.toPath();
        try {
            // 获取所有子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            log.info(" =====> fetch all from zk：" + servicePath);
            return mapInstances(nodes, servicePath);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @NotNull
    private List<InstanceMeta> mapInstances(List<String> nodes, String servicePath) {
        List<InstanceMeta> providers = nodes.stream().map(x -> {
            String[] strs = x.split("_");
            InstanceMeta instance = InstanceMeta.http(strs[0], Integer.valueOf(strs[1]));
            log.info(" ===> instance: {}", instance.toUrl());
            String nodePath = servicePath + "/" + x;
            byte[] bytes;
            try {
                bytes = client.getData().forPath(nodePath);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            HashMap params = JSON.parseObject(new String(bytes), HashMap.class);
            params.forEach((k, v) -> log.info(k + "->" + v));
            instance.setParameters(params);
            return instance;
        }).collect(Collectors.toList());
        return providers;
    }

    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangeedListener listener) {
        final TreeCache cache = TreeCache.newBuilder(client, "/" + service.toPath())
                .setCacheData(true).setMaxDepth(2).build();
        cache.getListenable().addListener(
                (client, event) -> {
                    // 有任何节点变动这里会执行
                    log.info(" =====> zk subscribe event: " + event);
                    List<InstanceMeta> nodes = fetchAll(service);
                    listener.fire(new Event(nodes));
        });
        cache.start();
    }
}
