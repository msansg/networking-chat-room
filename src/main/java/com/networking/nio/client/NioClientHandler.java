package com.networking.nio.client;

import com.networking.util.IOUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created on 2019/11/18 13:39
 *
 * @author kaming
 * 客户端线程类，专门接收服务端响应信息
 */
public class NioClientHandler implements Runnable {

    private Selector selector;

    private static final int BUFFER = 1024;

    public NioClientHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            for (; ; ) {
                int readyChannels = this.selector.select();

                if (readyChannels == 0) continue;

                /**
                 * 获取可用channel的Set集合
                 */
                Set<SelectionKey> selectionKeys = this.selector.selectedKeys();

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
                     * 如果是连接事件
                     */
                    if (selectionKey.isValid() && selectionKey.isConnectable()) {
                        this.connectHandle(selectionKey);
                    }
                    /**
                     * 如果是可读事件
                     */
                    if (selectionKey.isValid() && selectionKey.isReadable()) {
                        this.readHandle(selectionKey);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(this.selector);
        }
    }

    /**
     * 连接事件处理器
     */
    public void connectHandle(SelectionKey selectionKey) throws IOException {
        /**
         * 从selectionKey获取到已就绪的channel
         */
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        /**
         * 询问是否完成连接
         */
        if (socketChannel.isConnectionPending()) {
            // 连接成功、监听可读事件
            socketChannel.finishConnect();
            System.out.println("Connected successfully");
            selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }

    /**
     * 可读事件处理器
     */
    private void readHandle(SelectionKey selectionKey) throws IOException {
        /**
         * 从selectionKey获取到已就绪的channel
         */
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        /**
         * 创建一个buffer
         */
        ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER);

        /**
         * 存储响应数据
         */
        StringBuilder stringBuilder = new StringBuilder();

        /**
         * 循环读取服务器响应信息
         */
        while (socketChannel.read(byteBuffer) > 0) {
            /**
             * 切换byteBuffer为读模式
             */
            byteBuffer.flip();

            /**
             * 仍有数据时读取byteBuffer
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
        socketChannel.register(this.selector, SelectionKey.OP_READ);

        /**
         * 将服务器端响应信息打印到本地
         */
        if (stringBuilder.length() > 0) {
            String request = stringBuilder.toString();
            System.out.println(request);
        } else {
            // 通道异常
            selectionKey.cancel();
            this.selector.wakeup();
        }
    }
}
