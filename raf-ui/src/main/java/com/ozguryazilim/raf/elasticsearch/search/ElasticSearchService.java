package com.ozguryazilim.raf.elasticsearch.search;

import com.google.common.base.Strings;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
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
import org.primefaces.json.JSONObject;
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

    WebResource getWebResource() {
        if (myClient == null) {
            myClient = Client.create();
        }
        if (myWebResource == null) {
            myWebResource = myClient.resource(String.format("http://%s:%d", ConfigResolver.getPropertyValue("elasticSearch.server", "localhost"), Integer.parseInt(ConfigResolver.getPropertyValue("elasticSearch.port", "9200"))));
        }
        return myWebResource;
    }

    public long detailedSearchCount(DetailedSearchModel searchModel, List<RafDefinition> rafs) throws RafException {
        long result = 0;
        String query = getSearchQuery(searchModel, rafs, 0, 0);
        if (!Strings.isNullOrEmpty(query)) {
            ClientResponse clientResponse = getWebResource().path("_count").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, query);
            if (clientResponse != null && clientResponse.hasEntity()) {
                String output = clientResponse.getEntity(String.class);
                BasicDBObject dboMsg = BasicDBObject.parse(output);
                if (dboMsg != null && dboMsg.containsField("count")) {
                    result = Long.parseLong(dboMsg.get("count").toString());
                }
            }
        }
        return result;
    }

    public RafCollection detailedSearch(DetailedSearchModel searchModel, List<RafDefinition> rafs, int limit, int offset, String sortField, SortOrder sortOrder) throws RafException {
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
                BasicDBObject dboMsg = BasicDBObject.parse(output);
                if (dboMsg.containsField("hits")) {
                    BasicDBList list = new BasicDBList();
                    if (dboMsg.containsField("hits") && ((BasicDBObject) dboMsg.get("hits")).get("hits") instanceof BasicDBList) {
                        list = (BasicDBList) ((BasicDBObject) dboMsg.get("hits")).get("hits");
                        for (Object dbo : list) {
                            BasicDBObject bDbo = (BasicDBObject) dbo;
                            if (bDbo != null && bDbo.containsField("_source")) {
                                BasicDBObject dboSource = (BasicDBObject) bDbo.get("_source");
                                if (dboSource.containsField("filePath")) {
                                    String filePath = dboSource.getString("filePath");
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
        Map<String, String> sortFieldConvertMap = new HashMap();
        sortFieldConvertMap.put("nodes.[jcr:path]", "filePath");
        sortFieldConvertMap.put("nodes.[jcr:title]", "title");
        sortFieldConvertMap.put("nodes.[jcr:createBy]", "createBy");
        sortFieldConvertMap.put("nodes.[jcr:created]", "createDate");
        sortFieldConvertMap.put("nodes.[jcr:lastModifiedBy]", "updateBy");
        sortFieldConvertMap.put("nodes.[jcr:lastModified]", "updateDate");
        sortFieldConvertMap.put("exdoc.[externalDoc:documentCreator]", "externalDoc:documentCreator");
        sortFieldConvertMap.put("exdoc.[externalDoc:documentCreateDate]", "externalDoc:documentCreateDate");
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        BasicDBObject result = new BasicDBObject();
//        BasicDBList shouldRafList = new BasicDBList();
        BasicDBList mustQueryList = new BasicDBList();

        if (!Strings.isNullOrEmpty(searchModel.getSearchSubPath())) {
            mustQueryList.add(new BasicDBObject("wildcard", new BasicDBObject("filePath", searchModel.getSearchSubPath() + "*")));
        } else {
            List<String> rafCodes = new ArrayList();
            rafs.forEach((r) -> {
                rafCodes.add(r.getCode());
            });
            mustQueryList.add(new BasicDBObject("terms", new BasicDBObject("rafCode", rafCodes)));
        }

        if (searchModel.getDateFrom() != null) {
            mustQueryList.add(new BasicDBObject("range", new BasicDBObject("createDate", new BasicDBObject("gte", searchModel.getDateFrom().getTime()))));
        }

        if (searchModel.getDateTo() != null) {
            mustQueryList.add(new BasicDBObject("range", new BasicDBObject("createDate", new BasicDBObject("lte", searchModel.getDateTo().getTime()))));
        }

        if (!Strings.isNullOrEmpty(searchModel.getSearchText())) {
            Arrays.asList(searchModel.getSearchText().split(" ")).forEach((str) -> {
                mustQueryList.add(new BasicDBObject("wildcard", new BasicDBObject("title", "*" + str + "*")));
            });

        }

        if (!Strings.isNullOrEmpty(searchModel.getDocumentType())) {
            Arrays.asList(searchModel.getDocumentType().split(" ")).forEach((str) -> {
                mustQueryList.add(new BasicDBObject("wildcard", new BasicDBObject("externalDoc:documentType", "*" + str + "*")));
            });
        }

        if (!Strings.isNullOrEmpty(searchModel.getDocumentStatus())) {
            Arrays.asList(searchModel.getDocumentStatus().split(" ")).forEach((str) -> {
                mustQueryList.add(new BasicDBObject("wildcard", new BasicDBObject("externalDoc:documentStatus", "*" + str + "*")));
            });
        }

        if (searchModel.getRegisterDateFrom() != null) {
            mustQueryList.add(new BasicDBObject("range", new BasicDBObject("externalDoc:documentCreateDate", new BasicDBObject("gte", searchModel.getRegisterDateFrom().getTime()))));
        }

        if (searchModel.getRegisterDateTo() != null) {
            mustQueryList.add(new BasicDBObject("range", new BasicDBObject("externalDoc:documentCreateDate", new BasicDBObject("lte", searchModel.getRegisterDateTo().getTime()))));
        }

        if (searchModel.getMapAttValue() != null && !searchModel.getMapAttValue().isEmpty()) {
            for (Map.Entry<String, Object> entry : searchModel.getMapAttValue().entrySet()) {
                String key = entry.getKey().split(":")[1];
                Object value = entry.getValue();
                String valueStr = "";
                if (value != null && !value.toString().trim().isEmpty()) {
                    if (value instanceof Date) {
                        valueStr = sdf.format((Date) value);
                    } else {
                        valueStr = value.toString();
                    }

                    Arrays.asList(key.split(" ")).forEach((str) -> {
                        mustQueryList.add(new BasicDBObject("wildcard", new BasicDBObject("externalDocMetaTag:externalDocTypeAttribute", "*" + str + "*")));
                    });

                    Arrays.asList(valueStr.split(" ")).forEach((str) -> {
                        mustQueryList.add(new BasicDBObject("wildcard", new BasicDBObject("externalDocMetaTag:value", "*" + str + "*")));
                    });
                }
            }
        }

        if (!Strings.isNullOrEmpty(searchModel.getRecordType())) {
            mustQueryList.add(new BasicDBObject("match", new BasicDBObject("recordType", searchModel.getRecordType())));
        }
        if (searchModel.getMapWFAttValue() != null && !searchModel.getMapWFAttValue().isEmpty()) {
            for (Map.Entry<String, Object> entry : searchModel.getMapWFAttValue().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value != null && !value.toString().trim().isEmpty()) {
                    if (value instanceof String) {

                        Arrays.asList(value.toString().split(" ")).forEach((str) -> {
                            mustQueryList.add(new BasicDBObject("wildcard", new BasicDBObject(key, "*" + str + "*")));
                        });

                    } else if (value instanceof Date) {
                        mustQueryList.add(new BasicDBObject("match", new BasicDBObject(key, value)));
                    }
                }
            }

        }
        result.append("query", new BasicDBObject("bool", new BasicDBObject("must", mustQueryList)));
        if (!Strings.isNullOrEmpty(searchModel.getSortBy()) && !Strings.isNullOrEmpty(searchModel.getSortOrder())) {
            BasicDBList sortList = new BasicDBList();
            sortList.add(new BasicDBObject(sortFieldConvertMap.get(searchModel.getSortBy()), new BasicDBObject("order", searchModel.getSortOrder().toLowerCase())));
            result.append("sort", sortList);
        }

        result.append("from", offset);
        result.append("size", limit);
        LOG.debug("ES Search Query : {}", result.toString());
        return new JSONObject(result.toMap()).toString();
    }
}
