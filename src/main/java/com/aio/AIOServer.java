package com.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
/**
 * 类AIOServer.java的实现描述：服务端 
 * @author rengq 2018年6月12日 下午4:18:18
 */
public class AIOServer {
    private AsynchronousServerSocketChannel server;

    private ByteBuffer                      receiveBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer                      sendBuffer    = ByteBuffer.allocate(1024);

    public AIOServer(int port) throws IOException {
        server = AsynchronousServerSocketChannel.open();
        server.bind(new InetSocketAddress("localhost", port));
        System.out.println("服务器端已启动，监听端口：" + port);
    }

    public void listener() throws InterruptedException, ExecutionException {
        //        while (true) {
        new Thread() {
            @Override
            public void run() {
                server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

                    public void completed(AsynchronousSocketChannel client, Void attachment) {
                        //当前连接建立成功后，接收下一个请求建立新的连接 
                        server.accept(null, this);
                        process(client);
                    }

                    public void failed(Throwable exc, Void attachment) {
                        System.out.println("异步IO处理失败");
                    }

                    private void process(AsynchronousSocketChannel client) {
                        try {
                            receiveBuffer.clear();
                            int len = client.read(receiveBuffer).get();
                            receiveBuffer.flip();
                            String msg = new String(receiveBuffer.array(), 0, len);
                            System.out.println("已接收客户端发来的消息，" + msg);

                            sendBuffer.clear();
                            sendBuffer.put((msg + "已处理").getBytes());
                            sendBuffer.flip();
                            client.write(sendBuffer, null, new CompletionHandler<Integer, Object>() {

                                public void completed(Integer result, Object attachment) {
                                    if (result.intValue() == -1)
                                        System.out.println("Send response to client error!");
                                    else {
                                        System.out.println("The response has been sent back to client successfully!");
                                    }
                                }

                                public void failed(Throwable exc, Object attachment) {
                                    System.out.println("回写客户端失败！");
                                }

                            });
                        } catch (Exception e) {
                            System.out.println(e);
                            System.out.println("客户端读取数据失败！");
                        }
                    }
                });
                while (true) {
                }
            }
        }.run();
    }

    public static void main(String[] args) throws Exception {
        new AIOServer(8383).listener();
    }
}
