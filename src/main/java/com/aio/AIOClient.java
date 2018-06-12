package com.aio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
/**
 * 类AIOClient.java的实现描述：客户端 
 * @author rengq 2018年6月12日 下午4:17:59
 */
public class AIOClient {
    private AsynchronousSocketChannel client;

    private ByteBuffer                receiveBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer                sendBuffer    = ByteBuffer.allocate(1024);

    public AIOClient() throws Exception {
        client = AsynchronousSocketChannel.open();
        client.connect(new InetSocketAddress("localhost", 8383));
        System.out.println("客户端服务已启动");
    }

    public void send(String context) throws InterruptedException, ExecutionException {
        //向服务器端发送写的数据
        sendBuffer.clear();
        sendBuffer.put(context.getBytes());
        sendBuffer.flip();
        //异步写入
        Thread.sleep(1000L);//如果不设置发送等待时间，会出现 (java.io.IOException: 指定的网络名不再可用。)的情况,原因：由于客户端的处理速度太慢，缓冲区总是满的，导致服务器认为对方结束了连接.因此只要服务器发送的速度在客户端的可承受范围内的时候，就不会再出现这个问题。
        client.write(sendBuffer, null, new CompletionHandler<Integer, Object>() {

            public void completed(Integer result, Object attachment) {
                if (result.intValue() == -1)
                    System.out.println("发送服务失败！");
                else {
                    System.out.println("发送成功！");
                }
            }

            public void failed(Throwable exc, Object attachment) {
                System.out.println("发送失败！");
            }

        });
        //读取数据
        receiveBuffer.clear();
        int len = client.read(receiveBuffer).get();
        if (len > 0) {
            receiveBuffer.flip();
            String content = new String(receiveBuffer.array(), 0, len);
            System.out.println("客户端接受服务器端的消息：" + content);
        }
    }

    public static void main(String[] args) throws Exception {
        while (true) {
            System.out.print("请输入要发送的数据:");
            Scanner sc = new Scanner(System.in);
            String context = sc.nextLine();
            new AIOClient().send(context);
        }
    }
}
