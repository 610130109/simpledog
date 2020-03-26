package com.winter.cache;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCache {

	private static SimpleCache simpleCache;
	private ConcurrentHashMap<String, Method> pathControllerMap = new ConcurrentHashMap<>();

	private SimpleCache() {

	}

	public static SimpleCache getInstance() {
		if (simpleCache == null) {
			synchronized (SimpleCache.class) {
				if (simpleCache == null) {
					simpleCache = new SimpleCache();
				}
			}
		}
		return simpleCache;
	}

	public ConcurrentHashMap<String, Method> getPathControllerMap() {
		return pathControllerMap;
	}

	public void setPathControllerMap(ConcurrentHashMap<String, Method> pathControllerMap) {
		this.pathControllerMap = pathControllerMap;
	}

}
