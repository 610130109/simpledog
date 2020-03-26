package com.simpledog.serverlet;

import com.simpledog.entry.Request;
import com.simpledog.entry.Response;

public interface Serverlet {

	public void doGet(Request request, Response response) throws Exception;

	public void doPost(Request request, Response response) throws Exception;

}
