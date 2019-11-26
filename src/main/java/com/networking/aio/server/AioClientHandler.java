package com.networking.aio.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created on 2019/11/26 17:58
 *
 * @author kaming
 */
public class AioClientHandler implements CompletionHandler<Integer, Object> {

    private AsynchronousSocketChannel clientChannel;

    public AioClientHandler(AsynchronousSocketChannel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void completed(Integer result, Object attachment) {
        Map<String, Object> info = (Map<String, Object>) attachment;
        String type = (String) info.get("type");
        if ("read".equals(type)) {
            ByteBuffer byteBuffer = (ByteBuffer) info.get("buffer");
            byteBuffer.flip();
            info.put("type", "write");
            clientChannel.write(byteBuffer, info, this);
        }
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        // 处理异常
        exc.printStackTrace();
    }
}
