package com.ozguryazilim.raf;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.category.RafCategoryService;
import com.ozguryazilim.raf.entities.RafCategory;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.events.EventLogCommand;
import com.ozguryazilim.raf.events.EventLogCommandBuilder;
import com.ozguryazilim.raf.jcr.RafModeshapeRepository;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.models.RafNode;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.models.RafVersion;
import com.ozguryazilim.telve.audit.AuditLogCommand;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import org.apache.deltaspike.core.api.config.ConfigResolver;

/**
 * Raf hizmetleri için temel Service sınıfı.
 *
 * Burada bir çeşit Facede yapılmış durumda.
 *
 * @author Hakan Uygun
 */
@ApplicationScoped
public class RafService implements Serializable {

    @Inject
    private RafModeshapeRepository rafRepository;

    @Inject
    private RafCategoryService categoryService;

    @Inject
    private CommandSender commandSender;

    @Inject
    private Identity identity;

    private Boolean readLogEnabled;

    public boolean checkRafName(String name) {
        return !Strings.isNullOrEmpty(name)
                && !name.matches("\\s*")
                && !name.equals(ApplicationContstants.PRIVATE_RAF)
                && !name.equals(ApplicationContstants.SHARED_RAF);
    }

    public RafObject setRafCheckOutValue(String path, Boolean checkOut, String userName, Date checkTime) throws RafException {
        return rafRepository.setRafCheckOutValue(path, checkOut, userName, checkTime);
    }

    public Boolean getRafCheckStatus(String path) throws RafException {
        return Boolean.TRUE.equals(rafRepository.getRafCheckStatus(path));
    }

    public String getRafCheckerUser(String path) throws RafException {
        return rafRepository.getRafCheckerUser(path);
    }

    public RafObject turnBackToVersion(String path, String versionName) throws RafException {
        return rafRepository.turnBackToVersion(path, versionName);
    }

    public String getRafCheckOutPreviousVersion(String path) throws RafException {
        return rafRepository.getRafCheckOutPreviousVersion(path);
    }

    public void deleteVersion(String path, String versionName) throws RepositoryException {
        rafRepository.deleteVersion(path, versionName);
    }

    public List<RafFolder> getFolderList(String rafPath) throws RafException {
        //FIXME: yetki kontrolleri, sıralama v.b.
        sendAuditLog("", "GET_FOLDER_LIST", rafPath);
        return rafRepository.getFolderList(rafPath);
    }

    public List<RafFolder> getChildFolderList(String rafPath) throws RafException {
        //FIXME: yetki kontrolleri, sıralama v.b.
        sendAuditLog("", "GET_FOLDER_LIST", rafPath);
        return rafRepository.getChildFolderList(rafPath);
    }

    public RafCollection getCollectionPaged(String id, int page, int pageSize, boolean justFolders, String sortBy, Boolean descSort) throws RafException {
        //FIXME: yetki kontrolleri
        RafCollection result = rafRepository.getCollectionById(id, true, page, pageSize, justFolders, sortBy, descSort);

        if (isReadLogEnabled()) {
            sendAuditLog(id, "GET_PAGED_FOLDER_CONTENT", result.getPath());
        }
        return result;
    }

    public RafCollection getCollection(String id) throws RafException {
        //FIXME: yetki kontrolleri
        RafCollection result = rafRepository.getCollectionById(id, false, 0, 0, false, "jcr:title", false);

        if (isReadLogEnabled()) {
            sendAuditLog(id, "GET_FOLDER_CONTENT", result.getPath());
        }
        return result;
    }

    public RafCollection getCategoryCollection(Long categoryId, String category, String categoryPath, String rootPath) throws RafException {
        //FIXME: sıralama, yetki v.s.
        if (isReadLogEnabled()) {
            sendAuditLog(category, "GET_CATEGORY_CONTENT", rootPath);
        }
        return rafRepository.getCategoryCollection(categoryId, category, categoryPath, rootPath, false);
    }

    public RafCollection getTagCollection(String tag, String rootPath) throws RafException {
        //FIXME: sıralama, yetki v.s.
        if (isReadLogEnabled()) {
            sendAuditLog(tag, "GET_TAG_CONTENT", rootPath);
        }
        return rafRepository.getTagCollection(tag, rootPath);
    }

    public RafCollection getCategoryCollectionById(Long categoryId, String rootPath) throws RafException {
        //FIXME: sıralama, yetki v.s.
        //FIXME: NPE kontorol
        RafCategory cat = categoryService.findById(categoryId);
        if (isReadLogEnabled()) {
            sendAuditLog(cat.getName(), "GET_CATEGORY_CONTENT", rootPath);
        }
        return getCategoryCollection(cat.getId(), cat.getName(), cat.getPath(), rootPath);
    }

