package com.networking.bio.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2019/11/21 15:20
 *
 * @author kaming
 */
public class BioServer {

    private ServerSocket server;

    private static final int SERVER_PORT = 8001;

    /**
     * 存放所有客户端的输出流,用于广播消息
     */
    private List<PrintWriter> clientList;

    /**
     * 启动
     */
    public static void main(String[] args) throws Exception {
        BioServer bioServer = new BioServer();
        bioServer.start();
    }

    /**
     * 监听
     *
     * @throws Exception
     */
    public BioServer() throws Exception {
        server = new ServerSocket(SERVER_PORT);
        System.out.println("服务器启动成功！");
        clientList = new ArrayList<>();
    }


    public void start() throws IOException {
        /**
         * 循环等待客户端的连接
         */
        for (; ; ) {
            Socket socket = server.accept();

            /**
             * 启动一个线程来负责与该客户端交互
             */
            new Thread(new BioServerHandler(socket, clientList)).start();
        }
    }

}
