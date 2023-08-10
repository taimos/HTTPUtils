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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author thoeger
 */
public final class HTTPRequest {

    private static final Executor DEFAULT_EXECUTOR = Executors.newCachedThreadPool();

    private static CloseableHttpClient DEFAULT_HTTP_CLIENT = HttpClientBuilder.create().build();

    private final String url;

    private final Map<String, List<String>> headers = new ConcurrentHashMap<>();

    private final Map<String, List<String>> queryParams = new ConcurrentHashMap<>();

    private final Map<String, String> pathParams = new ConcurrentHashMap<>();

    private volatile HttpHost proxy;
    private volatile Integer timeout;

    private volatile boolean followRedirect = true;

    private volatile String body = "";

    private volatile String userAgent = null;

    private volatile int maxRetries = 0;

    private volatile Retryable retryable = null;

    private volatile WaitStrategy waitStrategy = null;

    /**
     * @param url URL
     */
    HTTPRequest(final String url) {
        this.url = url;
    }

    /**
     * resets the http client
     */
    public static void resetHTTPClient() {
        HTTPRequest.DEFAULT_HTTP_CLIENT = HttpClientBuilder.create().build();
    }

    /**
     * @param name  the name of the header
     * @param value the value of the header
     * @return this
     */
    public HTTPRequest header(final String name, final String value) {
        if(!this.headers.containsKey(name)) {
            this.headers.put(name, new CopyOnWriteArrayList<String>());
        }
        this.headers.get(name).add(value);
        return this;
    }

    /**
     * @param name  the name of the query parameter
     * @param value the value of the query parameter
     * @return this
     */
    public HTTPRequest queryParam(final String name, final String value) {
        if(!this.queryParams.containsKey(name)) {
            this.queryParams.put(name, new CopyOnWriteArrayList<String>());
        }
        this.queryParams.get(name).add(value);
        return this;
    }

    /**
     * @param name  the name of the path parameter
     * @param value the value of the path parameter
     * @return this
     */
    public HTTPRequest pathParam(final String name, final String value) {
        this.pathParams.put(name, value);
        return this;
    }

    /**
     * @param proxyHost the proxy hostname
     * @param proxyPort the proxy port
     * @return this
     */
    public HTTPRequest proxy(String proxyHost, int proxyPort) {
        this.proxy = new HttpHost(proxyHost, proxyPort);
        return this;
    }

    /**
     * @param newTimeout Timeout in ms
     * @return this
     */
    public HTTPRequest timeout(final int newTimeout) {
        this.timeout = newTimeout;
        return this;
    }

    /**
     * @param follow <code>true</code> to automatically follow redirects; <code>false</code> otherwise
     * @return this
     */
    public HTTPRequest followRedirect(final boolean follow) {
        this.followRedirect = follow;
        return this;
    }

    /**
     * Retry.
     *
     * @param maxRetries   Maximum number of retries
     * @param retryable    Function to determine if call should be retried
     * @param waitStrategy Function to calculate the time to wait between two retries
     * @return this
     */
    public HTTPRequest retry(final int maxRetries, final Retryable retryable, final WaitStrategy waitStrategy) {
        if(maxRetries <= 0) {
            throw new IllegalArgumentException("maxRetries must be > 0");
        }
        if(retryable == null) {
            throw new IllegalArgumentException("retryable must not be null");
        }
        if(waitStrategy == null) {
            throw new IllegalArgumentException("waitStrategy must not be null");
        }
        this.maxRetries = maxRetries;
        this.retryable = retryable;
        this.waitStrategy = waitStrategy;
        return this;
    }

    /**
     * Retry 5 times on Exception or 5XX status code with exponential backoff.
     *
     * @return this
     */
    public HTTPRequest retry() {
        return this.retry(5, Retryable.standard(), WaitStrategy.exponentialBackoff());
    }

    /**
     * @param agent the user agent string to use
     * @return this
     */
    public HTTPRequest userAgent(final String agent) {
        this.userAgent = agent;
        return this;
    }

