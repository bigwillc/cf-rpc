package com.bigwillc.cfrpccore.protocol;

import com.bigwillc.cfrpccore.protocol.http.OkHttpInvoker;
import com.bigwillc.cfrpccore.protocol.netty.NettyInvoker;

/**
 * Factory for creating different types of invokers
 *
 * @author bigwillc
 */
public class InvokerFactory {

//    public enum Protocol {
//        HTTP,
//        NETTY
//    }

    /**
     * Create an invoker based on the specified protocol
     *
     * @param protocol The protocol to use (HTTP or NETTY)
     * @param timeout Timeout in milliseconds
     * @return An HttpInvoker implementation
     */
    public static RpcInvoker createInvoker(String protocol, int timeout) {
        return switch (protocol) {
            case "http" -> new OkHttpInvoker(timeout);
            case "netty" -> new NettyInvoker(timeout);
            default -> new OkHttpInvoker(timeout);
        };
    }
}