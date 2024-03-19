package com.bigwillc.cfrpccore.api;

import com.bigwillc.cfrpccore.registry.ChangeedListener;

import java.util.List;

/**
 * @author bigwillc on 2024/3/16
 */
public interface RegistryCenter {

    void start();

    void stop();

    // provider侧
    void register(String serviceName, String instance);
    void unregister(String serviceName, String instance);

    // consumer侧
    List<String> fetchAll(String serviceName);

    void subscribe(String service, ChangeedListener listener);
//    void unsubscribe();
//    void headbeat();

    class StaticRegisterCenter implements RegistryCenter {

        List<String> providers;

        public StaticRegisterCenter(List<String> providers) {
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
        public void register(String serviceName, String instance) {
            System.out.println("StaticRegisterCenter register");
        }

        @Override
        public void unregister(String serviceName, String instance) {
            System.out.println("StaticRegisterCenter unregister");
        }

        @Override
        public List<String> fetchAll(String serviceName) {
            System.out.println("StaticRegisterCenter fetchAll");
            return providers;
        }

        @Override
        public void subscribe(String service, ChangeedListener listener) {
            System.out.println("StaticRegisterCenter subscribe");
        }
    }

}
