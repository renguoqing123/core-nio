package com.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * 类NIOClient.java的实现描述：客户端代码 
 * @author rengq 2018年6月11日 下午7:40:40
 */
public class NIOClient {
	
	private SocketChannel client;
	
	private Selector selector;
	
	private ByteBuffer receiveBuffer=ByteBuffer.allocate(1024);
	private ByteBuffer sendBuffer=ByteBuffer.allocate(1024);
	
	public NIOClient() throws IOException {
		client=SocketChannel.open();
		
		client.configureBlocking(false);
		
		client.connect(new InetSocketAddress("localhost",8080));
		
		selector=Selector.open();
		
		client.register(selector, SelectionKey.OP_CONNECT);
	}
	
	
	public void listener() throws IOException {
		while (true) {
			//判断当前客户端有没有注册
			int i=selector.select();
			if(i==0) {continue;}//不存在就跳出本循环
			//如果存在，就开始轮循
			Set<SelectionKey> keys=selector.selectedKeys();
			Iterator<SelectionKey> iterable=keys.iterator();
			while (iterable.hasNext()) {
				SelectionKey selectionKey = iterable.next();
				
				process(selectionKey);
				//处理完成就删除
				iterable.remove();
			}
		}
	}


	private void process(SelectionKey key) throws IOException {
		if(key.isConnectable()) {//判断是否建立连接
			if(client.isConnectionPending()){  //判断此通道上是否正在进行连接操作。  
				client.finishConnect();//开始连接
				System.out.println("完成连接");
			}
			client.register(selector, SelectionKey.OP_WRITE);//开始写操作
		}else if(key.isWritable()) {
			sendBuffer.clear();
			System.out.print("请输入内容：");
			Scanner sc=new Scanner(System.in);
			String name=sc.nextLine();
			System.out.println("客户端向服务端发送数据："+name);
			sendBuffer.put(name.getBytes());
			sendBuffer.flip();
			client.write(sendBuffer);
			client.register(selector, SelectionKey.OP_READ);//开始读操作
		}else if(key.isReadable()){
			receiveBuffer.clear();
			int len=client.read(receiveBuffer);
			if(len>0) {
				receiveBuffer.flip();
				String msg=new String(receiveBuffer.array(), 0, len);
				System.out.println("获取服务端返回的消息："+msg);
				client.register(selector, SelectionKey.OP_WRITE);//开始写操作
			}
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		new NIOClient().listener();
	}
}
