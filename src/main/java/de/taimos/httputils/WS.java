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
	 * @param url the base URL
	 * @return the created {@link HTTPRequest}
	 */
	public static HTTPRequest url(final String url) {
		return new HTTPRequest(url);
	}
	
	/**
	 * @param response the {@link HttpResponse}
	 * @return String the body as UTF-8 string
	 */
	public static String getResponseAsString(final HttpResponse response) {
		try {
			return EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (final ParseException e) {
			throw new RuntimeException(e);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @param response Response
	 * @return String
	 */
	public static int getStatus(final HttpResponse response) {
		return response.getStatusLine().getStatusCode();
	}
	
	/**
	 * @param response Response
	 * @return true if status code if between 200 and 299
	 */
	public static boolean isStatusOK(final HttpResponse response) {
		final int code = WS.getStatus(response);
		return (code >= 200) && (code <= 299);
	}
	
	/**
	 * @param response Response
	 * @return true if status code if between 300 and 399
	 */
	public static boolean isStatusRedirect(final HttpResponse response) {
		final int code = WS.getStatus(response);
		return (code >= 300) && (code <= 399);
	}
	
	/**
	 * @param response Response
	 * @return true if status code if between 400 and 499
	 */
	public static boolean isStatusClientError(final HttpResponse response) {
		final int code = WS.getStatus(response);
		return (code >= 400) && (code <= 499);
	}
	
	/**
	 * @param response Response
	 * @return true if status code if between 500 and 599
	 */
	public static boolean isStatusServerError(final HttpResponse response) {
		final int code = WS.getStatus(response);
		return (code >= 500) && (code <= 599);
	}
}