    /**
     * Verilen RafFolder modeli örnek olarak kullanılır.
     *
     * path, title, description gibi alanlar üzerinden yeni bir RafFolder
     * oluşturulur.
     *
     * Oluşturulan yeni folder geri döndürürlür.
     *
     * Eğer o path üzerinde bir folder var ise o geri döner.
     *
     * @param folder
     * @return
     * @throws RafException
     */
    public RafFolder createFolder(RafFolder folder) throws RafException {
        //FIXME: yetki kontrolü
        //TODO: klasör eklendiğine dair burada bir event fırlatmak lazım.
        RafFolder result = rafRepository.createFolder(folder);

        sendEventLog("CreateFolder", result);
        sendAuditLog(result.getId(), "CREATE_FOLDER", result.getPath());
        return result;

    }

    public RafRecord createRecord(RafRecord record) throws RafException {
        //FIXME: yetki kontrolü
        //TODO: record eklendiğine dair burada bir event fırlatmak lazım.
        RafRecord result = rafRepository.createRecord(record);

        sendEventLog("CreateRecord", result);
        sendAuditLog(result.getId(), "CREATE_RECORD", result.getPath());
        return result;
    }

    public void saveRecord(RafRecord record) throws RafException {
        //FIXME: yetki kontrolü
        //TODO: record eklendiğine dair burada bir event fırlatmak lazım.

        rafRepository.saveRecord(record);

        sendEventLog("Saverecord", record);
        sendAuditLog(record.getId(), "SAVE_RECORD", record.getPath());

    }

    /**
     * Verilen path için folder oluşturur.
     *
     * Title, Desctription gibi alanlar doğal olarak doldurulmazlar
     *
     * @param folderPath
     * @return
     * @throws RafException
     */
    public RafFolder createFolder(String folderPath) throws RafException {
        //FIXME: yetki kontrolü
        //TODO: klasör eklendiğine dair burada bir event fırlatmak lazım.
        RafFolder result = rafRepository.createFolder(folderPath);

        sendEventLog("CreateFolder", result);
        sendAuditLog(result.getId(), "CREATE_FOLDER", result.getPath());
        return result;

    }

    public RafDocument checkout(String path) throws RafException {
        //FIXME: yetki kontrolü
        RafDocument result = (RafDocument) rafRepository.checkout(path);

        sendEventLog("CheckOutDocument", result);
        sendAuditLog(result.getId(), "CHECKOUT_DOCUMENT", result.getPath());
        return result;
    }

    public RafDocument checkin(String path) throws RafException {
        //FIXME: yetki kontrolü
        RafDocument result = (RafDocument) rafRepository.checkin(path);

        sendEventLog("CheckInDocument", result);
        sendAuditLog(result.getId(), "CHECKIN_DOCUMENT", result.getPath());
        return result;
    }

    //checkin with new version
    public RafDocument checkin(String fileName, InputStream in) throws RafException {
        //FIXME: yetki kontrolü
        RafDocument result = (RafDocument) rafRepository.checkin(fileName, in);

        sendEventLog("CheckInDocument", result);
        sendAuditLog(result.getId(), "CHECKIN_DOCUMENT", result.getPath());
        return result;
    }

    public RafDocument uploadDocument(String fileName, InputStream in) throws RafException {
        //FIXME: yetki kontrolü
        RafDocument result = rafRepository.uploadDocument(fileName, in);

        sendEventLog("UploadDocument", result);
        sendAuditLog(result.getId(), "UPLOAD_DOCUMENT", result.getPath());
        return result;
    }

    public RafObject getRafObject(String id) throws RafException {
        //FIXME: yetki kontrolü gerekli.
        //FIXME: event fırlatalım ki log yazılabilsin v.b.
        RafObject result = rafRepository.getRafObject(id);
        if (isReadLogEnabled()) {
            sendAuditLog(result.getId(), "READ_OBJECT", result.getPath());
        }
        return result;
    }

    public RafObject getRafObjectByPath(String path) throws RafException {
        //FIXME: yetki kontrolü gerekli.
        //FIXME: event fırlatalım ki log yazılabilsin v.b.
        RafObject result = rafRepository.getRafObjectByPath(path);
        if (isReadLogEnabled()) {
            sendAuditLog(result.getId(), "READ_OBJECT", result.getPath());
        }
        return result;
    }

