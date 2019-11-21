package com.networking.bio.server;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created on 2019/11/21 15:40
 *
 * @author kaming
 * 该线程专门用来接收客户端发送过来的消息并广播到其他客户端
 */
public class BioServerHandler implements Runnable {

    private Socket socket;

    private String host;

    private List<PrintWriter> clientList;

    /**
     * 该线程任务负责与指定客户端交互工作
     */
    public BioServerHandler(Socket socket, List<PrintWriter> clientList) {
        this.socket = socket;
        InetAddress address = socket.getInetAddress();
        host = address.getHostAddress();
        this.clientList = clientList;
    }

    @Override
    public void run() {
        PrintWriter pw = null;
        try (
                InputStream in = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(in, UTF_8);
                BufferedReader br = new BufferedReader(isr);
                OutputStream out = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(out, UTF_8);
        ) {
            pw = new PrintWriter(osw, true);

            synchronized (this) {
                clientList.add(pw);
            }
            /**
             * 广播当前客户端上线
             */
            broadCast(pw, host + "上线了");
            pw.println("你与聊天室里其他人都不是好友关系，请注意隐私安全");
            String msg;
            while ((msg = br.readLine()) != null) {
                broadCast(pw, host + ": " + msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            synchronized (this) {
                /**
                 * 将该客户端的输出流从共享集合中删除
                 */
                clientList.remove(pw);
                broadCast(pw, host + "下线了");

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 将消息广播给所有客户端
     *
     * @param pw
     * @param msg
     */
    private void broadCast(PrintWriter pw, String msg) {
        synchronized (this) {
            for (PrintWriter o : clientList) {
                if (o != pw) {
                    o.println(msg);
                }
            }
        }
    }
}