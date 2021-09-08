package com.ozguryazilim.raf.elasticsearch.search;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.DetailedSearchModel;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.telve.auth.Identity;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class ElasticSearchService implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);

    @Inject
    private RafService rafService;

    @Inject
    private RafMemberService rafMemberService;

    @Inject
    private RafPathMemberService rafPathMemberService;

    @Inject
    private Identity identity;
    private WebResource myWebResource;
    private Client myClient;
    private List extendedQuery;
    private List extendedSortQuery;

    Gson gson = new Gson();

    WebResource getWebResource() {
        if (myClient == null) {
            myClient = Client.create();
        }
        if (myWebResource == null) {
            myWebResource = myClient.resource(String.format("http://%s:%d", ConfigResolver.getPropertyValue("elasticSearch.server", "localhost"), Integer.parseInt(ConfigResolver.getPropertyValue("elasticSearch.port", "9200"))));
        }
        return myWebResource;
    }

    public long detailedSearchCount(DetailedSearchModel searchModel, List<RafDefinition> rafs, List extendedQuery) throws RafException {
        long result = 0;
        this.extendedQuery = extendedQuery;
        String query = getSearchQuery(searchModel, rafs, 0, 0);
        if (!Strings.isNullOrEmpty(query)) {
            ClientResponse clientResponse = getWebResource().path("_count").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, query);
            if (clientResponse != null && clientResponse.hasEntity()) {
                String output = clientResponse.getEntity(String.class);
                Map dboMsg = gson.fromJson(output, Map.class);
                if (dboMsg != null && dboMsg.containsKey("count")) {
                    result = Math.round(Double.parseDouble(dboMsg.get("count").toString()));
                }
            }
        }
        return result;
    }

    public RafCollection detailedSearch(DetailedSearchModel searchModel, List<RafDefinition> rafs, int limit, int offset, String sortField, SortOrder sortOrder, List extendedQuery, List extendedSortQuery) throws RafException {
        this.extendedQuery = extendedQuery;
        this.extendedSortQuery = extendedSortQuery;
        RafCollection result = new RafCollection();
        result.setId("SEARCH");
        result.setMimeType("raf/search");
        result.setTitle("Detay Arama");
        result.setPath("SEARCH");
        result.setName("Detay Arama");
        String query = getSearchQuery(searchModel, rafs, limit, offset);

        if (!Strings.isNullOrEmpty(query)) {
            ClientResponse clientResponse = getWebResource().path("_search").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, query);
            if (clientResponse != null && clientResponse.hasEntity()) {
                String output = clientResponse.getEntity(String.class);
                Map dboMsg = gson.fromJson(output, Map.class);
                if (dboMsg.containsKey("hits")) {
                    List<Map<String, Object>> list = new ArrayList();
                    if (dboMsg.containsKey("hits") && ((Map) dboMsg.get("hits")).get("hits") instanceof List) {
                        list = (List) ((Map) dboMsg.get("hits")).get("hits");
                        for (Object dbo : list) {
                            Map bDbo = (Map) dbo;
                            if (bDbo != null && bDbo.containsKey("_source")) {
                                Map dboSource = (Map) bDbo.get("_source");
                                if (dboSource.containsKey("filePath")) {
                                    String filePath = dboSource.get("filePath").toString();
                                    if (identity != null && !Strings.isNullOrEmpty(identity.getLoginName())) {
                                        if (!rafPathMemberService.hasMemberInPath(identity.getLoginName(), filePath) || rafPathMemberService.hasReadRole(identity.getLoginName(), filePath)) {
                                            try {
                                                result.getItems().add(rafService.getRafObjectByPath(filePath));
                                            } catch (RafException e) {
                                                LOG.error("RafEx", e);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
        return result;
    }

    public String getSearchQuery(DetailedSearchModel searchModel, List<RafDefinition> rafs, int limit, int offset) {
        Map result = new HashMap();
        List mustQueryList = new ArrayList();
        mustQueryList.addAll(extendedQuery);

        Map must = new HashMap();
        Map bool = new HashMap();
        must.put("must", mustQueryList);
        bool.put("bool", must);
        result.put("query", bool);
        if (extendedSortQuery != null && !extendedQuery.isEmpty()) {
            result.put("sort", extendedSortQuery);
        }
        result.put("from", offset);
        result.put("size", limit);
        LOG.debug("ES Search Query : {}", result.toString());
        return gson.toJson(result);
    }
}
