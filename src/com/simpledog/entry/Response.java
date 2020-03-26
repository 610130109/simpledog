package com.simpledog.entry;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;

public class Response {

	private String protocol;
	private int code;
	private String state;
	private Map<String, String> header;
	private String data;

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Map<String, String> getHeader() {
		return header;
	}

	public void setHeader(Map<String, String> header) {
		this.header = header;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
		if (header != null) {
			header.put("content-length", String.valueOf(data != null ? data.getBytes().length : 0));
		}
	}

	public String toResponseString() {
		StringBuilder sb = new StringBuilder();
		sb.append(protocol).append(" ").append(String.valueOf(code)).append(" ").append(state).append("\r\n");
		if (header != null && !header.isEmpty()) {
			for (Entry<String, String> entry : header.entrySet()) {
				sb.append(entry.getKey()).append(":").append(entry.getValue()).append("\r\n");
			}
		}
		sb.append("").append("\r\n");
		sb.append(data);
		return sb.toString();
	}

	public ByteBuffer toByteBuffer() {
		try {
			return ByteBuffer.wrap(toResponseString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
