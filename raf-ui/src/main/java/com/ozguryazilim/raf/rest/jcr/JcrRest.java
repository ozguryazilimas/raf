package com.ozguryazilim.raf.rest.jcr;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.rest.jcr.handler.RestBinaryHandler;
import com.ozguryazilim.raf.rest.jcr.handler.RestItemHandler;
import com.ozguryazilim.raf.rest.jcr.handler.RestQueryHandler;
import com.ozguryazilim.raf.rest.jcr.model.IndexDefinitionPayload;
import com.ozguryazilim.raf.rest.jcr.model.RestQueryPlanResult;
import com.ozguryazilim.raf.rest.jcr.model.RestQueryResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.codehaus.jettison.json.JSONException;
import org.modeshape.jcr.api.index.IndexDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;

@RequiresPermissions("admin")
@Path("/api/jcr")
public class JcrRest implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(JcrRest.class);

    @Inject
    private RafService rafService;

    private RestItemHandler itemHandler = new RestItemHandler();
    private RestQueryHandler queryHandler = new RestQueryHandler();
    private RestBinaryHandler binaryHandler = new RestBinaryHandler();

    @GET
    @Path("/indexes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIndexes(@QueryParam("index") String indexName) {
        Map<String, IndexDefinitionPayload> indexDefinitionPayloadMap;
        try {
            indexDefinitionPayloadMap = getIndexDefinitionPayloadMap();
        } catch (RafException ex) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(ex.getMessage()).build();
        }

        if (indexName != null && !indexName.isEmpty()) {
            if (!indexDefinitionPayloadMap.containsKey(indexName)) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(String.format("Could not found index named \"%s\"", indexName))
                        .build();
            } else {
                return Response.ok(indexDefinitionPayloadMap.get(indexName)).build();
            }
        } else {
            return Response.ok(indexDefinitionPayloadMap).build();
        }
    }

    @GET
    @Path("/indexes/unregister")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unregisterIndex(@QueryParam("index") String indexName) {
        Map<String, IndexDefinitionPayload> indexDefinitionPayloadMap;

        try {
            indexDefinitionPayloadMap = getIndexDefinitionPayloadMap();

            if (!indexName.isEmpty()) {
                if (!indexDefinitionPayloadMap.containsKey(indexName)) {

                    return Response
                            .status(Response.Status.NOT_FOUND)
                            .entity(String.format("Could not found index named \"%s\"", indexName))
                            .build();
                } else {
                    rafService.unregisterIndexes(indexName);
                    indexDefinitionPayloadMap = getIndexDefinitionPayloadMap();
                    return Response.ok(indexDefinitionPayloadMap).build();
                }
            } else {
                return Response.ok(indexDefinitionPayloadMap).build();
            }
        } catch (RafException ex) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("/indexes/reindex")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reindexAsync(
            @FormParam("path") String path,
            @DefaultValue("true") @FormParam("isAsync") Boolean isAsync ) {
        try {
            rafService.reindex(path, isAsync);
            return Response.ok().build();
        } catch (RafException ex) {
            LOG.error("Error while async reindexing", ex);
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    @SuppressWarnings("deprecation")
    @POST
    @Path("/query")
    @Consumes("application/jcr+xpath")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN})
    public Response postXPathQuery(@Context HttpServletRequest request,
                                          @Context HttpServletResponse response,
                                          @QueryParam("offset") @DefaultValue("-1") long offset,
                                          @QueryParam("limit") @DefaultValue("-1") long limit,
                                          @Context UriInfo uriInfo,
                                          String requestContent) throws JSONException {
        try {
            RestQueryResult queryResult = queryHandler.executeQuery(request, Query.XPATH, requestContent, offset, limit, uriInfo);
            return Response.ok(queryResult.toJSON().toString()).build();
        } catch (RepositoryException ex) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(ex.getMessage()).build();
        }
    }

    @SuppressWarnings("deprecation")
    @POST
    @Path("/queryPlan")
    @Consumes("application/jcr+xpath")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN})
    public Response postXPathQueryPlan(@Context HttpServletRequest request,
                                                  @Context HttpServletResponse response,
                                                  @QueryParam("offset") @DefaultValue("-1") long offset,
                                                  @QueryParam("limit") @DefaultValue("-1") long limit,
                                                  @Context UriInfo uriInfo,
                                                  String requestContent) throws JSONException {
        try {
            RestQueryPlanResult queryPlanResult = queryHandler.planQuery(request, Query.XPATH, requestContent, offset, limit, uriInfo);
            return Response.ok(queryPlanResult.toJSON().toString()).build();
        } catch (RepositoryException ex) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("/query")
    @Consumes("application/jcr+sql2")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN})
    public Response postJcrSql2Query(@Context HttpServletRequest request,
                                            @Context HttpServletResponse response,
                                            @QueryParam("offset") @DefaultValue("-1") long offset,
                                            @QueryParam("limit") @DefaultValue("-1") long limit,
                                            @Context UriInfo uriInfo,
                                            String requestContent) throws JSONException {
        try {
            RestQueryResult queryResult = queryHandler.executeQuery(request, Query.JCR_SQL2, requestContent, offset, limit, uriInfo);
            return Response.ok(queryResult.toJSON().toString()).build();
        } catch (RepositoryException ex) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("/queryPlan")
    @Consumes("application/jcr+sql2")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN})
    public Response postJcrSql2QueryPlan(@Context HttpServletRequest request,
                                                    @Context HttpServletResponse response,
                                                    @QueryParam("offset") @DefaultValue("-1") long offset,
                                                    @QueryParam("limit") @DefaultValue("-1") long limit,
                                                    @Context UriInfo uriInfo,
                                                    String requestContent) throws JSONException {
        try {
            RestQueryPlanResult queryPlanResult = queryHandler.planQuery(request, Query.JCR_SQL2, requestContent, offset, limit, uriInfo);
            return Response.ok(queryPlanResult.toJSON().toString()).build();
        } catch (RepositoryException ex) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(ex.getMessage()).build();
        }
    }

    private Map<String, IndexDefinitionPayload> getIndexDefinitionPayloadMap() throws RafException {
        Map<String, IndexDefinition> indexes = rafService.getIndexDefinitions();
        return indexes.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new IndexDefinitionPayload(e.getValue())));
    }

}
