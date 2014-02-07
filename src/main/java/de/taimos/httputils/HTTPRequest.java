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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * @author thoeger
 * 
 */
public class HTTPRequest {
	
	private static Executor executor = Executors.newCachedThreadPool();
	
	private final String url;
	
	private final HashMap<String, List<String>> headers = new HashMap<>();
	
	private final HashMap<String, List<String>> queryParams = new HashMap<>();
	
	private final HashMap<String, String> pathParams = new HashMap<>();
	
	private Integer timeout;
	
	private boolean followRedirect = true;
	
	private String body = "";
	
	private String userAgent = null;
	
	
	/**
	 * @param url URL
	 */
	public HTTPRequest(final String url) {
		this.url = url;
	}
	
	/**
	 * @param name the name of the header
	 * @param value the value of the header
	 * @return this
	 */
	public HTTPRequest header(final String name, final String value) {
		if (!this.headers.containsKey(name)) {
			this.headers.put(name, new ArrayList<String>());
		}
		this.headers.get(name).add(value);
		return this;
	}
	
	/**
	 * @param name the name of the query parameter
	 * @param value the value of the query parameter
	 * @return this
	 */
	public HTTPRequest queryParam(final String name, final String value) {
		if (!this.queryParams.containsKey(name)) {
			this.queryParams.put(name, new ArrayList<String>());
		}
		this.queryParams.get(name).add(value);
		return this;
	}
	
