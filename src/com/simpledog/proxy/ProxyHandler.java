package com.simpledog.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyHandler implements InvocationHandler {

	private Object object;

	public ProxyHandler(Object object) {
		this.object = object;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("serverlet in");
		Long startTime = System.currentTimeMillis();
		method.invoke(object, args);
		System.out.println("time cost:" + (System.currentTimeMillis() - startTime) + "ms");
		System.out.println("serverlet out");
		return null;
	}

}
