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


    LoadBalancer<?> Default = p -> (p == null || p.isEmpty()) ? null : p.get(0);

}
