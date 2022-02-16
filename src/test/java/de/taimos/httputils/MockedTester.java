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

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

/**
 * @author thoeger
 */
public class MockedTester {

    private static final int PORT = 8888;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().port(MockedTester.PORT).bindAddress("localhost"));

    @Test
    public void retrySuccess() {
        final String scenario = "retrySuccess";
        stubFor(get(urlEqualTo("/" + scenario)).inScenario(scenario)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("retry0")
        );
        stubFor(get(urlEqualTo("/" + scenario)).inScenario(scenario)
                .whenScenarioStateIs("retry0")
                .willReturn(aResponse().withStatus(201))
                .willSetStateTo("done")
        );
        final HTTPRequest request = WS.url("http://localhost:" + MockedTester.PORT + "/" + scenario).retry(1, Retryable.standard(), WaitStrategy.constant(100));
        try (final HTTPResponse response = request.get()) {
            Assert.assertEquals(201, response.getStatus());
        }
    }

    @Test
    public void retryFailure() {
        final String scenario = "retryFailure";
        stubFor(get(urlEqualTo("/" + scenario)).inScenario(scenario)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("retry0")
        );
        stubFor(get(urlEqualTo("/" + scenario)).inScenario(scenario)
                .whenScenarioStateIs("retry0")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("done")
        );
        try (final HTTPResponse ignored = WS.url("http://localhost:" + MockedTester.PORT + "/" + scenario).retry(1, Retryable.standard(), WaitStrategy.constant(100)).get()) {
            Assert.fail();
        } catch (final RuntimeException e) {
            Assert.assertEquals("retry exhausted", e.getMessage());
            Assert.assertEquals(RuntimeException.class, e.getCause().getClass());
            Assert.assertEquals("status code 500", e.getCause().getMessage());
        }
    }

}
