package com.networking.aio.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

/**
 * Created on 2019/11/26 17:12
 *
 * @author kaming
 */
public class AioServer {
    private final static String LOCALHOST = "localhost";
    private final static int DEFAULT_PORT = 8888;
    private AsynchronousServerSocketChannel serverChannel;

    private void close(Closeable... closeables) {
        if (closeables != null && closeables.length > 0) {
            try {
                for (int i = 0, length = closeables.length; i < length; i++) {
                    closeables[i].close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        try {
            // 绑定监听端口
            serverChannel = AsynchronousServerSocketChannel.open();
            // 绑定端口
            serverChannel.bind(new InetSocketAddress(DEFAULT_PORT));
            System.out.println("启动服务器，监听端口：" + DEFAULT_PORT);

            for (; ; ) {
                serverChannel.accept(null, new AioServerAcceptHandle(serverChannel));
                // 防止主线程结束
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.close(serverChannel);
        }
    }
}
