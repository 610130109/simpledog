package com.simpledog.convert;

import java.util.HashMap;
import java.util.Map;

import com.simpledog.entry.Response;

public class ResponseCreator {

	private static class ResponseCreatorInstance {
		private static final ResponseCreator instance = new ResponseCreator();
	}

	private ResponseCreator() {
	}

	public static ResponseCreator getInstance() {
		return ResponseCreatorInstance.instance;
	}

	public Response create(String data) {
		Response response = new Response();
		response.setCode(200);
		response.setState("OK");
		response.setProtocol("HTTP/1.1");
		Map<String, String> header = new HashMap<>();
		header.put("connection", "close");
		header.put("server", "server");
		header.put("content-type", "text/html;charset=utf-8");
		header.put("content-length", String.valueOf(data != null ? data.length() : 0));
		response.setHeader(header);
		response.setData(data);
		return response;
	}

}
