package com.networking.nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created on 2019/11/18 12:31
 *
 * @author kaming
 * NIO客户端
 */
public class NioClient {

    private static final int PORT = 1877;

    private static final String SERVER_HOST = "127.0.0.1";

    /**
     * 启动
     */
    private void start(String nickname) throws IOException {
        /**
         * 1. 创建一个selector
         */
        Selector selector = Selector.open();

        /**
         * 2. 创建一个socketChannel
         */
        SocketChannel socketChannel = SocketChannel.open();

        /**
         * 3. **设置channel为非阻塞模式**
         */
        socketChannel.configureBlocking(false);

        /**
         * 4. 将channel注册到selector上，监听连接事件
         */
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        /**
         * 5. 连接服务器
         */
        socketChannel.connect(new InetSocketAddress(SERVER_HOST, PORT));

        /**
         * 6. 处理服务端的响应
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
                socketChannel.write(UTF_8.encode(nickname + ": " + request));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        NioClient nioClient = new NioClient();
        nioClient.start("A");
    }
}