    /**
     * Geriye verilen RafDocument için eğer varsa Sürüm Tarihçesini döndürür.
     *
     * @param object
     * @return
     * @throws RafException
     */
    public List<RafVersion> getVersionHistory(RafDocument object) throws RafException {
        return rafRepository.getVersionHistory(object);
    }

    public InputStream getDocumentContent(String id) throws RafException {
        if (isReadLogEnabled()) {
            sendAuditLog(id, "READ_DOCUMENT_CONTENT", "");
        }
        return rafRepository.getDocumentContent(id);
    }

    public void getDocumentContent(String id, OutputStream out) throws RafException {
        if (isReadLogEnabled()) {
            sendAuditLog(id, "READ_DOCUMENT_CONTENT", "");
        }
        rafRepository.getDocumentContent(id, out);
    }

    public InputStream getPreviewContent(String id) throws RafException {
        if (isReadLogEnabled()) {
            sendAuditLog(id, "READ_PREVIEW_CONTENT", "");
        }
        return rafRepository.getPreviewContent(id);
    }

    public void reGeneratePreview(String id) throws RafException {
        rafRepository.reGeneratePreview(id);
    }

    public void reGenerateObjectPreviews(List<RafObject> rafObjects, Integer recursiveCallCounter) throws RafException {
        if (recursiveCallCounter == 10) {
            //Devre Kesici !! En fazla 10 defa kendini çağırabilir. (10 alt klasör çalıştırılabilir.)
            return;
        }
        for (RafObject rafObject : rafObjects) {
            if (rafObject instanceof RafDocument && ((RafDocument) rafObject).getHasPreview()) {
                reGeneratePreview(rafObject.getId());
            } else if (rafObject instanceof RafFolder) {
                RafCollection r = rafRepository.getCollectionById(rafObject.getId(), false, 0, 0, false, "jcr:title", false);
                reGenerateObjectPreviews(r.getItems(), recursiveCallCounter + 1);

            }
        }
    }

    public RafCollection getRafCollectionForAllNode() throws RafException {
        return rafRepository.getRafCollectionForAllNode();
    }

    public InputStream getDocumentVersionContent(String id, String version) throws RafException {
        if (isReadLogEnabled()) {
            sendAuditLog(id, "READ_DOCUMENT_CONTENT", "");
        }
        return rafRepository.getVersionContent(id, version);
    }

    public void saveMetadata(String id, RafMetadata data) throws RafException {
        //FIXME: Yetki kontrolü + event
        sendAuditLog(id, "SAVE_METADATA", "");
        rafRepository.saveMetadata(id, data);
    }

    public void saveMetadatas(String id, List<RafMetadata> datas) throws RafException {
        //FIXME: Yetki kontrolü + event
        sendAuditLog(id, "SAVE_METADATAS", "");
        rafRepository.saveMetadatas(id, datas);
    }

    /**
     * RafObject üzerinde bulunan title, info v.b. alanları günceller.
     *
     * @param data
     * @throws RafException
     */
    public void saveProperties(RafObject data) throws RafException {
        //FIXME: Yetki kontrolü + event
        sendAuditLog(data.getId(), "SAVE_PROPERTIES", data.getPath());
        rafRepository.saveProperties(data);
    }

    public void deleteObject(RafObject data) throws RafException {
        //FIXME: Yetki kontrolü
        sendAuditLog(data.getId(), "DELETE_OBJECT", data.getPath());
        deleteObject(data.getId());
    }

    public void deleteObject(String id) throws RafException {
        //FIXME: Yetki kontrolü
        RafObject obj = rafRepository.getRafObject(id);
        sendEventLog("DeleteObject", obj);
        sendAuditLog(id, "DELETE_OBJECT", "");
        rafRepository.deleteObject(id);
    }

    public void copyObject(RafObject from, RafFolder to) throws RafException {
        //FIXME: yetki kontrolü
        sendAuditLog(from.getId(), "COPY_OBJECT_FROM", from.getPath());
        sendAuditLog(to.getId(), "COPY_OBJECT_TO", to.getPath());
        rafRepository.copyObject(from, to);
    }

    public void copyObject(RafObject from, RafRecord to) throws RafException {
        //FIXME: yetki kontrolü
        sendAuditLog(from.getId(), "COPY_OBJECT_FROM", from.getPath());
        sendAuditLog(to.getId(), "COPY_OBJECT_TO", to.getPath());
        rafRepository.copyObject(from, to);
    }

