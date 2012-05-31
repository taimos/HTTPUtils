package de.taimos.httputils;

import org.apache.http.HttpResponse;
import org.junit.Test;

/**
 * @author thoeger
 * 
 */
public class Tester1 {

	/**
	 * 
	 */
	@Test
	public void testGet() {
		final HttpResponse response = WS.url("http://www.heise.de").get();
		System.out.println("Status: " + WS.getStatus(response));
		System.out.println(WS.getResponseAsString(response));
	}
}
