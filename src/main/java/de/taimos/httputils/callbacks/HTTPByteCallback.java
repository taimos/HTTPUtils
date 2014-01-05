package de.taimos.httputils.callbacks;

import org.apache.http.HttpResponse;

import de.taimos.httputils.WS;

public abstract class HTTPByteCallback extends HTTPStatusCheckCallback {
	
	@Override
	protected void checkedResponse(HttpResponse response) {
		this.byteResponse(WS.getResponseAsBytes(response), response);
	}
	
	protected abstract void byteResponse(byte[] body, HttpResponse response);
	
}
