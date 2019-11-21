package com.networking.nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Created on 2019/11/18 12:31
 *
 * @author kaming
 * NIO客户端
 */
public class NioClient {

    /**
     * 启动
     */
    private void start(String nickname) throws IOException {
        /**
         * 1. 创建一个selector
         */
        Selector selector = Selector.open();

        /**
         * 2. 连接服务器端
         */
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8000));
        System.out.println("连接服务器成功！");

        /**
         * 3. **设置channel为非阻塞模式**
         */
        socketChannel.configureBlocking(false);

        /**
         * 4. 将channel注册到selector上，监听可读事件
         */
        socketChannel.register(selector, SelectionKey.OP_READ);

        /**
         * 5. 处理服务端的响应
         * 新开一个线程，专门等待和处理服务端的响应
         */
        new Thread(new NioClientHandler(selector)).start();

        /**
         * 向服务器端发送数据
         */
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String request = scanner.nextLine();
            if (request != null && request.length() > 0) {
                socketChannel.write(StandardCharsets.UTF_8.encode(nickname + ": " + request));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        NioClient nioClient = new NioClient();
        nioClient.start("A");
    }
}
