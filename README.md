httputils
=========

HTTP Utilities

[![Build Status](https://travis-ci.org/taimos/httputils.png?branch=master)](https://travis-ci.org/taimos/httputils)

Usage
=====

### Create and execute request

	final HttpResponse response = WS.url("http://www.heise.de").get();
	
### Get status code

	Assert.assertEquals(WS.getStatus(response), 200);
	
### Get response body

	final String body = WS.getResponseAsString(response);