package com.bigwillc.cfrpccore.consumer.netty.codec;

import com.alibaba.fastjson.JSON;
import com.bigwillc.cfrpccore.api.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

// RpcEncoder.java
@Slf4j
public class RpcEncoder extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        log.info("Encoding object of type: {}", msg.getClass().getName());

        // Convert object to JSON string
        String jsonString = JSON.toJSONString(msg);
        byte[] bytes = jsonString.getBytes("UTF-8");

        log.debug("Serialized JSON size: {} bytes", bytes.length);

        // Write length of the JSON string, followed by the string bytes
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
        log.debug("Written message with length: {} bytes", bytes.length);
    }
}