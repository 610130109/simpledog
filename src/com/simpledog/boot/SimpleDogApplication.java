package com.simpledog.boot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.simpledog.annotation.WebServerlet;
import com.simpledog.scan.ClassScannerUtils;
import com.simpledog.server.Server;
import com.simpledog.server.ServerFactory;
import com.simpledog.serverlet.Serverlet;

public class SimpleDogApplication {

	@SuppressWarnings("unchecked")
	public static void run(Class<?> clazz, String[] args) {

		Server server = ServerFactory.getServer("NIOServer");

		Set<Class<?>> classSet = ClassScannerUtils.searchClasses(clazz.getPackage().getName());
		Iterator<Class<?>> iterator = classSet.iterator();

		while (iterator.hasNext()) {
			Class<?> clazzItem = iterator.next();
			clazzItem.getInterfaces();
			WebServerlet webServerlet = clazzItem.getAnnotation(WebServerlet.class);
			if (webServerlet == null) {
				continue;
			}
			Class<?>[] interfaces = clazzItem.getInterfaces();
			if (interfaces == null || interfaces.length <= 0) {
				continue;
			}
			boolean isExtendsServerlet = false;
			for (Class<?> interfaceItem : interfaces) {
				if (interfaceItem.getName().equals(Serverlet.class.getName())) {
					isExtendsServerlet = true;
				}
			}
			if (!isExtendsServerlet) {
				continue;
			}
			String path = webServerlet.value();
			server.addPathServerlet(path, (Class<? extends Serverlet>) clazzItem);
		}

//		server.
		Map<String, String> config = null;
		try {
			config = getConfig(clazz);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (config != null) {
			String port = config.get("server.port");
			if (port != null && port.matches("^\\d+$")) {
				server.setPort(Integer.valueOf(port));
			}
			String maxThread = config.get("server.maxThread");
			if (port != null && port.matches("^\\d+$")) {
				server.setMaxThread(Integer.valueOf(maxThread));
			}
		}

		server.start();
	}

	public static Map<String, String> getConfig(Class<?> clazz) throws IOException {
		Map<String, String> config = new HashMap<>();
		URL rootURL = clazz.getResource("/");
		String applicationConfig = rootURL.getPath() + "application.properties";
		File file = new File(applicationConfig);
		if (file.exists()) {
			@SuppressWarnings("resource")
			BufferedReader bufferReader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = bufferReader.readLine()) != null) {
				if (line != null) {
					String[] params = line.split("=");
					if (params != null && params.length == 2) {
						config.put(params[0], params[1]);
					}
				}
			}

		}
		return config;
	}

}
