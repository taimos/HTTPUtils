package de.taimos.httputils.callbacks;

import org.apache.http.HttpResponse;

import de.taimos.httputils.WS;

public abstract class HTTPStringCallback extends HTTPStatusCheckCallback {
	
	@Override
	protected void checkedResponse(HttpResponse response) {
		this.stringResponse(WS.getResponseAsString(response), response);
	}
	
	protected abstract void stringResponse(String body, HttpResponse response);
	
}
