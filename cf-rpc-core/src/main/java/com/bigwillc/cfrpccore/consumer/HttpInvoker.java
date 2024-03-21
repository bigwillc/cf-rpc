package com.bigwillc.cfrpccore.consumer;

import com.bigwillc.cfrpccore.api.RpcRequest;
import com.bigwillc.cfrpccore.api.RpcResponse;

/**
 * @author bigwillc on 2024/3/20
 */
public interface HttpInvoker {

    RpcResponse<?> post(RpcRequest rpcRequest, String url);

}