    /**
     * @param type the Content-Type
     * @return this
     */
    public HTTPRequest contentType(final String type) {
        return this.header(WSConstants.HEADER_CONTENT_TYPE, type);
    }

    // #######################
    // Some header shortcuts
    // #######################

    /**
     * @param authString the Authorization header
     * @return this
     */
    public HTTPRequest auth(final String authString) {
        return this.header(WSConstants.HEADER_AUTHORIZATION, authString);
    }

    /**
     * @param user     the username
     * @param password the password
     * @return this
     */
    public HTTPRequest authBasic(final String user, final String password) {
        if((user == null) || (password == null)) {
            throw new IllegalArgumentException("Neither user nor password can be null");
        }
        if(user.contains(":")) {
            throw new IllegalArgumentException("Colon not allowed in user according to RFC2617 Sec. 2");
        }
        final String credentials = user + ":" + password;
        final String auth = Base64.encodeBase64String(credentials.getBytes());
        return this.auth("Basic " + auth);
    }

    /**
     * @param accessToken the OAuth2 Bearer access token
     * @return this
     */
    public HTTPRequest authBearer(final String accessToken) {
        return this.auth("Bearer " + accessToken);
    }

    /**
     * @param type the Accept type
     * @return this
     */
    public HTTPRequest accept(final String type) {
        return this.header(WSConstants.HEADER_ACCEPT, type);
    }

    /**
     * @param bodyString the body entity
     * @return this
     */
    public HTTPRequest body(final String bodyString) {
        this.body = bodyString;
        return this;
    }

    /**
     * @param form the form content
     * @return this
     */
    public HTTPRequest form(final Map<String, String> form) {
        final StringBuilder formString = new StringBuilder();
        final Iterator<Entry<String, String>> parts = form.entrySet().iterator();
        if(parts.hasNext()) {
            final Entry<String, String> firstEntry = parts.next();
            formString.append(firstEntry.getKey());
            formString.append("=");
            formString.append(firstEntry.getValue());
            while(parts.hasNext()) {
                final Entry<String, String> entry = parts.next();
                formString.append("&");
                formString.append(entry.getKey());
                formString.append("=");
                formString.append(entry.getValue());
            }
        }
        return this.contentType("application/x-www-form-urlencoded").body(formString.toString());
    }

    /**
     * @return the {@link HTTPResponse}
     */
    public HTTPResponse get() {
        return this.execute(Method.GET);
    }

    /**
     * @return the {@link HTTPResponse}
     */
    public HTTPResponse put() {
        return this.execute(Method.PUT);
    }

    /**
     * @return the {@link HTTPResponse}
     */
    public HTTPResponse patch() {
        return this.execute(Method.PATCH);
    }

    /**
     * @return the {@link HTTPResponse}
     */
    public HTTPResponse post() {
        return this.execute(Method.POST);
    }

    /**
     * @return the {@link HTTPResponse}
     */
    public HTTPResponse delete() {
        return this.execute(Method.DELETE);
    }

    /**
     * @return the {@link HTTPResponse}
     */
    public HTTPResponse options() {
        return this.execute(Method.OPTIONS);
    }

    /**
     * @return the {@link HTTPResponse}
     */
    public HTTPResponse head() {
        return this.execute(Method.HEAD);
    }

    /**
     * @param callback {@link HTTPResponseCallback}
     */
    public void getAsync(final HTTPResponseCallback callback) {
        this.getAsync(HTTPRequest.DEFAULT_EXECUTOR, callback);
    }

    /**
     * @param executor Thread pool
     * @param callback {@link HTTPResponseCallback}
     */
    public void getAsync(final Executor executor, final HTTPResponseCallback callback) {
        this.executeAsync(executor, Method.GET, callback);
    }

    /**
     * @param callback {@link HTTPResponseCallback}
     */
    public void putAsync(final HTTPResponseCallback callback) {
        this.putAsync(HTTPRequest.DEFAULT_EXECUTOR, callback);
    }

