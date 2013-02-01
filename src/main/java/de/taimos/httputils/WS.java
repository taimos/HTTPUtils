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

	/**
	 * @param response
	 * @return true if status code if between 200 and 299
	 */
	public static boolean isStatusOK(final HttpResponse response) {
		final int code = WS.getStatus(response);
		return code >= 200 && code <= 299;
	}

	/**
	 * @param response
	 * @return true if status code if between 300 and 399
	 */
	public static boolean isStatusRedirect(final HttpResponse response) {
		final int code = WS.getStatus(response);
		return code >= 300 && code <= 399;
	}

	/**
	 * @param response
	 * @return true if status code if between 400 and 499
	 */
	public static boolean isStatusClientError(final HttpResponse response) {
		final int code = WS.getStatus(response);
		return code >= 400 && code <= 499;
	}

	/**
	 * @param response
	 * @return true if status code if between 500 and 599
	 */
	public static boolean isStatusServerError(final HttpResponse response) {
		final int code = WS.getStatus(response);
		return code >= 500 && code <= 599;
	}
}
