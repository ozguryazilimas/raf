package com.ozguryazilim.raf.elasticsearch.search;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.encoder.RafEncoder;
import com.ozguryazilim.raf.encoder.RafEncoderFactory;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.jersey.api.client.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.primefaces.json.JSONObject;

@CommandExecutor(command = ElasticSearchExporterCommand.class)
public class ElasticSearchExporterCommandExecutor extends AbstractCommandExecuter<ElasticSearchExporterCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchExporterCommandExecutor.class);
    private SimpleDateFormat sdf;

    @Inject
    RafService rafService;

    @Inject
    RafDefinitionService rafDefinitionService;

    ElasticSearchExporterCommand command;

//    RafEncoder re;

    private WebResource myWebResource;
    private Client myClient;

    @Override
    public void execute(ElasticSearchExporterCommand command) {
        this.command = command;
//        this.re = RafEncoderFactory.getEncoder();
        this.sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        if (command == null || Strings.isNullOrEmpty(command.getDbHostName())) {
            FacesMessages.error("Komut ayarları tanımlı değil.");
            return;
        }
        if (Strings.isNullOrEmpty(command.getDbHostName())) {
            FacesMessages.error("Veritabanı sunucu adı tanımlı değil.");
            return;
        }
        if (Strings.isNullOrEmpty(command.getDbName())) {
            FacesMessages.error("Veritabanı adı tanımlı değil.");
            return;
        }

        if (command.getReindexRepository()) {
            LOG.info("Reindex command executing...");
            rafService.reindex();
            LOG.info("Reindex command executed...");
        } else {
            exportAllDocuments();
        }
    }

    WebResource getWebResource() {
        if (myClient == null) {
            myClient = Client.create();
        }
        if (myWebResource == null) {
            myWebResource = myClient.resource(String.format("http://%s:%d", command.getDbHostName(), command.getDbPort(), command.getDbName()));
        }
        return myWebResource;
    }

    private void exportAllDocuments() {
        LOG.info("Raf elastic exporting command is executing.");
        try {
            getWebResource().path(command.getDbName()).put(new JSONObject("{\n"
                    + "    \"mappings\": {\n"
                    + "        \"default\": {\n"
                    + "            \"dynamic_templates\": [                \n"
                    + "                { \"tr\": {\n"
                    + "                      \"match\":\"*\", \n"
                    + "                      \"match_mapping_type\": \"string\",\n"
                    + "                      \"mapping\": {\n"
                    + "                          \"type\":\"string\",\n"
                    + "                          \"analyzer\":\"whitespace\"\n"
                    + "                      }\n"
                    + "                }}\n"
                    + "            ]\n"
                    + "}}}").toString());
        } catch (Exception ex) {
            LOG.error("Exception", ex);
        }

        if (command.getOwerwrite() && command.getStartDate() != null) {
            for (RafDefinition r : rafDefinitionService.getRafs()) {
                if (r.getCode() != null) {
                    LOG.info("{} raf exporting to elastic.", r.getCode());
                    String rafId = r.getNode() != null ? r.getNode().getId() : r.getNodeId();
                    exportFolder(r.getCode(), rafId);
                    LOG.info("{} raf exported.", r.getCode());
                } else {
                    LOG.info("{} raf node is null.", r.getCode());
                }
            }
        } else {
            for (RafDefinition r : rafDefinitionService.getRafs()) {
                try {
                    LOG.info("{} raf exporting to elastic.", r.getCode());
                    RafCollection searchResult = rafService.getLastCreatedFilesCollection(command.getStartDate(), Arrays.asList(new RafDefinition[]{r}));
                    exportRafCollection(searchResult, r.getCode());
                    LOG.info("{} raf exported.", r.getCode());
                } catch (RafException ex) {
                    LOG.error("RafException", ex);
                }
            }

        }

        LOG.info("Raf elastic exporting command is executed.");
    }

    private void exportRafCollection(RafCollection rCol, String rafCode) {
        for (RafObject itm : rCol.getItems()) {
            try {
                LOG.info("{} file is exporting to elastic.", itm.getPath());
                Map<String, Object> dboRec = null;
                if (itm instanceof RafRecord) {
                    dboRec = insertRafRecord(rafCode, (RafRecord) itm);
                } else if (itm instanceof RafFolder) {
                    dboRec = insertRafFolder(rafCode, (RafFolder) itm);
                    exportFolder(rafCode, itm.getId());
                } else if (itm instanceof RafDocument) {
                    dboRec = insertRafDocument(rafCode, (RafDocument) itm);
                }
                getWebResource().path(command.getDbName()).path("default").path(itm.getId()).post(new JSONObject(dboRec).toString());
            } catch (Exception e) {
                LOG.error("Exception", e);
            }
        }
    }

    private void exportFolder(String rafCode, String id) {
        try {
            RafCollection rCol = rafService.getCollection(id);
            exportRafCollection(rCol, rafCode);
        } catch (RafException ex) {
            LOG.error("RafException", ex);
        }
    }

    private Map<String, Object> insertRafRecord(String raf, RafRecord record) {
        Map<String, Object> dboRec = new HashMap();
        dboRec.put("rafCode", raf);
        dboRec.put("filePath", record.getPath());
        dboRec.put("rafType", "RafRecord");
        dboRec.put("documentType", record.getDocumentType());
        dboRec.put("mimeType", record.getMimeType());
        dboRec.put("name", record.getName());
        dboRec.put("title", record.getTitle());
        dboRec.put("recordNo", record.getRecordNo());
        dboRec.put("version", record.getVersion());
        dboRec.put("recordType", record.getRecordType());
        dboRec.put("createBy", record.getCreateBy());
        dboRec.put("createDate", record.getCreateDate().getTime());
        dboRec.put("updateBy", record.getUpdateBy());
        dboRec.put("updateDate", record.getUpdateDate() != null ? record.getUpdateDate().getTime() : null);
        dboRec.put("category", record.getCategory());
        dboRec.put("tags", record.getTags());
        if (record.getMetadatas() != null) {
            for (RafMetadata metadata : record.getMetadatas()) {
                for (Map.Entry<String, Object> entry : metadata.getAttributes().entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof Date) {
                        value = ((Date) value).getTime();
                    }
                    dboRec.put(key, value);
                }
            }
        }
        return dboRec;
    }

    private Map<String, Object> insertRafFolder(String raf, RafFolder record) {
        Map<String, Object> dboRec = new HashMap();
        dboRec.put("rafCode", raf);
        dboRec.put("filePath", record.getPath());
        dboRec.put("rafType", "RafFolder");
        dboRec.put("mimeType", record.getMimeType());
        dboRec.put("name", record.getName());
        dboRec.put("title", record.getTitle());
        dboRec.put("version", record.getVersion());
        dboRec.put("createBy", record.getCreateBy());
        dboRec.put("createDate", record.getCreateDate().getTime());
        dboRec.put("updateBy", record.getUpdateBy());
        dboRec.put("updateDate", record.getUpdateDate() != null ? record.getUpdateDate().getTime() : null);
        dboRec.put("category", record.getCategory());
        dboRec.put("tags", record.getTags());
        if (record.getMetadatas() != null) {
            for (RafMetadata metadata : record.getMetadatas()) {
                for (Map.Entry<String, Object> entry : metadata.getAttributes().entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof Date) {
                        value = ((Date) value).getTime();
                    }
                    dboRec.put(key, value);
                }
            }
        }
        return dboRec;
    }

    private Map<String, Object> insertRafDocument(String raf, RafDocument record) {
        Map<String, Object> dboRec = new HashMap();
        dboRec.put("rafCode", raf);
        dboRec.put("filePath", record.getPath());
        dboRec.put("rafType", "RafDocument");
        dboRec.put("mimeType", record.getMimeType());
        dboRec.put("name", record.getName());
        dboRec.put("title", record.getTitle());
        dboRec.put("version", record.getVersion());
        dboRec.put("createBy", record.getCreateBy());
        dboRec.put("createDate", record.getCreateDate().getTime());
        dboRec.put("updateBy", record.getUpdateBy());
        dboRec.put("updateDate", record.getUpdateDate().getTime());
        dboRec.put("category", record.getCategory());
        dboRec.put("tags", record.getTags());
        if (record.getMetadatas() != null) {
            for (RafMetadata metadata : record.getMetadatas()) {
                for (Map.Entry<String, Object> entry : metadata.getAttributes().entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof Date) {
                        value = ((Date) value).getTime();
                    }
                    dboRec.put(key, value);
                }
            }
        }
        return dboRec;
    }
}
