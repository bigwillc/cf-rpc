package com.bigwillc.cfrpcdemoprovider;

import com.bigwillc.cfrpccore.annotation.CFProvider;
import com.bigwillc.cfrpcdemoapi.Order;
import com.bigwillc.cfrpcdemoapi.OrderService;
import org.springframework.stereotype.Component;

/**
 * @author bigwillc on 2024/3/9
 */
@Component
@CFProvider
public class OrderServiceImpl implements OrderService {

    @Override
    public Order findById(Integer id) {

        if (id == 404) {
            throw new RuntimeException("404 exception");
        }

        return new Order(id, 100f);
    }
}
