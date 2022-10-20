package com.ozguryazilim.raf.rest.jcr.model;

import java.util.Collections;
import java.util.List;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * A REST representation of the {@link javax.jcr.Property}
 * 
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public final class RestProperty extends RestItem {

    private final List<String> values;
    private final boolean multiValued;

    /**
     * Creates a new rest property instance.
     * 
     * @param name a {@code non-null} string, the name of the property
     * @param url a {@code non-null} string, the absolute url to this property
     * @param parentUrl a {@code non-null} string, the absolute url to this property's parent
     * @param values a list of possible values for the property, may be {@code null}
     * @param multiValued true if this property is a multi-valued property
     */
    public RestProperty( String name,
                         String url,
                         String parentUrl,
                         List<String> values,
                         boolean multiValued ) {
        super(name, url, parentUrl);
        this.values = values != null ? values : Collections.<String>emptyList();
        this.multiValued = multiValued;
    }

    List<String> getValues() {
        return values;
    }

    String getValue() {
        return !values.isEmpty() ? values.get(0) : null;
    }

    boolean isMultiValue() {
        return multiValued;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();
        if (isMultiValue()) {
            object.put("values", values);
        } else if (getValue() != null) {
            object.put(name, getValue());
        }
        object.put("self", url);
        object.put("up", parentUrl);
        return object;
    }
}
