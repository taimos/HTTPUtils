package de.taimos.httputils;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

/**
 * @author thoeger
 * 
 */
public class WS {

	/**
	 * @param url
	 *            the base URL
	 * @return the created {@link HTTPRequest}
	 */
	public static HTTPRequest url(String url) {
		return new HTTPRequest(url);
	}

	/**
	 * @param response
	 * @return String
	 */
	public static String getResponseAsString(HttpResponse response) {
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
	public static int getStatus(HttpResponse response) {
		return response.getStatusLine().getStatusCode();
	}
}
