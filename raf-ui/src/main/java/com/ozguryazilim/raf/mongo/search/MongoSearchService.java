package com.ozguryazilim.raf.mongo.search;

import com.google.common.base.Strings;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.QueryBuilder;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.DetailedSearchModel;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.bson.Document;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class MongoSearchService implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(MongoSearchService.class);

    @Inject
    private RafService rafService;

    @Inject
    private RafMemberService rafMemberService;

    @Inject
    private RafPathMemberService rafPathMemberService;

    @Inject
    private Identity identity;

    public long detailedSearchCount(DetailedSearchModel searchModel, List<RafDefinition> rafs) throws RafException {
        long result = 0;
        DBObject query = getSearchQuery(searchModel, rafs);
        MongoClient mongoClient = getMongoClient();
        if (mongoClient != null) {
            String mongoDatabase = ConfigResolver.getPropertyValue("mongoSearch.mongoDatabase", "raf_repository");
            MongoDatabase db = mongoClient.getDatabase(mongoDatabase);
            MongoCollection col = db.getCollection("rafRepository");
            result = col.count(new Document(query.toMap()));
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
        DBObject query = getSearchQuery(searchModel, rafs);
        Map<String, String> sortFieldConvertMap = new HashMap();
        sortFieldConvertMap.put("nodes.[jcr:path]", "filePath");
        sortFieldConvertMap.put("nodes.[jcr:createBy]", "createBy");
        sortFieldConvertMap.put("nodes.[jcr:created]", "createDate");
        sortFieldConvertMap.put("nodes.[jcr:lastModifiedBy]", "updateBy");
        sortFieldConvertMap.put("nodes.[jcr:lastModified]", "updateDate");
        sortFieldConvertMap.put("exdoc.[externalDoc:documentCreator]", "metaDatas.attributes.externalDoc:documentCreator");
        sortFieldConvertMap.put("exdoc.[externalDoc:documentCreateDate]", "metaDatas.attributes.externalDoc:documentCreateDate");

        MongoClient mongoClient = getMongoClient();
        if (mongoClient != null) {
            String mongoDatabase = ConfigResolver.getPropertyValue("mongoSearch.mongoDatabase", "raf_repository");
            MongoDatabase db = mongoClient.getDatabase(mongoDatabase);
            MongoCollection<Document> col = db.getCollection("rafRepository", Document.class);
//            createIndexForQuery(query, col);
            FindIterable<Document> find = col.find(new Document(query.toMap()));
            if (!Strings.isNullOrEmpty(searchModel.getSortBy()) && !Strings.isNullOrEmpty(searchModel.getSortOrder())) {
                find.sort(new Document(sortFieldConvertMap.get(searchModel.getSortBy()), "DESC".equals(searchModel.getSortOrder()) ? -1 : 1));
            }
            MongoCursor<Document> cursor = find.projection(new Document("filePath", 1)).limit(limit).skip(offset).iterator();
            while (cursor.hasNext()) {
                Document doc = (Document) cursor.next();
                String filePath = doc.getString("filePath");
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
        return result;
    }

    private void addIndexToMap(DBObject dboQuery, Document index) {
        dboQuery.toMap().keySet().forEach((k) -> {
            if (!k.toString().contains("$")) {
                index.append(k.toString(), 1);
            } else if (dboQuery.get(k.toString()) instanceof DBObject) {
                addIndexToMap((DBObject) dboQuery.get(k.toString()), index);
            } else if (dboQuery.get(k.toString()) instanceof ArrayList) {
                List<Object> list = (List<Object>) dboQuery.get(k.toString());
                for (Object o : list) {
                    if (o instanceof DBObject) {
                        addIndexToMap((DBObject) o, index);
                    }
                }
            }
        });
    }

    private void createIndexForQuery(DBObject dboQuery, MongoCollection col) {
        Document index = new Document();
        addIndexToMap(dboQuery, index);
        col.createIndex(index);
    }

    private MongoClient getMongoClient() {
        String mongoServer = ConfigResolver.getPropertyValue("mongoSearch.mongoServer", "localhost");
        String mongoDatabase = ConfigResolver.getPropertyValue("mongoSearch.mongoDatabase", "raf_repository");
        int mongoPort = Integer.parseInt(ConfigResolver.getPropertyValue("mongoSearch.mongoPort", "27017"));
        String mongoUserName = ConfigResolver.getPropertyValue("mongoSearch.mongoUserName", "");
        String mongoPassword = ConfigResolver.getPropertyValue("mongoSearch.mongoPassword", "");
        MongoCredential credential = MongoCredential.createCredential(mongoUserName, mongoDatabase, mongoPassword.toCharArray());
        MongoClient mongoClient = Strings.isNullOrEmpty(mongoUserName) ? new MongoClient(new ServerAddress(mongoServer, mongoPort)) : new MongoClient(new ServerAddress(mongoServer, mongoPort), Arrays.asList(credential));
        return mongoClient;
    }

    public DBObject getSearchQuery(DetailedSearchModel searchModel, List<RafDefinition> rafs) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        List<String> rafCodes = new ArrayList();
        rafs.forEach((r) -> rafCodes.add(r.getCode()));
        QueryBuilder qb = QueryBuilder.start("rafCode").in(rafCodes);

        if (searchModel.getDateFrom() != null) {
            qb.and("createDate").greaterThanEquals(searchModel.getDateFrom());
        }

        if (searchModel.getDateTo() != null) {
            qb.and("createDate").lessThanEquals(searchModel.getDateTo());
        }

        if (!Strings.isNullOrEmpty(searchModel.getSearchText())) {
            qb.and("name").is(Pattern.compile(searchModel.getSearchText()));
        }

        if (!Strings.isNullOrEmpty(searchModel.getSearchSubPath())) {
            qb.and("filePath").is(Pattern.compile(searchModel.getSearchSubPath()));
        }

        if (!Strings.isNullOrEmpty(searchModel.getDocumentType())) {
            qb.and("metaDatas.attributes.externalDoc:documentType").is(searchModel.getDocumentType());
        }

        if (!Strings.isNullOrEmpty(searchModel.getDocumentStatus())) {
            qb.and("metaDatas.attributes.externalDoc:documentStatus").is(searchModel.getDocumentStatus());
        }

        if (searchModel.getRegisterDateFrom() != null) {
            qb.and("metaDatas.attributes.externalDoc:documentCreateDate").greaterThanEquals(searchModel.getRegisterDateFrom());
        }

        if (searchModel.getRegisterDateTo() != null) {
            qb.and("metaDatas.attributes.externalDoc:documentCreateDate").lessThanEquals(searchModel.getRegisterDateTo());
        }

        if (searchModel.getMapAttValue() != null && !searchModel.getMapAttValue().isEmpty()) {
            List<DBObject> andList = new ArrayList();
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
                    andList.add(QueryBuilder.start("metaDatas.attributes.externalDocMetaTag:externalDocTypeAttribute").is(Pattern.compile(key))
                            .and("metaDatas.attributes.externalDocMetaTag:value").is(Pattern.compile(valueStr)).get());
                }
            }
            if (!andList.isEmpty()) {
                qb.and("$and").is(andList);
            }
        }

        if (!Strings.isNullOrEmpty(searchModel.getRecordMetaDataName())) {
            qb.and("metaDatas.type").is(searchModel.getRecordMetaDataName());
        }

        if (searchModel.getMapWFAttValue() != null && !searchModel.getMapWFAttValue().isEmpty()) {
            for (Map.Entry<String, Object> entry : searchModel.getMapWFAttValue().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value != null && !value.toString().trim().isEmpty()) {
                    if (value instanceof String) {
                        qb.and(QueryBuilder.start("metaDatas.attributes.".concat(key)).is(Pattern.compile(value.toString())).get());
                    } else if (value instanceof Date) {
                        qb.and(QueryBuilder.start("metaDatas.attributes.".concat(key)).is(value).get());
                    }
                }
            }

        }

        LOG.debug("Mongo Search Query : {}", qb.get().toString());
        return qb.get();
    }
}
