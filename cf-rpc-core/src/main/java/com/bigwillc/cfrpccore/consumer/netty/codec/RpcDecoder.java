package com.bigwillc.cfrpccore.consumer.netty.codec;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;
@Slf4j
public class RpcDecoder extends ByteToMessageDecoder {

    private final Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Make sure we have enough bytes for the length field
        if (in.readableBytes() < 4) {
            return;
        }

        // Mark the current read index
        in.markReaderIndex();

        // Read the length of the message
        int length = in.readInt();
        log.debug("Decoder read message length: {} bytes", length);

        // Check if the complete message is available
        if (in.readableBytes() < length) {
            log.debug("Not enough bytes available yet. Reset reader index.");
            in.resetReaderIndex();
            return;
        }

        // Read the message bytes
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        log.debug("Read {} bytes from buffer", length);

        try {
            // Convert JSON to object
            String jsonString = new String(bytes, "UTF-8");
            Object obj = JSON.parseObject(jsonString, genericClass);
            log.info("Deserialized object of type: {}", obj.getClass().getName());
            out.add(obj);
        } catch (Exception e) {
            log.error("Failed to deserialize message", e);
            throw e;
        }
    }
}