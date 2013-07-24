package de.taimos.httputils;

/*
 * #%L Taimos HTTPUtils %% Copyright (C) 2012 - 2013 Taimos GmbH %% Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License. #L%
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
		Assert.assertTrue(WS.isStatusOK(response));
		final String body = WS.getResponseAsString(response);
		Assert.assertNotNull(body);
		Assert.assertFalse(body.isEmpty());
	}
	
	/**
	 * 
	 */
	@Test
	public void testGetAsync() throws InterruptedException {
		final CountDownLatch cdl = new CountDownLatch(1);
		WS.url("http://www.heise.de").getAsync(new HTTPResponseCallback() {
			
			@Override
			public void response(HttpResponse response) {
				Assert.assertEquals(WS.getStatus(response), 200);
				Assert.assertTrue(WS.isStatusOK(response));
				final String body = WS.getResponseAsString(response);
				Assert.assertNotNull(body);
				Assert.assertFalse(body.isEmpty());
				cdl.countDown();
			}
			
			@Override
			public void fail(Exception e) {
				Assert.fail();
				cdl.countDown();
			}
		});
		cdl.await(20, TimeUnit.SECONDS);
	}
	
	/**
	 * 
	 */
	@Test(timeout = 2500, expected = Exception.class)
	public void testGetWithTimeout() {
		WS.url("http://www.sdfsdfdfs.de").timeout(2000).get();
	}
	
}
