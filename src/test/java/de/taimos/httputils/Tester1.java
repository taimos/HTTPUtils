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

	/**
	 * 
	 */
	@Test
	public void testBurnin() {
		long time = System.nanoTime();
		for (int i = 0; i < 100; i++) {
			this.testGet();

			// output system infos like call duration and memory consumption
			final long duration = ((System.nanoTime() - time) / 1000000);
			final long mem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000;
			System.out.println(duration + " ms with ram " + mem);
			time = System.nanoTime();
		}
	}
}
