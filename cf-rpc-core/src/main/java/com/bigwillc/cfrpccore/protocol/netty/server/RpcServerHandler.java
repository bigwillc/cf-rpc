package com.bigwillc.cfrpccore.protocol.netty.server;

import com.bigwillc.cfrpccore.api.RpcRequest;
import com.bigwillc.cfrpccore.api.RpcResponse;
import com.bigwillc.cfrpccore.meta.ProviderMeta;
import com.bigwillc.cfrpccore.provider.ProviderBootstrap;
import com.bigwillc.cfrpccore.provider.ProviderInvoker;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.context.ApplicationContext;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;

@Slf4j
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final ApplicationContext applicationContext;

    public RpcServerHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        log.info("Server received request: {}", request);

        // Process the request
        ProviderInvoker providerInvoker = applicationContext.getBean(ProviderInvoker.class);
        RpcResponse<?> response = providerInvoker.invoke(request);

        // Ensure response ID matches request ID
        if (response.getId() == null || !response.getId().equals(request.getId())) {
            log.warn("Response ID doesn't match request ID. Setting correct ID.");
            response.setId(request.getId());
        }

        log.info("Server sending response: {}", response);

        // Send response with a listener to verify it was sent successfully
        ChannelFuture future = ctx.writeAndFlush(response);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("Response sent successfully for request ID: {}", request.getId());
                } else {
                    log.error("Failed to send response for request ID: {}", request.getId(), future.cause());
                }
            }
        });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Server channel active: {}", ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Server channel inactive: {}", ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Server exception caught", cause);
        ctx.close();
    }
}