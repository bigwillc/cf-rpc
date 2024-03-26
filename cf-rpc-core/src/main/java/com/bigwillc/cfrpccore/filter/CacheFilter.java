package com.bigwillc.cfrpccore.filter;

import com.bigwillc.cfrpccore.api.Filter;
import com.bigwillc.cfrpccore.api.RpcRequest;
import com.bigwillc.cfrpccore.api.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author bigwillc on 2024/3/26
 */
public class CacheFilter implements Filter {

    // todo 添加过期时间
    // 替换成guava cache，加容量和过期时间

    // 声明一个cache
    static Map<String, Object> cache = new ConcurrentHashMap();

    @Override
    public Object preFilter(RpcRequest rpcRequest) {
        return cache.get(rpcRequest.toString());
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
        cache.putIfAbsent(request.toString(), result);
        return result;
    }

}
