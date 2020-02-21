package com.ozguryazilim.raf.mongo.search;

import com.google.common.base.Strings;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandExecutor(command = MongoSearchExporterCommand.class)
public class MongoSearchExporterCommandExecutor extends AbstractCommandExecuter<MongoSearchExporterCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(MongoSearchExporterCommandExecutor.class);
    private SimpleDateFormat sdf;

    @Inject
    RafService rafService;

    @Inject
    RafDefinitionService rafDefinitionService;

    MongoSearchExporterCommand command;

    RafEncoder re;

    @Override
    public void execute(MongoSearchExporterCommand command) {
        this.command = command;
        this.re = RafEncoderFactory.getEncoder();
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

        exportAllDocuments();
    }

    private void exportAllDocuments() {
        LOG.info("Raf mongo exporting command is executing.");
        MongoCredential credential = MongoCredential.createCredential(command.getDbUserName(), command.getDbName(), command.getDbPassword().toCharArray());
        MongoClient mongoClient = Strings.isNullOrEmpty(command.getDbUserName()) ? new MongoClient(new ServerAddress(command.getDbHostName(), command.getDbPort())) : new MongoClient(new ServerAddress(command.getDbHostName(), command.getDbPort()), Arrays.asList(credential));
        MongoDatabase db = mongoClient.getDatabase(command.getDbName());
        MongoCollection col = db.getCollection("rafRepository");

        for (RafDefinition r : rafDefinitionService.getRafs()) {
            if (r.getCode() != null && r.getNode() != null) {
                LOG.info("{} raf exporting to mongo.", r.getCode());
                col.deleteMany(new Document("rafCode", r.getCode()));
                exportFolder(col, r.getCode(), r.getNode().getId());
                LOG.info("{} raf exported.", r.getCode());
            } else {
                LOG.info("{} raf node is null.", r.getCode());
            }
        }
        mongoClient.close();
        LOG.info("Raf mongo exporting command is executed.");
    }

    private void exportFolder(MongoCollection col, String rafCode, String id) {
        try {
            int page = 0;
            RafCollection rCol = rafService.getCollection(id);

            for (RafObject itm : rCol.getItems()) {
                try {
                    LOG.info("{} file is exporting to mongo.", itm.getPath());
                    Document dboRec = null;
                    if (itm instanceof RafRecord) {
                        dboRec = insertRafRecord(rafCode, (RafRecord) itm);
                    } else if (itm instanceof RafFolder) {
                        dboRec = insertRafFolder(rafCode, (RafFolder) itm);
                        exportFolder(col, rafCode, itm.getId());
                    } else if (itm instanceof RafDocument) {
                        dboRec = insertRafDocument(rafCode, (RafDocument) itm);
                    }
                    col.insertOne(dboRec);
                } catch (Exception e) {
                    LOG.error("Exception", e);
                }
            }

        } catch (RafException ex) {
            LOG.error("RafException", ex);
        }
    }

    private Document insertRafRecord(String raf, RafRecord record) {
        Document dboRec = new Document();
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
        dboRec.put("createDate", record.getCreateDate());
        dboRec.put("updateBy", record.getUpdateBy());
        dboRec.put("updateDate", record.getUpdateDate());
        dboRec.put("category", record.getCategory());
        dboRec.put("tags", record.getTags());
        List<Document> metaDatas = new ArrayList();
        if (record.getMetadatas() != null) {
            for (RafMetadata metadata : record.getMetadatas()) {
                Document mdRec = new Document();
                mdRec.put("nodeId", metadata.getNodeId());
                mdRec.put("type", metadata.getType());
                mdRec.put("attributes", metadata.getAttributes());
                metaDatas.add(mdRec);
            }
        }
        dboRec.put("metaDatas", metaDatas);
        return dboRec;
    }

    private Document insertRafFolder(String raf, RafFolder record) {
        Document dboRec = new Document();
        dboRec.put("rafCode", raf);
        dboRec.put("filePath", record.getPath());
        dboRec.put("rafType", "RafFolder");
        dboRec.put("mimeType", record.getMimeType());
        dboRec.put("name", record.getName());
        dboRec.put("title", record.getTitle());
        dboRec.put("version", record.getVersion());
        dboRec.put("createBy", record.getCreateBy());
        dboRec.put("createDate", record.getCreateDate());
        dboRec.put("updateBy", record.getUpdateBy());
        dboRec.put("updateDate", record.getUpdateDate());
        dboRec.put("category", record.getCategory());
        dboRec.put("tags", record.getTags());
        List<Document> metaDatas = new ArrayList();
        if (record.getMetadatas() != null) {
            for (RafMetadata metadata : record.getMetadatas()) {
                Document mdRec = new Document();
                mdRec.put("nodeId", metadata.getNodeId());
                mdRec.put("type", metadata.getType());
                mdRec.put("attributes", metadata.getAttributes());
                metaDatas.add(mdRec);
            }
        }
        dboRec.put("metaDatas", metaDatas);
        return dboRec;
    }

    private Document insertRafDocument(String raf, RafDocument record) {
        Document dboRec = new Document();
        dboRec.put("rafCode", raf);
        dboRec.put("filePath", record.getPath());
        dboRec.put("rafType", "RafDocument");
        dboRec.put("mimeType", record.getMimeType());
        dboRec.put("name", record.getName());
        dboRec.put("title", record.getTitle());
        dboRec.put("version", record.getVersion());
        dboRec.put("createBy", record.getCreateBy());
        dboRec.put("createDate", record.getCreateDate());
        dboRec.put("updateBy", record.getUpdateBy());
        dboRec.put("updateDate", record.getUpdateDate());
        dboRec.put("category", record.getCategory());
        dboRec.put("tags", record.getTags());
        List<Document> metaDatas = new ArrayList();
        if (record.getMetadatas() != null) {
            for (RafMetadata metadata : record.getMetadatas()) {
                Document mdRec = new Document();
                mdRec.put("nodeId", metadata.getNodeId());
                mdRec.put("type", metadata.getType());
                mdRec.put("attributes", metadata.getAttributes());
                metaDatas.add(mdRec);
            }
        }
        dboRec.put("metaDatas", metaDatas);
        return dboRec;
    }
}
