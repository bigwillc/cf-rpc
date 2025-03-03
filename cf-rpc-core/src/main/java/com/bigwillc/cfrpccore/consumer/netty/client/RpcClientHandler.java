package com.bigwillc.cfrpccore.consumer.netty.client;

import com.bigwillc.cfrpccore.api.RpcRequest;
import com.bigwillc.cfrpccore.api.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private final ConcurrentHashMap<String, RequestFuture> pendingRequests =
            new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client channel active: {}", ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client channel inactive: {}", ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        log.info("Client received response: {}", msg);
        String requestId = msg.getId();
        RequestFuture future = pendingRequests.get(requestId);

        if (future != null) {
            pendingRequests.remove(requestId);
            log.info("Found pending request for ID: {}", requestId);

            if (msg.getEx() != null) {
                future.setThrowable(msg.getEx());
                log.warn("Response contains exception: {}", msg.getEx().getMessage());
            } else {
                future.setResult(msg);
                log.info("Response processed successfully");
            }

            // Notify waiting thread
            future.done();
        } else {
            log.warn("Received response for unknown request ID: {}", requestId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Client exception caught", cause);
        // Notify all pending requests about the channel exception
        pendingRequests.forEach((id, future) -> {
            future.setThrowable(cause);
            future.done();
        });
        ctx.close();
    }

    // Added method to handle any message type (debugging)
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("channelRead received message of type: {}", msg.getClass().getName());
        super.channelRead(ctx, msg);
    }

    public void addPendingRequest(String requestId, RequestFuture future) {
        pendingRequests.put(requestId, future);
        log.info("Added pending request with ID: {}", requestId);
    }

    // Improved RequestFuture class with timeout support
    public static class RequestFuture {
        private final CountDownLatch latch = new CountDownLatch(1);
        private Object result;
        private Throwable throwable;
        private static final int DEFAULT_TIMEOUT_SECONDS = 30;

        public void await() throws InterruptedException {
            await(DEFAULT_TIMEOUT_SECONDS);
        }

        public boolean await(long timeoutMilliSeconds) throws InterruptedException {
            boolean completed = latch.await(timeoutMilliSeconds, TimeUnit.MILLISECONDS);
            if (!completed) {
                throwable = new RuntimeException("RPC call timed out after " + timeoutMilliSeconds + " seconds");
            }
            return completed;
        }

        public void done() {
            latch.countDown();
        }

        public void setResult(Object result) {
            this.result = result;
        }

        public Object getResult() {
            return result;
        }

        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
        }

        public Throwable getThrowable() {
            return throwable;
        }
    }
}