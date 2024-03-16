package com.bigwillc.cfrpccore.api;

import java.util.List;

/**
 * 负载均衡，weightedRP, AWR-自适应
 *
 * avg * 0.3 + last*0.7 = weight
 *
 * @author bigwillc on 2024/3/16
 */
public interface LoadBalancer<T> {

    T choose(List<T> providers);


    LoadBalancer<Object> Default = new LoadBalancer<Object>() {
        @Override
        public Object choose(List<Object> providers) {
            return providers == null || providers.isEmpty() ? null : providers.get(0);
        }
    };

}
