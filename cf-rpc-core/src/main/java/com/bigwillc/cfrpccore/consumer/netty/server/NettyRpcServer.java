package com.bigwillc.cfrpccore.consumer.netty.server;

import com.bigwillc.cfrpccore.api.RpcRequest;
import com.bigwillc.cfrpccore.consumer.netty.codec.RpcDecoder;
import com.bigwillc.cfrpccore.consumer.netty.codec.RpcEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

@Slf4j
public class NettyRpcServer {

    @Value("${netty.server.port:8090}")
    private int port;
    private final ApplicationContext applicationContext;

    // For improved performance
    private static final int BOSS_THREADS = 1;
    private static final int WORKER_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    public NettyRpcServer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void start() throws Exception {
        log.info("start netty server port:{}", port);
        // Configure event loop groups
        EventLoopGroup bossGroup = new NioEventLoopGroup(BOSS_THREADS);
        EventLoopGroup workerGroup = new NioEventLoopGroup(WORKER_THREADS);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new RpcDecoder(RpcRequest.class))
                                    .addLast(new RpcEncoder())
                                    .addLast(new RpcServerHandler(applicationContext));
                        }
                    });

            // Bind and start to accept incoming connections
            b.bind(port).sync().channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads
            log.info("Shut down all event loops to terminate all threads");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}