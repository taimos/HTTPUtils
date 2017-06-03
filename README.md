httputils
=========

HTTP Utilities

[![Build Status](https://travis-ci.org/taimos/HTTPUtils.png?branch=master)](https://travis-ci.org/taimos/HTTPUtils)

Setup
=====

```
<dependency>
    <groupId>de.taimos</groupId>
    <artifactId>httputils</artifactId>
    <version>someversion</version>
</dependency>
```

Usage
=====

### Create and execute request

	final HttpResponse response = WS.url("http://www.heise.de").get();
	
### Get status code

	Assert.assertEquals(WS.getStatus(response), 200);
	
### Get response body

	final String body = WS.getResponseAsString(response);