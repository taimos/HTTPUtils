package de.taimos.httputils;

import org.apache.http.HttpResponse;

public interface HTTPResponseCallback {
	
	void response(HttpResponse response);
	
	void fail(Exception e);
}
