package com.bigwillc.cfrpccore.registry.zk;

import com.bigwillc.cfrpccore.api.RegistryCenter;
import com.bigwillc.cfrpccore.meta.InstanceMeta;
import com.bigwillc.cfrpccore.meta.ServiceMeta;
import com.bigwillc.cfrpccore.registry.ChangeedListener;
import com.bigwillc.cfrpccore.registry.Event;
import lombok.SneakyThrows;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bigwillc on 2024/3/16
 */
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
        System.out.println(" =====> zk client started to server[" + servers + "/"+ root + "]");
        client.start();
    }

    @Override
    public void stop() {
        System.out.println(" =====> zk client stopped...");
        client.close();
        // todo cache close
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            // 创建服务的持久化节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }
            // 创建实例的临时性节点
            String instancePath = servicePath + "/" + instance.toPath();
            System.out.println(" =====> register to zk：" + instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            System.out.println(" =====> unregister from zk：" + instancePath);
            client.delete().quietly().forPath(instancePath);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        String servicePath = "/" + service.toPath();
        try {
            // 获取所有子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            System.out.println(" =====> fetch all from zk：" + servicePath);
            nodes.forEach(System.out::println);

            return mapInstances(nodes);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static List<InstanceMeta> mapInstances(List<String> nodes) {
        List<InstanceMeta> providers = nodes.stream().map(x -> {
            String[] strs = x.split("_");
            return InstanceMeta.http(strs[0], Integer.parseInt(strs[1]));
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
                    System.out.println(" =====> zk subscribe event: " + event);
                    List<InstanceMeta> nodes = fetchAll(service);
                    listener.fire(new Event(nodes));
        });
        cache.start();
    }
}
