package de.taimos.httputils;

import org.apache.http.HttpResponse;
import org.junit.Assert;
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
		Assert.assertEquals(WS.getStatus(response), 200);
		final String body = WS.getResponseAsString(response);
		Assert.assertNotNull(body);
		Assert.assertFalse(body.isEmpty());
	}

}
