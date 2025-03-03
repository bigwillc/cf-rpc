package com.bigwillc.cfrpccore.protocol.netty;

import com.bigwillc.cfrpccore.api.RpcRequest;
import com.bigwillc.cfrpccore.api.RpcResponse;
import com.bigwillc.cfrpccore.protocol.RpcInvoker;
import com.bigwillc.cfrpccore.protocol.netty.client.NettyRpcClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bigwillc on 2025/3/3
 */
public class NettyInvoker implements RpcInvoker {

    private final int timeout;

    public NettyInvoker(int timeout) {
        this.timeout = timeout;
    }

    private Map<String, NettyRpcClient> clientMap = new HashMap<>();

    @Override
    public RpcResponse<?> post(RpcRequest rpcRequest, String urlString) {
        String host = "";
        int port = 8090;
        try {
            URL url = new URL(urlString);
            host = url.getHost();
            port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
        }catch (MalformedURLException e){
            throw new IllegalArgumentException("Invalid URL format: " + urlString, e);
        }

        String urlIndex = host + ":" + port;
        NettyRpcClient client = clientMap.get(urlIndex);
        if (client == null) {
            client = new NettyRpcClient(host, port, timeout);
            clientMap.put(urlIndex, client);
        }

        try {
            RpcResponse<?> response = client.invoke(rpcRequest);
            return response;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
