package com.bigwillc.cfrpccore.api;

import lombok.SneakyThrows;

/**
 * @author bigwillc on 2024/3/16
 */
public interface Filter {

    Object preFilter(RpcRequest rpcRequest);

    Object postFilter(RpcRequest request, RpcResponse rpcResponse, Object result);

    Filter Default = new Filter() {
        @Override
        public RpcResponse preFilter(RpcRequest rpcRequest) {
            return null;
        }

        @Override
        public RpcResponse postFilter(RpcRequest request, RpcResponse rpcResponse, Object result) {
            return null;
        }
    };
}