    /**
     * @param executor Thread pool
     * @param callback {@link HTTPResponseCallback}
     */
    public void putAsync(final Executor executor, final HTTPResponseCallback callback) {
        this.executeAsync(executor, Method.PUT, callback);
    }

    /**
     * @param callback {@link HTTPResponseCallback}
     */
    public void patchAsync(final HTTPResponseCallback callback) {
        this.patchAsync(HTTPRequest.DEFAULT_EXECUTOR, callback);
    }

    /**
     * @param executor Thread pool
     * @param callback {@link HTTPResponseCallback}
     */
    public void patchAsync(final Executor executor, final HTTPResponseCallback callback) {
        this.executeAsync(executor, Method.PATCH, callback);
    }

    /**
     * @param callback {@link HTTPResponseCallback}
     */
    public void postAsync(final HTTPResponseCallback callback) {
        this.postAsync(HTTPRequest.DEFAULT_EXECUTOR, callback);
    }

    /**
     * @param executor Thread pool
     * @param callback {@link HTTPResponseCallback}
     */
    public void postAsync(final Executor executor, final HTTPResponseCallback callback) {
        this.executeAsync(executor, Method.POST, callback);
    }

    /**
     * @param callback {@link HTTPResponseCallback}
     */
    public void deleteAsync(final HTTPResponseCallback callback) {
        this.deleteAsync(HTTPRequest.DEFAULT_EXECUTOR, callback);
    }

    /**
     * @param executor Thread pool
     * @param callback {@link HTTPResponseCallback}
     */
    public void deleteAsync(final Executor executor, final HTTPResponseCallback callback) {
        this.executeAsync(executor, Method.DELETE, callback);
    }

    /**
     * @param callback {@link HTTPResponseCallback}
     */
    public void optionsAsync(final HTTPResponseCallback callback) {
        this.optionsAsync(HTTPRequest.DEFAULT_EXECUTOR, callback);
    }

    /**
     * @param executor Thread pool
     * @param callback {@link HTTPResponseCallback}
     */
    public void optionsAsync(final Executor executor, final HTTPResponseCallback callback) {
        this.executeAsync(executor, Method.OPTIONS, callback);
    }

    /**
     * @param callback {@link HTTPResponseCallback}
     */
    public void headAsync(final HTTPResponseCallback callback) {
        this.headAsync(HTTPRequest.DEFAULT_EXECUTOR, callback);
    }

    /**
     * @param executor Thread pool
     * @param callback {@link HTTPResponseCallback}
     */
    public void headAsync(final Executor executor, final HTTPResponseCallback callback) {
        this.executeAsync(executor, Method.HEAD, callback);
    }

    private void executeAsync(final Executor executor, final Method method, final HTTPResponseCallback cb) {
        final Runnable execute = () -> {
            final HTTPResponse res;
            try {
                res = HTTPRequest.this.execute(method);
            } catch(final Exception e) {
                cb.fail(e);
                return;
            }
            try {
                // TODO find a solution for exceptions thrown in callback
                cb.response(res);
            } finally {
                res.close();
            }
        };
        executor.execute(execute);
    }

