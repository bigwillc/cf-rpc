package com.bigwillc.cfrpccore.consumer.netty.client;

import com.bigwillc.cfrpccore.api.RpcRequest;
import com.bigwillc.cfrpccore.api.RpcResponse;
import com.bigwillc.cfrpccore.consumer.netty.codec.RpcDecoder;
import com.bigwillc.cfrpccore.consumer.netty.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class NettyRpcClient {

    private final String host;
    private final int port;
    private final int timeout;
    private Channel channel;
    private RpcClientHandler handler;
    private EventLoopGroup group;

    private static final int CLIENT_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int CONNECT_TIMEOUT_MILLIS = 5000;

    public NettyRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.timeout = 30;
    }

    public NettyRpcClient(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    public void connect() throws Exception {
        handler = new RpcClientHandler();
        group = new NioEventLoopGroup(CLIENT_THREADS);

        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("logger", new LoggingHandler(LogLevel.INFO))
                                .addLast("decoder", new RpcDecoder(RpcResponse.class))
                                .addLast("encoder", new RpcEncoder())
                                .addLast("handler", handler);
                    }
                });

        // Start the client and get the channel
        log.info("Connecting to {}:{}", host, port);
        channel = b.connect(host, port).sync().channel();
        log.info("Connected successfully to {}:{}", host, port);
    }

    public void close() {
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    public RpcResponse<?> invoke(RpcRequest request) throws Exception {
        // Check if connection is active
        if (channel == null || !channel.isActive()) {
            log.info("Reconnecting to server {}:{}", host, port);
            connect();
        }

        log.info("Sending request: {}", request);

        // Create future to handle response
        RpcClientHandler.RequestFuture future = new RpcClientHandler.RequestFuture();
        handler.addPendingRequest(request.getId(), future);

        // Send request
        channel.writeAndFlush(request).addListener(f -> {
            if (f.isSuccess()) {
                log.info("Successfully sent request ID: {}", request.getId());
            } else {
                log.error("Failed to send request", f.cause());
                // Clean up future in case of write failure
                handler.addPendingRequest(request.getId(), null);
                future.setThrowable(f.cause());
                future.done();
            }
        });

        // Wait for response with timeout
        try {
            if (!future.await(timeout)) {
                throw new RuntimeException("RPC call timeout after " + timeout + " milliseconds");
            }

            // Check for errors
            if (future.getThrowable() != null) {
                throw new RuntimeException("RPC call failed", future.getThrowable());
            }

            RpcResponse<?> response = (RpcResponse<?>) future.getResult();
            log.info("RPC call returned: {}", response);
            return response;
        } catch (InterruptedException e) {
            throw new RuntimeException("RPC call interrupted", e);
        }
    }
}