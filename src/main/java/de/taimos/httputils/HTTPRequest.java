package de.taimos.httputils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.SystemDefaultHttpClient;

/**
 * @author thoeger
 * 
 */
public class HTTPRequest {

	private final String url;

	private final HashMap<String, List<String>> headers = new HashMap<>();

	private final HashMap<String, List<String>> queryParams = new HashMap<>();

	private String body;

	/**
	 * @param url
	 */
	public HTTPRequest(String url) {
		this.url = url;
	}

	/**
	 * @param name
	 *            the name of the header
	 * @param value
	 *            the value of the header
	 * @return this
	 */
	public HTTPRequest header(String name, String value) {
		if (!this.headers.containsKey(name)) {
			this.headers.put(name, new ArrayList<String>());
		}
		this.headers.get(name).add(value);
		return this;
	}

	/**
	 * @param name
	 *            the name of the header
	 * @param value
	 *            the value of the header
	 * @return this
	 */
	public HTTPRequest queryParam(String name, String value) {
		try {
			final String encoded = URLEncoder.encode(value, "UTF-8");
			if (!this.queryParams.containsKey(name)) {
				this.queryParams.put(name, new ArrayList<String>());
			}
			this.queryParams.get(name).add(encoded);
			return this;
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param body
	 *            the body entity
	 * @return this
	 */
	public HTTPRequest body(String body) {
		this.body = body;
		return this;
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
	public HttpResponse post() {
		return this.execute(new HttpPost(this.buildURI()));
	}

	private HttpResponse execute(HttpUriRequest req) {
		try {
			final HttpClient httpclient = new SystemDefaultHttpClient();
			// if request has data populate body
			if (req instanceof HttpEntityEnclosingRequestBase) {
				final HttpEntityEnclosingRequestBase entityBase = (HttpEntityEnclosingRequestBase)req;
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
			final URIBuilder builder = new URIBuilder(this.url);
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
