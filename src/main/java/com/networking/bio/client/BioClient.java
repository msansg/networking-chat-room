package com.networking.bio.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created on 2019/11/21 15:22
 *
 * @author kaming
 */
public class BioClient {

    private Socket socket;

    private static final int SERVER_PORT = 8001;

    private static final String SERVER_ADDRESS = "localhost";

    /**
     * 启动
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        BioClient bioClient = new BioClient();
        bioClient.start();
    }

    /**
     * 构造方法连接服务器
     *
     * @throws Exception
     */
    public BioClient() throws Exception {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        System.out.println("连接服务器成功！");
    }

    /**
     * 客户端启动方法
     */
    private void start() {
        try (OutputStream ops = socket.getOutputStream();
             Scanner scanner = new Scanner(System.in)) {
            /**
             * 启动读取服务端发送过来消息的线程
             */
            new Thread(new BioClientHandler(socket)).start();

            /**
             * 发送消息
             */
            OutputStreamWriter osw = new OutputStreamWriter(ops, UTF_8);
            PrintWriter pw = new PrintWriter(osw, true);
            for (; ; ) {
                String message = scanner.nextLine();
                if (message != null && message.length() > 0) {
                    pw.println(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
