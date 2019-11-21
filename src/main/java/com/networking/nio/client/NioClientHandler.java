package com.networking.nio.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * Created on 2019/11/18 13:39
 *
 * @author kaming
 * 客户端线程类，专门接收服务端响应信息
 */
public class NioClientHandler implements Runnable {

    private Selector selector;

    public NioClientHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            for (; ; ) {
                int readyChannels = selector.select();

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
                     * 6. 根据就绪状态，调用对应方法处理业务逻辑
                     */
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
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
             * 读取byteBuffer
             */
            stringBuilder.append(StandardCharsets.UTF_8.decode(byteBuffer));
        }
        /**
         * 将channel再次注册到selector上，监听他的可读事件
         */
        socketChannel.register(selector, SelectionKey.OP_READ);

        /**
         * 将服务器端响应信息打印到本地
         */
        if (stringBuilder.length() > 0) {
            String request = stringBuilder.toString();
            System.out.println(request);
        }
    }
}
