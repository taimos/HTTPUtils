package de.taimos.httputils;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HTTPResponse implements AutoCloseable {

    private final HttpResponse response;

    public HTTPResponse(final HttpResponse response) {
        this.response = response;
    }

    public HttpResponse getResponse() {
        return response;
    }

    /**
     * @return String the body as UTF-8 string
     */
    public String getResponseAsString() {
        try {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (final ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return String the body as UTF-8 string
     */
    public byte[] getResponseAsBytes() {
        try {
            return EntityUtils.toByteArray(response.getEntity());
        } catch (final ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return String
     */
    public int getStatus() {
        return response.getStatusLine().getStatusCode();
    }

    /**
     * @return true if status code if between 200 and 299
     */
    public boolean isStatusOK() {
        final int code = this.getStatus();
        return (code >= 200) && (code <= 299);
    }

    /**
     * @return true if status code if between 300 and 399
     */
    public boolean isStatusRedirect() {
        final int code = this.getStatus();
        return (code >= 300) && (code <= 399);
    }

    /**
     * @return true if status code if between 400 and 499
     */
    public boolean isStatusClientError() {
        final int code = this.getStatus();
        return (code >= 400) && (code <= 499);
    }

    /**
     * @return true if status code if between 500 and 599
     */
    public boolean isStatusServerError() {
        final int code = this.getStatus();
        return (code >= 500) && (code <= 599);
    }

    @Override
    public void close() {
        try {
            this.response.getEntity().getContent().close();
        } catch (final IOException e) {
            // if the stream can not be created it doesn't need to be closed
            // if the stream is already closed it's also fine
        }
    }
}