	/**
	 * @param name the name of the path parameter
	 * @param value the value of the path parameter
	 * @return this
	 */
	public HTTPRequest pathParam(final String name, final String value) {
		this.pathParams.put(name, value);
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
	public HTTPRequest followRedirect(boolean follow) {
		this.followRedirect = follow;
		return this;
	}
	
	/**
	 * @param agent the user agent string to use
	 * @return this
	 */
	public HTTPRequest userAgent(String agent) {
		this.userAgent = agent;
		return this;
	}
	
	// #######################
	// Some header shortcuts
	// #######################
	
	/**
	 * @param type the Content-Type
	 * @return this
	 */
	public HTTPRequest contentType(final String type) {
		return this.header(WSConstants.HEADER_CONTENT_TYPE, type);
	}
	
	/**
	 * @param authString the Authorization header
	 * @return this
	 */
	public HTTPRequest auth(final String authString) {
		return this.header(WSConstants.HEADER_AUTHORIZATION, authString);
	}
	
	/**
	 * @param user the username
	 * @param password the password
	 * @return this
	 */
	public HTTPRequest authBasic(final String user, final String password) {
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
	public HTTPRequest form(Map<String, String> form) {
		StringBuilder formString = new StringBuilder();
		Iterator<Entry<String, String>> parts = form.entrySet().iterator();
		if (parts.hasNext()) {
			formString.append(parts.next().getKey());
			formString.append("=");
			formString.append(parts.next().getValue());
			while (parts.hasNext()) {
				formString.append("&");
				formString.append(parts.next().getKey());
				formString.append("=");
				formString.append(parts.next().getValue());
			}
		}
		return this.contentType("application/x-www-form-urlencoded").body(formString.toString());
	}
	
	/**
	 * @return the {@link HttpResponse}
	 */
	public HttpResponse get() {
		return this.execute(new HttpGet(this.buildURI()));
	}
	
	/**
	 * @return the {@link HttpResponse}
	 */
	public HttpResponse put() {
		return this.execute(new HttpPut(this.buildURI()));
	}
	
	/**
	 * @return the {@link HttpResponse}
	 */
	public HttpResponse patch() {
		return this.execute(new HttpPatch(this.buildURI()));
	}
	
	/**
	 * @return the {@link HttpResponse}
	 */
	public HttpResponse post() {
		return this.execute(new HttpPost(this.buildURI()));
	}
	
	/**
	 * @return the {@link HttpResponse}
	 */
	public HttpResponse delete() {
		return this.execute(new HttpDelete(this.buildURI()));
	}
	
	/**
	 * @return the {@link HttpResponse}
	 */
	public HttpResponse options() {
		return this.execute(new HttpOptions(this.buildURI()));
	}
	
	/**
	 * @return the {@link HttpResponse}
	 */
	public void getAsync(HTTPResponseCallback callback) {
		this.executeAsync(new HttpGet(this.buildURI()), callback);
	}
	
	/**
	 * @return the {@link HttpResponse}
	 */
	public void putAsync(HTTPResponseCallback callback) {
		this.executeAsync(new HttpPut(this.buildURI()), callback);
	}
	
	/**
	 * @return the {@link HttpResponse}
	 */
	public void patchAsync(HTTPResponseCallback callback) {
		this.executeAsync(new HttpPatch(this.buildURI()), callback);
	}
	
	/**
	 * @return the {@link HttpResponse}
	 */
	public void postAsync(HTTPResponseCallback callback) {
		this.executeAsync(new HttpPost(this.buildURI()), callback);
	}
	
	/**
	 * @return the {@link HttpResponse}
	 */
	public void deleteAsync(HTTPResponseCallback callback) {
		this.executeAsync(new HttpDelete(this.buildURI()), callback);
	}
	
	/**
	 * @return the {@link HttpResponse}
	 */
	public void optionsAsync(HTTPResponseCallback callback) {
		this.executeAsync(new HttpOptions(this.buildURI()), callback);
	}
	
	private void executeAsync(final HttpUriRequest req, final HTTPResponseCallback cb) {
		Runnable execute = new Runnable() {
			
			@Override
			public void run() {
				try {
					HttpResponse res = HTTPRequest.this.execute(req);
					cb.response(res);
				} catch (Exception e) {
					cb.fail(e);
				}
			}
		};
		HTTPRequest.executor.execute(execute);
	}
	
	private HttpResponse execute(final HttpUriRequest req) {
		HttpClientBuilder builder = HttpClientBuilder.create();
		Builder reqConfig = RequestConfig.custom();
		if (this.timeout != null) {
			reqConfig.setConnectTimeout(this.timeout);
		}
		reqConfig.setRedirectsEnabled(this.followRedirect);
		builder.setDefaultRequestConfig(reqConfig.build());
		if ((this.userAgent != null) && !this.userAgent.isEmpty()) {
			builder.setUserAgent(this.userAgent);
		}
		try {
			final CloseableHttpClient httpclient = builder.build();
			// if request has data populate body
			if (req instanceof HttpEntityEnclosingRequestBase) {
				final HttpEntityEnclosingRequestBase entityBase = (HttpEntityEnclosingRequestBase) req;
				entityBase.setEntity(new StringEntity(this.body, "UTF-8"));
			}
			// Set headers
			final Set<Entry<String, List<String>>> entrySet = this.headers.entrySet();
			for (final Entry<String, List<String>> entry : entrySet) {
				final List<String> list = entry.getValue();
				for (final String string : list) {
					req.addHeader(entry.getKey(), string);
				}
			}
			
			final HttpResponse response = httpclient.execute(req);
			return response;
		} catch (final ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private URI buildURI() {
		try {
			String u = this.url;
			for (final Entry<String, String> pathEntry : this.pathParams.entrySet()) {
				u = u.replace("{" + pathEntry.getKey() + "}", pathEntry.getValue());
			}
			final URIBuilder builder = new URIBuilder(u);
			final Set<Entry<String, List<String>>> entrySet = this.queryParams.entrySet();
			for (final Entry<String, List<String>> entry : entrySet) {
				final List<String> list = entry.getValue();
				for (final String string : list) {
					builder.addParameter(entry.getKey(), string);
				}
			}
			final URI uri = builder.build();
			return uri;
		} catch (final URISyntaxException e) {
			throw new RuntimeException("Invalid URI", e);
		}
	}
	
}
