package de.taimos.httputils;

/*
 * #%L
 * Taimos HTTPUtils
 * %%
 * Copyright (C) 2012 - 2013 Taimos GmbH
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


/**
 * @author thoeger
 * 
 */
public interface WSConstants {

	// Header names

	/** Authentication credentials for HTTP authentication */
	String HEADER_AUTHORIZATION = "Authorization";

	/** Content-Types that are acceptable */
	String HEADER_ACCEPT = "Accept";

	/** Character sets that are acceptable */
	String HEADER_ACCEPT_CHARSET = "Accept-Charset";

	/** Acceptable encodings */
	String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

	/** Acceptable languages for response */
	String HEADER_ACCEPT_LANGUAGE = "Accept-Language";

	/** What type of connection the user-agent would prefer */
	String HEADER_CONNECTION = "Connection";

	/** an HTTP cookie previously sent by the server with Set-Cookie */
	String HEADER_COOKIE = "Cookie";

	/** Used to specify directives that MUST be obeyed by all caching mechanisms along the request/response chain */
	String HEADER_CACHE_CONTROL = "Cache-Control";

	/** The length of the request body in octets (8-bit bytes) */
	String HEADER_CONTENT_LENGTH = "Content-Length";

	/** A Base64-encoded binary MD5 sum of the content of the request body */
	String HEADER_CONTENT_MD5 = "Content-MD5";

	/** The MIME type of the body of the request (used with POST and PUT requests) */
	String HEADER_CONTENT_TYPE = "Content-Type";

	/** The date and time that the message was sent */
	String HEADER_DATE = "Date";

	/** Indicates that particular server behaviors are required by the client */
	String HEADER_EXPECT = "Expect";

	/** The email address of the user making the request */
	String HEADER_FROM = "From";

	/** Only perform the action if the client supplied entity matches the same entity on the server */
	String HEADER_IF_MATCH = "If-Match";

	/** Allows a 304 Not Modified to be returned if content is unchanged */
	String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";

	/** Allows a 304 Not Modified to be returned if content is unchanged */
	String HEADER_IF_NONE_MATCH = "If-None-Match";

	/** If the entity is unchanged, send me the part(s) that I am missing; otherwise, send me the entire new entity */
	String HEADER_IF_RANGE = "If-Range";

	/** Only send the response if the entity has not been modified since a specific time */
	String HEADER_IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

	/** Limit the number of times the message can be forwarded through proxies or gateways */
	String HEADER_MAX_FORWARDS = "Max-Forwards";

	/** Implementation-specific headers that may have various effects anywhere along the request-response chain */
	String HEADER_PRAGMA = "Pragma";

	/** Authorization credentials for connecting to a proxy */
	String HEADER_PROXY_AUTHORIZATION = "Proxy-Authorization";

	/** Request only part of an entity. Bytes are numbered from 0. */
	String HEADER_RANGE = "Range";

	/** This is the address of the previous web page from which a link to the currently requested page was followed */
	String HEADER_REFERER = "Referer";

	/** The transfer encodings the user agent is willing to accept */
	String HEADER_TARNSFER_ENCODING = "TE";

	/** Ask the server to upgrade to another protocol */
	String HEADER_UPGRADE = "Upgrade";

	/** The user agent string of the user agent */
	String HEADER_USER_AGENT = "User-Agent";

	/** Informs the server of proxies through which the request was sent */
	String HEADER_VIA = "Via";

	/** A general warning about possible problems with the entity body */
	String HEADER_WARNING = "Warning";

}
