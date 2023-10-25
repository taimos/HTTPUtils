package de.taimos.httputils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Copyright 2023 Cinovo AG<br>
 * <br>
 *
 * @author mweise
 */
public class ProxyTest {
    private WireMockServer targetService;
    private WireMock target;

    private WireMockServer proxyingService;
    private WireMock proxy;

    @Before
    public void init() {
        String bindAddress = "127.0.0.1";
        this.targetService = new WireMockServer(wireMockConfig().dynamicPort().bindAddress(bindAddress).stubCorsEnabled(true));
        this.targetService.start();
        this.target = WireMock.create().port(this.targetService.port()).build();

        this.proxyingService = new WireMockServer(wireMockConfig().dynamicPort().bindAddress(bindAddress));
        this.proxyingService.start();
        this.proxy = WireMock.create().port(this.proxyingService.port()).build();

        WireMock.configureFor(this.targetService.port());
    }

    @After
    public void stop() {
        this.targetService.stop();
        this.proxyingService.stop();
    }

    @Test
    public void testWithoutHttpProxy() {
        String targetPath = "/direct";
        this.target.register(
                get(urlEqualTo(targetPath))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "text/plain")
                                        .withBody("Content")));


        this.proxy.register(get(urlEqualTo(targetPath)).atPriority(10).willReturn(aResponse().proxiedFrom(this.targetService.baseUrl())));

        String targetUrl = this.targetService.url(targetPath);
        HTTPRequest request = WS.url(targetUrl); // direct access
        try (HTTPResponse response = request.get()) {
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals("Content", response.getResponseAsString());
            this.proxy.verifyThat(0, getRequestedFor(urlMatching(targetPath)));
            this.target.verifyThat(1, getRequestedFor(urlMatching(targetPath)));
        }
    }

    @Test
    public void testWithHttpProxy() {
        String targetPath = "/proxied";

        this.target.register(
                get(urlEqualTo(targetPath))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "text/plain")
                                        .withBody("Proxied content")));

        this.proxy.register(get(urlEqualTo(targetPath)).atPriority(10).willReturn(aResponse().proxiedFrom(this.targetService.baseUrl())));

        String targetUrl = this.targetService.url(targetPath);
        HTTPRequest request = WS.url(targetUrl).proxy("localhost", this.proxyingService.port());
        try (HTTPResponse response = request.get()) {
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals("Proxied content", response.getResponseAsString());
            this.proxy.verifyThat(1, getRequestedFor(urlMatching(targetPath)));
            this.target.verifyThat(1, getRequestedFor(urlMatching(targetPath)));
        }
    }
}
