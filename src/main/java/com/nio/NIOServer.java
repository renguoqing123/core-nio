package com.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 类NIOServer.java的实现描述：服务端代码 
 * @author rengq 2018年6月11日 下午7:41:00
 */
public class NIOServer {
	
	private int port;
	
	private ServerSocketChannel server;
	
	private Selector selector;
	
	private ByteBuffer receiveBuffer=ByteBuffer.allocate(1024);
	private ByteBuffer sendBuffer=ByteBuffer.allocate(1024);
	
	private String msg;
	
	public NIOServer(int port) throws IOException {
		this.port=port;
		server=ServerSocketChannel.open();
		server.configureBlocking(false);//非阻塞
		//绑定端口
		server.socket().bind(new InetSocketAddress(this.port));
		selector=Selector.open();//开始工作
		server.register(selector, SelectionKey.OP_ACCEPT);//服务端注册,准备就绪
		System.out.println("服务端已经启动，监听端口："+this.port);
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
				//来一个处理一个
				process(selectionKey);
				//处理完成就删除
				iterable.remove();
			}
		}
	}

	private void process(SelectionKey key) throws IOException {
		//判断客户端是否跟服务端建立连接
		if(key.isAcceptable()) {
			SocketChannel client=server.accept();
			client.configureBlocking(false);
			client.register(selector, SelectionKey.OP_READ);
		}
		//判断是否可以读数据
		else if(key.isReadable()) {
			receiveBuffer.clear();
			SocketChannel client=(SocketChannel)key.channel();
			int len=client.read(receiveBuffer);
			if(len>0) {
				msg=new String(receiveBuffer.array(), 0, len);
				System.out.println("接受客户端请求："+msg);
				client.register(selector, SelectionKey.OP_WRITE);
			}
		}
		//判断是否可以写数据
		else if(key.isWritable()) {
			sendBuffer.clear();
			SocketChannel client=(SocketChannel)key.channel();
			System.out.println("服务端处理结果完成,msg="+msg);
			sendBuffer.put((msg+",你的消息已处理").getBytes());
			sendBuffer.flip();
			client.write(sendBuffer);
			client.register(selector, SelectionKey.OP_READ);
		}
	}
	
	public static void main(String[] args) throws IOException {
		new NIOServer(8080).listener();
	}
}