    public void copyObject(List<RafObject> from, RafFolder to) throws RafException {
        //FIXME: yetki kontrolü
        for (RafObject o : from) {
            sendAuditLog(o.getId(), "COPY_OBJECT_FROM", o.getPath());
        }
        sendAuditLog(to.getId(), "COPY_OBJECT_TO", to.getPath());
        rafRepository.copyObject(from, to);
    }

    public void copyObject(List<RafObject> from, RafRecord to) throws RafException {
        //FIXME: yetki kontrolü
        for (RafObject o : from) {
            sendAuditLog(o.getId(), "COPY_OBJECT_FROM", o.getPath());
        }
        sendAuditLog(to.getId(), "COPY_OBJECT_TO", to.getPath());
        rafRepository.copyObject(from, to);
    }

    public void moveObject(RafObject from, RafRecord to) throws RafException {
        //FIXME: yetki kontrolü
        sendAuditLog(from.getId(), "MOVE_OBJECT_FROM", from.getPath());
        sendAuditLog(to.getId(), "MOVE_OBJECT_TO", to.getPath());
        rafRepository.moveObject(from, to);
    }

    public void moveObject(List<RafObject> from, RafRecord to) throws RafException {
        //FIXME: yetki kontrolü
        for (RafObject o : from) {
            sendAuditLog(o.getId(), "MOVE_OBJECT_FROM", o.getPath());
        }
        sendAuditLog(to.getId(), "MOVE_OBJECT_TO", to.getPath());
        rafRepository.moveObject(from, to);
    }

    public void moveObject(List<RafObject> from, RafFolder to) throws RafException {
        //FIXME: yetki kontrolü
        for (RafObject o : from) {
            sendAuditLog(o.getId(), "MOVE_OBJECT_FROM", o.getPath());
        }
        sendAuditLog(to.getId(), "MOVE_OBJECT_TO", to.getPath());
        rafRepository.moveObject(from, to);
    }

    public RafFolder getFolder(String rafPath) throws RafException {
        //FIXME: yetki kontrolleri, sıralama v.b.
        return rafRepository.getFolder(rafPath);
    }

    public List<RafFolder> getRootFolders(String path) throws RafException {
        List<RafFolder> result = new ArrayList<>();
        result.add(getFolder(path));
        result.addAll(rafRepository.getChildren(path));
        return result;
    }

    public List<RafFolder> getChildren(String path) throws RafException {
        return rafRepository.getChildren(path);
    }

    public RafNode getProcessRafNode() throws RafException {
        return rafRepository.getProcessRafNode();
    }

    protected void sendEventLog(String eventType, RafObject object) {

        if (identity == null || "SYSTEM".equals(identity.getLoginName())) {
            return;
        }
        //FIXME: ıdentity
        EventLogCommand command = EventLogCommandBuilder.forRaf("RAF")
                .eventType(eventType)
                .forRafObject(object)
                .message("event." + eventType + "$%&" + (identity != null ? identity.getUserName() : "Sistem") + "$%&" + object.getTitle())
                .user(identity != null ? identity.getLoginName() : "SYSTEM")
                .build();

        commandSender.sendCommand(command);

    }

    protected void sendAuditLog(String id, String action, String path) {
        if (identity == null || "SYSTEM".equals(identity.getLoginName())) {
            return;
        }

        AuditLogCommand command = new AuditLogCommand("RAF", Long.MIN_VALUE, id, action, "RAF", identity.getLoginName(), path);
        commandSender.sendCommand(command);
    }

    protected boolean isReadLogEnabled() {
        if (readLogEnabled == null) {
            readLogEnabled = "true".equals(ConfigResolver.getPropertyValue("auditLog.read", "false"));
        }

        return readLogEnabled;
    }

    public void reindex() {
        rafRepository.reindex();
    }

    public RafCollection getLastCreatedOrModifiedFilesCollection(Date fromDate, List<RafDefinition> rafs, boolean created) throws RafException {
        return rafRepository.getLastCreatedOrModifiedFilesCollection(fromDate, rafs, created);
    }

    public void unregisterIndexes(String... indexNames) {
        rafRepository.unregisterIndexes(indexNames);
    }

    public long getFolderSize(String absPath, Long maxSumSize) throws RafException {
        return rafRepository.getFolderSize(absPath, maxSumSize);
    }

    public String getDocumentExtractedText(String id) throws RafException {
        if (isReadLogEnabled()) {
            sendAuditLog(id, "READ_DOCUMENT_EXTRACTED_TEXT", "");
        }
        return rafRepository.getDocumentExtractedText(id);
    }
}
