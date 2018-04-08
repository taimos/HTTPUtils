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

```
try(final HTTPResponse response = WS.url("https://www.google.de/").get()) {
    response.getStatus(); // Get status code
    response.getResponseAsString(); // Get response body
}
```