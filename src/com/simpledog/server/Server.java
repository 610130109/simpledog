package com.simpledog.server;

import com.simpledog.serverlet.Serverlet;

public interface Server {

	public void start();

	public void addPathServerlet(String path, Class<? extends Serverlet> clazz);

	public void setPort(int port);

	public void setMaxThread(int maxThread);

}
