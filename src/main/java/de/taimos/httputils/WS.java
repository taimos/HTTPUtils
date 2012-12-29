package de.taimos.httputils;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

/**
 * @author thoeger
 * 
 */
public final class WS {

	private WS() {
		//
	}

	/**
	 * @param url
	 *            the base URL
	 * @return the created {@link HTTPRequest}
	 */
	public static HTTPRequest url(final String url) {
		return new HTTPRequest(url);
	}

	/**
	 * @param response
	 *            the {@link HttpResponse}
	 * @return String the body as UTF-8 string
	 */
	public static String getResponseAsString(final HttpResponse response) {
		try {
			return EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (final ParseException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * @param response
	 * @return String
	 */
	public static int getStatus(final HttpResponse response) {
		return response.getStatusLine().getStatusCode();
	}
}
