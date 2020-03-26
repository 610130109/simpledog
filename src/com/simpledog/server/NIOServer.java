package com.simpledog.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.simpledog.serverlet.Serverlet;

public class NIOServer implements Server {

	private int port = 8090;
	private int maxThread = 5;
	private Selector selector;
	private ExecutorService executorService = null;
	private Map<String, Class<? extends Serverlet>> pathServerletMap = new HashMap<>();

	public NIOServer() {
	}

	protected void init() {
		initThreadPool();
		initServer();
		loadServerletMap();
	}

	protected void initThreadPool() {
		this.executorService = Executors.newFixedThreadPool(maxThread);
	}

	protected void initServer() {
		ServerSocketChannel ssc = null;
		try {
			// 开启socket
			ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			// 绑定端口
			ssc.bind(new InetSocketAddress(port));
			// 注册selector
			selector = Selector.open();
			// 接收请求
			ssc.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("max thread num " + maxThread);
			System.out.println("NioServer started on " + port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void loadServerletMap() {
//		pathServerletMap.put("/test", TestServerlet.class);
	}

	public void addPathServerlet(String path, Class<? extends Serverlet> clazz) {
		pathServerletMap.put(path, clazz);
	}

	@Override
	public void start() {
		init();
		while (true) {
			int count = 0;
			try {
				count = selector.select();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (count <= 0)
				continue;
			Set<SelectionKey> selectionKeySet = selector.selectedKeys();
			Iterator<SelectionKey> keys = selectionKeySet.iterator();
			while (keys.hasNext()) {
				SelectionKey key = keys.next();
				keys.remove();
				if (!key.isValid()) {
					continue;
				}
				if (key.isAcceptable()) {
					accept(key);
				} else if (key.isReadable()) {
					read(key);
				}
			}
		}
	}

	protected void accept(SelectionKey key) {
		try {
			ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
			SocketChannel sc = ssc.accept();
			sc.configureBlocking(false);
			sc.register(selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void read(SelectionKey key) {
		executorService.submit(new NIOServerHandler(pathServerletMap, key));
		key.cancel();
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getMaxThread() {
		return maxThread;
	}

	public void setMaxThread(int maxThread) {
		this.maxThread = maxThread;
	}

}