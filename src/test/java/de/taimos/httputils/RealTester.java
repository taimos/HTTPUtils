package de.taimos.httputils;

/*
 * #%L
 * Taimos HTTPUtils
 * %%
 * Copyright (C) 2012 - 2015 Taimos GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import de.taimos.httputils.callbacks.HTTPStringCallback;

/**
 * @author thoeger
 */
public class RealTester {

    /**
     *
     */
    @Test
    public void testGetSuccess() {
        try (final HTTPResponse response = WS.url("http://www.heise.de").get()) {
            this.assertOKWithBody(response);
        }
    }

    /**
     *
     */
    @Test
    public void testGetSuccessWithRetry() {
        try (final HTTPResponse response = WS.url("http://www.heise.de").retry().get()) {
            this.assertOKWithBody(response);
        }
    }

    /**
     *
     */
    @Test
    public void testGetAsyncResponseCallbackSuccess() throws InterruptedException {
        final CountDownLatch cdl = new CountDownLatch(1);
        WS.url("http://www.heise.de").getAsync(new HTTPResponseCallback() {

            @Override
            public void response(final HTTPResponse response) {
                RealTester.this.assertOKWithBody(response);
                cdl.countDown();
            }

            @Override
            public void fail(final Exception e) {
                e.printStackTrace();
            }
        });
        Assert.assertTrue(cdl.await(10, TimeUnit.SECONDS));
    }

    /**
     *
     */
    @Test
    public void testGetAsyncResponseCallbackSuccessWithRetry() throws InterruptedException {
        final CountDownLatch cdl = new CountDownLatch(1);
        WS.url("http://www.heise.de").retry().getAsync(new HTTPResponseCallback() {

            @Override
            public void response(final HTTPResponse response) {
                RealTester.this.assertOKWithBody(response);
                cdl.countDown();
            }

            @Override
            public void fail(final Exception e) {
                e.printStackTrace();
            }
        });
        Assert.assertTrue(cdl.await(10, TimeUnit.SECONDS));
    }

    /**
     *
     */
    @Test
    public void testGetAsyncStringCallbackSuccess() throws InterruptedException {
        final CountDownLatch cdl = new CountDownLatch(1);
        WS.url("http://www.heise.de").getAsync(new HTTPStringCallback() {

            @Override
            public void fail(final Exception e) {
                e.printStackTrace();
                Assert.fail();
                cdl.countDown();
            }

            @Override
            protected void invalidStatus(final int status, final HTTPResponse response) {
                System.out.println("Invalid status: " + status);
                Assert.fail();
                cdl.countDown();
            }

            @Override
            protected void stringResponse(final String body, final HTTPResponse response) {
                Assert.assertNotNull(body);
                Assert.assertFalse(body.isEmpty());
                cdl.countDown();
            }

        });
        Assert.assertTrue(cdl.await(10, TimeUnit.SECONDS));
    }

    /**
     *
     */
    @Test(timeout = 2000)
    public void testTimeout() {
        try (final HTTPResponse ignored = WS.url("http://www.sdfsdfdfs.de").timeout(1000).get()) {
            Assert.fail();
        } catch (final RuntimeException e) {
            Assert.assertEquals(UnknownHostException.class, e.getCause().getClass());
        }
    }

    /**
     *
     */
    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testRetryExhausted() {
        final WaitStrategy zero = (retry) -> 0;
        try (final HTTPResponse ignored = WS.url("http://www.sdfsdfdfs.de").timeout(1000).retry(2, Retryable.standard(), zero).get()) {
            Assert.fail();
        } catch (final RuntimeException e) {
            Assert.assertEquals("retry exhausted", e.getMessage());
            Assert.assertEquals(UnknownHostException.class, e.getCause().getClass());
            throw e;
        }
    }

    private void assertOKWithBody(HTTPResponse response) {
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertTrue(response.isStatusOK());
        final String body = response.getResponseAsString();
        Assert.assertNotNull(body);
        Assert.assertFalse(body.isEmpty());
    }

}
