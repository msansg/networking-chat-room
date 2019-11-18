package com.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

/**
 * Created on 2019/11/18 12:31
 *
 * @author kaming
 * NIO服务器端
 */
public class NioServer {

    /**
     * 启动
     */
    public void start() throws IOException {
        /**
         * 1. 创建一个selector
         */
        Selector selector = Selector.open();

        /**
         * 2. 通过ServerSocketChannel创建channel通道
         */
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        /**
         * 3. 为channel绑定监听端口
         */
        serverSocketChannel.bind(new InetSocketAddress(8000));

        /**
         * 4. **设置channel为非阻塞模式**
         */
        serverSocketChannel.configureBlocking(false);

        /**
         * 5. 将channel注册到selector上，监听连接事件
         */
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动完成！");

        /**
         * 6. 处理新接入的连接
         * 新开一个线程，专门等待和处理新接入的连接
         */
        new Thread(new NioServerHandler(selector, serverSocketChannel)).start();
    }

    /**
     * 主方法
     * @param args
     */
    public static void main(String[] args) throws IOException {
        NioServer nioServer = new NioServer();
        nioServer.start();
    }

}
