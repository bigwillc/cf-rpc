package com.bigwillc.cfrpccore.api;

import com.bigwillc.cfrpccore.meta.InstanceMeta;
import lombok.Data;

import java.util.List;

/**
 * @author bigwillc on 2024/3/16
 */

@Data
public class RpcContext {

    List<Filter> filters;

    Router<InstanceMeta> router;

    LoadBalancer<InstanceMeta> loadBalancer;
}
