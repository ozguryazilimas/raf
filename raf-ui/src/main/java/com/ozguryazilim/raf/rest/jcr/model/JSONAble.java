package com.ozguryazilim.raf.rest.jcr.model;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * An interface which should be implemented by objects that provide a JSON (currently Jettison) implementation.
 * 
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public interface JSONAble {
    /**
     * Returns the JSON representation of this object.
     * 
     * @return a {@code non-null} {@link JSONObject}
     * @throws JSONException if conversion to JSON is not possible.
     */
    public JSONObject toJSON() throws JSONException;
}
