package com.ozguryazilim.raf.rest.jcr.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * A representation of an {@link Exception} which is used by the REST service to signal clients that a server-side exception
 * has occurred.
 *
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public final class RestException implements JSONAble {
    private final String message;
    private final String stackTrace;

    /**
     * Creates a new exception, using only a message
     *
     * @param message a {@code non-null} string
     */
    public RestException( String message ) {
        this.message = message;
        this.stackTrace = null;
    }

    /**
     * Creates a new exception, using a message and a {@link Throwable}
     *
     * @param message a {@code non-null} string
     * @param t a {@code non-null} {@link Exception}
     */
    public RestException( String message, Throwable t ) {
        this.message = message;
        StringWriter stringWriter = new StringWriter();
        t.printStackTrace(new PrintWriter(stringWriter));
        this.stackTrace = stringWriter.toString();
    }

    /**
     * Creates a new exception, based on an existing {@link Throwable}
     *
     * @param t a {@code non-null} {@link Exception}
     */
    public RestException( Throwable t ) {
        this.message = t.getMessage();
        StringWriter stringWriter = new StringWriter();
        t.printStackTrace(new PrintWriter(stringWriter));
        this.stackTrace = stringWriter.toString();
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("exception", message);
        if (stackTrace != null) {
            object.put("stacktrace", stackTrace);
        }
        return object;
    }
}
