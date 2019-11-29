package com.networking.nio.server;

import com.networking.util.IOUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created on 2019/11/18 14:09
 *
 * @author kaming
 * 服务端线程类，专门处理客户端请求
 */
public class NioServerHandler implements Runnable {

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    public NioServerHandler(Selector selector, ServerSocketChannel serverSocketChannel) {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;
    }

    @Override
    public void run() {
        try {
            for (; ; ) {  // while(true) c for;;
                /**
                 * TODO 获取可用channel数量
                 */
                int readyChannels = selector.select();

                /**
                 * TODO 为什么要这样！！？
                 */
                if (readyChannels == 0) continue;

                /**
                 * 获取可用channel的Set集合
                 */
                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {
                    /**
                     * selectionKey实例
                     */
                    SelectionKey selectionKey = iterator.next();

                    /**
                     * **移除Set中当前selectionKey**
                     */
                    iterator.remove();

                    /**
                     * 7. 根据就绪状态，调用对应方法处理业务逻辑
                     */
                    /**
                     * 如果是接入事件
                     */
                    if (selectionKey.isAcceptable()) {
                        acceptHandle(serverSocketChannel, selector);
                    }

                    /**
                     * 如果是可读事件
                     */
                    if (selectionKey.isReadable()) {
                        readHandle(selectionKey, selector);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(serverSocketChannel, selector);
        }
    }

    /**
     * 接入事件处理器
     */
    private void acceptHandle(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        /**
         * 如果是接入事件，创建socketChannel
         */
        SocketChannel socketChannel = serverSocketChannel.accept();

        /**
         * 将socketChannel设置为非阻塞工作模式
         */
        socketChannel.configureBlocking(false);

        /**
         * 将channel注册到selector上，监听可读事件
         */
        socketChannel.register(selector, SelectionKey.OP_READ);

        /**
         * 回复客户端提示信息
         */
        socketChannel.write(UTF_8.encode("你与聊天室里其他人都不是好友关系，请注意隐私安全"));
    }

    /**
     * 可读事件处理器
     */
    private void readHandle(SelectionKey selectionKey, Selector selector) throws IOException {
        /**
         * 从selectionKey获取到已就绪的channel
         */
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        /**
         * 创建一个buffer
         */
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        /**
         * 存储请求数据
         */
        StringBuilder stringBuilder = new StringBuilder();

        /**
         * 循环读取客户端的请求信息
         */
        while (socketChannel.read(byteBuffer) > 0) {
            /**
             * 切换byteBuffer为读模式
             */
            byteBuffer.flip();

            /**
             * 仍有数据时继续读取byteBuffer
             */
            while (byteBuffer.hasRemaining()) {
                stringBuilder.append(UTF_8.decode(byteBuffer));
            }

            /**
             * 切换byteBuffer为写模式
             */
            byteBuffer.clear();
        }
        /**
         * 将channel再次注册到selector上，监听他的可读事件
         */
        socketChannel.register(selector, SelectionKey.OP_READ);

        /**
         * 将客户端发送的请求信息 广播给其他客户端
         */
        if (stringBuilder.length() > 0) {
            // 广播给其他客户端
            broadCast(selector, socketChannel, stringBuilder.toString());
        }
    }

    /**
     * 广播给其他客户端
     */
    private void broadCast(Selector selector, SocketChannel sourceChannel, String request) {
        /**
         * 获取到所有已接入的客户端channel
         */
        Set<SelectionKey> selectionKeys = selector.keys();

        selectionKeys.forEach(selectionKey -> {
            Channel targetChannel = selectionKey.channel();
            if (targetChannel instanceof SocketChannel && targetChannel != sourceChannel) {
                try {
                    // 将信息发送刚到targetChannel客户端
                    ((SocketChannel) targetChannel).write(UTF_8.encode(request));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
