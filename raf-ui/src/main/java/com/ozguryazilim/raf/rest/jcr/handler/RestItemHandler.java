package com.ozguryazilim.raf.rest.jcr.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import com.ozguryazilim.raf.rest.jcr.model.RestItem;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.modeshape.common.util.StringUtil;

/**
 * An extension to the {@link ItemHandler} which is used by {@link com.ozguryazilim.raf.rest.jcr.JcrRest} to interact
 * with properties and nodes.
 *
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public final class RestItemHandler extends ItemHandler {

    /**
     * Retrieves the JCR {@link Item} at the given path, returning its rest representation.
     *
     * @param request the servlet request; may not be null or unauthenticated
     * @param path the path to the item
     * @param depth the depth of the node graph that should be returned if {@code path} refers to a node. @{code 0} means return
     *        the requested node only. A negative value indicates that the full subgraph under the node should be returned. This
     *        parameter defaults to {@code 0} and is ignored if {@code path} refers to a property.
     * @return a the rest representation of the item, as a {@link RestItem} instance.
     * @throws RepositoryException if any JCR operations fail.
     */
    public RestItem item( HttpServletRequest request,
                          String path,
                          int depth ) throws RepositoryException {
        Session session = getSession(request);
        Item item = itemAtPath(path, session);
        return createRestItem(request, depth, session, item);
    }

    @Override
    protected JSONObject getProperties( JSONObject jsonNode ) throws JSONException {
        JSONObject properties = new JSONObject();
        for (Iterator<?> keysIterator = jsonNode.keys(); keysIterator.hasNext();) {
            String key = keysIterator.next().toString();
            if (CHILD_NODE_HOLDER.equalsIgnoreCase(key)) {
                continue;
            }
            properties.put(key, jsonNode.get(key));
        }
        return properties;
    }

    private String newNodeName( String path ) {
        int lastSlashInd = path.lastIndexOf('/');
        String name = lastSlashInd == -1 ? path : path.substring(lastSlashInd + 1);
        // Remove any SNS index ...
        name = name.replaceAll("\\[\\d+\\]$", "");
        return name;
    }

    private JSONObject stringToJSONObject( String requestBody ) throws JSONException {
        return StringUtil.isBlank(requestBody) ? new JSONObject() : new JSONObject(requestBody);
    }

    private JSONArray stringToJSONArray( String requestBody ) throws JSONException {
        return StringUtil.isBlank(requestBody) ? new JSONArray() : new JSONArray(requestBody);
    }

    private Response createOkResponse( final List<RestItem> result ) {
        GenericEntity<List<RestItem>> entity = new GenericEntity<List<RestItem>>(result) {};
        return Response.ok().entity(entity).build();
    }
}
