package com.ozguryazilim.raf.rest.jcr.model;

import java.util.ArrayList;
import java.util.List;

import com.ozguryazilim.raf.rest.jcr.RestHelper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * A REST representation of a collection of {@link Workspace workspaces}
 *
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public final class RestWorkspaces implements JSONAble {

    private final List<Workspace> workspaces;

    /**
     * Creates an empty instance.
     */
    public RestWorkspaces() {
        this.workspaces = new ArrayList<Workspace>();
    }

    /**
     * Adds a new workspace to the list of workspaces.
     *
     * @param name a {@code non-null} string, the name of a workspace.
     * @param repositoryUrl a {@code non-null} string, the absolute url to the repository to which the workspace belongs.
     * @return a {@link Workspace} instance
     */
    public Workspace addWorkspace( String name,
                                   String repositoryUrl ) {
        Workspace workspace = new Workspace(name, repositoryUrl);
        workspaces.add(workspace);
        return workspace;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();
        JSONArray workspaces = new JSONArray();
        for (Workspace workspace : this.workspaces) {
            workspaces.put(workspace.toJSON());
        }
        result.put("workspaces", workspaces);
        return result;
    }

    private class Workspace implements JSONAble {
        private final String name;
        private final String repositoryUrl;
        private final String queryUrl;
        private final String itemsUrl;
        private final String binaryUrl;
        private final String nodeTypesUrl;

        public Workspace( String name,
                          String repositoryUrl ) {
            this.name = name;
            this.repositoryUrl = repositoryUrl;
            this.queryUrl = RestHelper.urlFrom(repositoryUrl, name, RestHelper.QUERY_METHOD_NAME);
            this.itemsUrl = RestHelper.urlFrom(repositoryUrl, name, RestHelper.ITEMS_METHOD_NAME);
            this.binaryUrl = RestHelper.urlFrom(repositoryUrl, name, RestHelper.BINARY_METHOD_NAME);
            this.nodeTypesUrl = RestHelper.urlFrom(repositoryUrl, name, RestHelper.NODE_TYPES_METHOD_NAME);
        }

        @Override
        public JSONObject toJSON() throws JSONException {
            JSONObject result = new JSONObject();
            result.put("name", name);
            result.put("repository", repositoryUrl);
            result.put("items", itemsUrl);
            result.put("query", queryUrl);
            result.put("binary", binaryUrl);
            result.put("nodeTypes", nodeTypesUrl);
            return result;
        }
    }
}
