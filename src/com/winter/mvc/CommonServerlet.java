package com.winter.mvc;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import com.simpledog.annotation.WebServerlet;
import com.simpledog.entry.Request;
import com.simpledog.entry.Response;
import com.simpledog.serverlet.Serverlet;
import com.winter.cache.SimpleCache;

@WebServerlet("/")
public class CommonServerlet implements Serverlet {

	@Override
	public void doGet(Request request, Response response) throws Exception {
		StringBuilder sb = new StringBuilder();
		if (isGetFile(request)) {
			// 文件请求
			URL rootURL = this.getClass().getResource("/");
			String fullPath = rootURL.getPath() + "webpages" + request.getUrl();
			System.out.println("fullPath:" + fullPath);
			File file = new File(fullPath);
			if (file.exists()) {
				FileReader fileReader = new FileReader(file);
				char[] buf = new char[1024];
				int num = 0;
				while ((num = fileReader.read(buf)) != -1) {
					sb.append(new String(buf, 0, num));
				}
				fileReader.close();
			}
		} else {
			// get请求
			Method method = getRequestMethod(request);
			if (method != null) {
				Class<?> clazz = method.getDeclaringClass();
				Object res = method.invoke(clazz.newInstance(), request.getData());
				if (res instanceof String) {
					sb.append(res);
				}
			} else {
				throw new RuntimeException("找不到对应方法");
			}
		}
		response.setData(sb.toString());

	}

	@Override
	public void doPost(Request request, Response response) throws Exception {
		// get请求
		StringBuilder sb = new StringBuilder();
		Method method = getRequestMethod(request);
		Class<?> clazz = method.getDeclaringClass();
		Object res = method.invoke(clazz.newInstance(), request.getData());
		if (res instanceof String) {
			sb.append(res);
		}

		response.setData(sb.toString());
	}

	protected boolean isGetFile(Request request) {
		String url = request.getUrl();
		String[] supportFileType = new String[] { "html", "js", "css" };
		for (String fileType : supportFileType) {
			if (url.endsWith("." + fileType)) {
				return true;
			}
		}
		return false;
	}

	protected Method getRequestMethod(Request request) {
		String url = request.getUrl();
		ConcurrentHashMap<String, Method> pathMethodMap = SimpleCache.getInstance().getPathControllerMap();
		return pathMethodMap.get(url);
	}

}
