package com.networking.aio.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2019/11/26 17:30
 *
 * @author kaming
 */
public class AioServerAcceptHandle implements CompletionHandler<AsynchronousSocketChannel, Object> {

    private AsynchronousServerSocketChannel serverChannel;

    public AioServerAcceptHandle(AsynchronousServerSocketChannel serverChannel) {
        this.serverChannel = serverChannel;
    }

    @Override
    public void completed(AsynchronousSocketChannel result, Object attachment) {
        if (serverChannel.isOpen()) {
            serverChannel.accept(null, this);
        }
        AsynchronousSocketChannel clientChannel = result;

        // 保证channel的正常
        if (clientChannel == null || !clientChannel.isOpen()) return;

        AioClientHandler handler = new AioClientHandler(clientChannel);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Map<String, Object> info = new HashMap<>();
        info.put("type", "read");
        info.put("buffer", buffer);

        clientChannel.read(buffer, info, handler);
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        // 处理错误
        exc.printStackTrace();
    }
}
