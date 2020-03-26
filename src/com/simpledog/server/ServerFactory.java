package com.simpledog.server;

// 服务工厂 其他扩张。。。
public class ServerFactory {

	public static Server getServer(String serverType) {
		if ("NIOServer".equals(serverType)) {
			return new NIOServer();
		}
		return null;
	}

}
