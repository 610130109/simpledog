package com.simpledog.convert;

import java.util.HashMap;
import java.util.Map;

import com.simpledog.entry.Request;

public class RequestConvert {

	private static RequestConvert instance = null;

	private RequestConvert() {
	}

	// 单例实现 双重校验
	public static RequestConvert getInstance() {
		if (instance == null) {
			synchronized (RequestConvert.class) {
				if (instance == null) {
					instance = new RequestConvert();
				}
			}
		}
		return instance;
	}

	public Request convert(String requestStr) {
		Request request = new Request();
		if (requestStr == null || requestStr.isEmpty()) {
			return request;
		}
		String[] lines = requestStr.split("\r\n");
		// 第一行
		String[] line0 = lines[0].split(" ");
		request.setMethod(line0[0]);
		request.setUrl(line0[1]);
		request.setProtocol(line0[2]);
		// header
		int i = 1;
		Map<String, String> header = new HashMap<>();
		for (; i < lines.length; i++) {
			String line = lines[i];
			if (line.equals("")) {
				break;
			} else {
				String[] headParam = line.split(":");
				header.put(headParam[0], headParam[1]);
			}
		}
		request.setHeader(header);
		// body
		StringBuilder bodySb = new StringBuilder();
		i++;
		for (; i < lines.length; i++) {
			String line = lines[i];
			bodySb.append(line);
			if (i < lines.length - 1) {
				bodySb.append("\r\n");
			}
		}
		request.setData(bodySb.toString());
		return request;
	}

}
