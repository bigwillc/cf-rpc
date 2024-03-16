package com.bigwillc.cfrpccore.cluster;

import com.bigwillc.cfrpccore.api.LoadBalancer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author bigwillc on 2024/3/16
 */
public class RoundRibbonLoadBalancer<T> implements LoadBalancer<T> {

    AtomicInteger index = new AtomicInteger(0);

    @Override
    public T choose(List<T> providers) {
        if (providers == null || providers.isEmpty()) {
            return null;
        }

        if(providers.size() == 1) {
            return providers.get(0);
        }

        // index.getAndIncrement() & 0x7fffffff 保证index不会溢出，永远是正数
        return providers.get((index.getAndIncrement()&0x7fffffff) % providers.size());
    }
}
