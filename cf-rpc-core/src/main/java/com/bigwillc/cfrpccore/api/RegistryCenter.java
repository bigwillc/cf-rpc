package com.bigwillc.cfrpccore.api;

import com.bigwillc.cfrpccore.meta.InstanceMeta;
import com.bigwillc.cfrpccore.meta.ServiceMeta;
import com.bigwillc.cfrpccore.registry.ChangeedListener;
import lombok.extern.slf4j.Slf4j;

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

    @Slf4j
    class StaticRegisterCenter implements RegistryCenter {

        List<InstanceMeta> providers;

        public StaticRegisterCenter(List<InstanceMeta> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {
            log.info("StaticRegisterCenter start");
        }

        @Override
        public void stop() {
            log.info("StaticRegisterCenter stop");
        }

        @Override
        public void register(ServiceMeta serviceName, InstanceMeta instance) {
            log.info("StaticRegisterCenter register");
        }

        @Override
        public void unregister(ServiceMeta serviceName, InstanceMeta instance) {
            log.info("StaticRegisterCenter unregister");
        }

        @Override
        public List<InstanceMeta> fetchAll(ServiceMeta serviceName) {
            log.info("StaticRegisterCenter fetchAll");
            return providers;
        }

        @Override
        public void subscribe(ServiceMeta service, ChangeedListener listener) {
            log.info("StaticRegisterCenter subscribe");
        }
    }

}
