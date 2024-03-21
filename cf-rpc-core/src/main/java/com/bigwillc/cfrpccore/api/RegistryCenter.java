package com.bigwillc.cfrpccore.api;

import com.bigwillc.cfrpccore.meta.InstanceMeta;
import com.bigwillc.cfrpccore.meta.ServiceMeta;
import com.bigwillc.cfrpccore.registry.ChangeedListener;

import java.util.List;

/**
 * @author bigwillc on 2024/3/16
 */
public interface RegistryCenter {

    void start();

    void stop();

    // provider侧
    void register(ServiceMeta serviceName, InstanceMeta instance);
    void unregister(ServiceMeta serviceName, InstanceMeta instance);

    // consumer侧
    List<InstanceMeta> fetchAll(ServiceMeta serviceName);

    void subscribe(ServiceMeta service, ChangeedListener listener);
//    void unsubscribe();
//    void headbeat();

    class StaticRegisterCenter implements RegistryCenter {

        List<InstanceMeta> providers;

        public StaticRegisterCenter(List<InstanceMeta> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {
            System.out.println("StaticRegisterCenter start");
        }

        @Override
        public void stop() {
            System.out.println("StaticRegisterCenter stop");
        }

        @Override
        public void register(ServiceMeta serviceName, InstanceMeta instance) {
            System.out.println("StaticRegisterCenter register");
        }

        @Override
        public void unregister(ServiceMeta serviceName, InstanceMeta instance) {
            System.out.println("StaticRegisterCenter unregister");
        }

        @Override
        public List<InstanceMeta> fetchAll(ServiceMeta serviceName) {
            System.out.println("StaticRegisterCenter fetchAll");
            return providers;
        }

        @Override
        public void subscribe(ServiceMeta service, ChangeedListener listener) {
            System.out.println("StaticRegisterCenter subscribe");
        }
    }

}