    private HTTPResponse execute(final Method method) {
        final URI uri = this.buildURI();
        // attempt == 0 is not a retry, attempt > 0 are retries
        for(int attempt = 0; attempt <= this.maxRetries; attempt++) {
            if(attempt > 0) {
                final int wait = this.waitStrategy.milliseconds(attempt - 1);
                if(wait > 0) {
                    try {
                        Thread.sleep(wait);
                    } catch(final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            try {
                final HTTPResponse response = this.attempt(method, uri);
                if(this.retryable != null) {
                    final int statusCode = response.getStatus();
                    if(this.retryable.retry(Optional.empty(), Optional.of(statusCode))) {
                        response.close(); // we are not interested in the body
                        if(attempt < this.maxRetries) {
                            continue; // retry
                        } else {
                            throw new RuntimeException("status code " + statusCode);
                        }
                    }
                }
                return response;
            } catch(final IOException e) {
                if(this.retryable != null) {
                    if(attempt < this.maxRetries) {
                        continue; // retry
                    } else {
                        throw new RuntimeException("retry exhausted", e);
                    }
                } else {
                    throw new RuntimeException(e);
                }
            } catch(final RuntimeException e) {
                if(this.retryable != null) {
                    if(attempt < this.maxRetries) {
                        continue; // retry
                    } else {
                        throw new RuntimeException("retry exhausted", e);
                    }
                } else {
                    throw e;
                }
            }
        }
        throw new RuntimeException("retry failed"); // should never be reached
    }

    private HTTPResponse attempt(final Method method, final URI uri) throws IOException {
        // prepare request configuration
        final Builder requestConfigBuilder = RequestConfig.custom();
        if(this.timeout != null) {
            requestConfigBuilder.setConnectTimeout(this.timeout);
            requestConfigBuilder.setConnectionRequestTimeout(this.timeout);
            requestConfigBuilder.setSocketTimeout(this.timeout);
        }
        requestConfigBuilder.setRedirectsEnabled(this.followRedirect);
        if (this.proxy != null) {
            requestConfigBuilder.setProxy(this.proxy);
        }
        final RequestConfig requestConfig = requestConfigBuilder.build();

        // prepare request
        final HttpRequestBase request = method.request(uri);
        request.setConfig(requestConfig);
        if((this.userAgent != null) && !this.userAgent.isEmpty()) {
            request.setHeader(WSConstants.HEADER_USER_AGENT, this.userAgent);
        }
        for(final Entry<String, List<String>> entry : this.headers.entrySet()) {
            final List<String> list = entry.getValue();
            for(final String string : list) {
                request.addHeader(entry.getKey(), string);
            }
        }
        if(request instanceof HttpEntityEnclosingRequestBase) {
            final HttpEntityEnclosingRequestBase entityBase = (HttpEntityEnclosingRequestBase) request;
            entityBase.setEntity(new StringEntity(this.body, "UTF-8"));
        }

        return new HTTPResponse(DEFAULT_HTTP_CLIENT.execute(request));
    }

    private URI buildURI() {
        try {
            String u = this.url;
            for(final Entry<String, String> pathEntry : this.pathParams.entrySet()) {
                u = u.replace("{" + pathEntry.getKey() + "}", pathEntry.getValue());
            }
            final URIBuilder builder = new URIBuilder(u);
            final Set<Entry<String, List<String>>> entrySet = this.queryParams.entrySet();
            for(final Entry<String, List<String>> entry : entrySet) {
                final List<String> list = entry.getValue();
                for(final String string : list) {
                    builder.addParameter(entry.getKey(), string);
                }
            }
            return builder.build();
        } catch(final URISyntaxException e) {
            throw new RuntimeException("Invalid URI", e);
        }
    }

    public enum Method implements Request {
        GET {
            @Override
            public HttpRequestBase request(final URI uri) {
                return new HttpGet(uri);
            }
        }, HEAD {
            @Override
            public HttpRequestBase request(final URI uri) {
                return new HttpHead(uri);
            }
        }, POST {
            @Override
            public HttpRequestBase request(final URI uri) {
                return new HttpPost(uri);
            }
        }, PUT {
            @Override
            public HttpRequestBase request(final URI uri) {
                return new HttpPut(uri);
            }
        }, DELETE {
            @Override
            public HttpRequestBase request(final URI uri) {
                return new HttpDelete(uri);
            }
        }, PATCH {
            @Override
            public HttpRequestBase request(final URI uri) {
                return new HttpPatch(uri);
            }
        }, OPTIONS {
            @Override
            public HttpRequestBase request(final URI uri) {
                return new HttpOptions(uri);
            }
        }
    }

    interface Request {
        HttpRequestBase request(URI uri);
    }

}
