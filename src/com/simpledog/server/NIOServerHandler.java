package com.simpledog.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Map.Entry;

import com.simpledog.convert.RequestConvert;
import com.simpledog.convert.ResponseCreator;
import com.simpledog.entry.Request;
import com.simpledog.entry.Response;
import com.simpledog.proxy.ProxyHandler;
import com.simpledog.serverlet.Serverlet;

public class NIOServerHandler implements Runnable {

	private Map<String, Class<? extends Serverlet>> pathServerletMap;
	private SelectionKey selectionKey;

	public NIOServerHandler(Map<String, Class<? extends Serverlet>> pathServerletMap, SelectionKey selectionKey) {
		this.pathServerletMap = pathServerletMap;
		this.selectionKey = selectionKey;

	}

	@Override
	public void run() {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		SocketChannel sc = (SocketChannel) this.selectionKey.channel();
		Response response = ResponseCreator.getInstance().create("");
		try {
			// 清空缓冲区的旧数据
			buffer.clear();
			int count = sc.read(buffer);
			Request request = null;
			if (count != -1) {
				// 读取到了数据，将buffer的position复位到0
				buffer.flip();
				byte[] bytes = new byte[buffer.remaining()];
				buffer.get(bytes);
				String requestStr = new String(bytes).trim();
				request = RequestConvert.getInstance().convert(requestStr);
			}
			if (request != null) {
				String method = request.getMethod();
				String url = request.getUrl();
				Serverlet serverlet = getServerlet(url);
				if (serverlet != null) {
					try {
						if ("get".equals((method.toLowerCase()))) {
							serverlet.doGet(request, response);
						} else if ("post".equals((method.toLowerCase()))) {
							serverlet.doPost(request, response);
						} else {
							throw new RuntimeException("不支持该请求");
						}
					} catch (Exception e) {
//						response.setCode(200);
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						response.getHeader().put("content-type", "text/plain");
						response.setData(sw.toString());
					}
				} else {
					response.setCode(404);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ByteBuffer byteBuffer = response.toByteBuffer();
				while (byteBuffer.hasRemaining()) {
					sc.write(byteBuffer);
				}
				sc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected Serverlet getServerlet(String url) {
		Class<? extends Serverlet> clazz = pathServerletMap.get(url.toLowerCase());
		if (clazz == null) {
			for (Entry<String, Class<? extends Serverlet>> entry : pathServerletMap.entrySet()) {
				if (url.toLowerCase().startsWith(entry.getKey())) {
					clazz = entry.getValue();
					break;
				}
			}
		}
		Serverlet serverlet = null;
		// 反射
		if (clazz == null) {
			return null;
		}
		try {
			serverlet = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		// 动态代理 调用前后处理AOP
		InvocationHandler handler = new ProxyHandler(serverlet);
		ClassLoader classLoader = serverlet.getClass().getClassLoader();
		Class<?>[] interfaces = serverlet.getClass().getInterfaces();
		Serverlet proxyServerlet = (Serverlet) Proxy.newProxyInstance(classLoader, interfaces, handler);
		return proxyServerlet;
	}

}
