package com.ozguryazilim.raf;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.category.RafCategoryService;
import com.ozguryazilim.raf.email.EmailNotificationService;
import com.ozguryazilim.raf.entities.RafCategory;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.enums.EmailNotificationActionType;
import com.ozguryazilim.raf.enums.SortType;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jcr.RepositoryException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Raf hizmetleri için temel Service sınıfı.
 *
 * Burada bir çeşit Facede yapılmış durumda.
 *
 * @author Hakan Uygun
 */
@ApplicationScoped
public class RafService implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RafModeshapeRepository.class);

    @Inject
    private RafModeshapeRepository rafRepository;

    @Inject
    private RafCategoryService categoryService;

    @Inject
    private CommandSender commandSender;

    @Inject
    private Identity identity;

    @Inject
    private EmailNotificationService emailNotificationService;

    private Boolean readLogEnabled;
    
    private boolean bpmnSystemEnabled;

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

    public RafCollection getCollectionPaged(String id, int page, int pageSize, boolean justFolders, SortType sortBy, Boolean descSort) throws RafException {
        //FIXME: yetki kontrolleri
        RafCollection result = rafRepository.getCollectionById(id, true, page, pageSize, justFolders, sortBy, descSort);

        if (isReadLogEnabled()) {
            sendAuditLog(id, "GET_PAGED_FOLDER_CONTENT", result.getPath());
        }
        return result;
    }

    public RafCollection getCollection(String id) throws RafException {
        //FIXME: yetki kontrolleri
        RafCollection result = rafRepository.getCollectionById(id, false, 0, 0, false, SortType.NAME, false);

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
        emailNotificationService.sendEmailToFavorites(result, EmailNotificationActionType.ADD);
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
        RafFolder result = rafRepository.createFolder(folderPath);
        emailNotificationService.sendEmailToFavorites(result, EmailNotificationActionType.ADD);
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
        emailNotificationService.sendEmailToFavorites(result, EmailNotificationActionType.UPDATE);
        sendEventLog("CheckInDocument", result);
        sendAuditLog(result.getId(), "CHECKIN_DOCUMENT", result.getPath());
        return result;
    }

    //checkin with new version
    public RafDocument checkin(String fileName, InputStream in, String versionComment) throws RafException {
        //FIXME: yetki kontrolü
        RafDocument result = (RafDocument) rafRepository.checkin(fileName, in, versionComment);
        emailNotificationService.sendEmailToFavorites(result, EmailNotificationActionType.UPDATE);
        sendEventLog("CheckInDocument", result);
        sendAuditLog(result.getId(), "CHECKIN_DOCUMENT", result.getPath());
        return result;
    }

    public RafDocument uploadDocument(String fileName, InputStream in) throws RafException {
        //FIXME: yetki kontrolü
        RafDocument result = rafRepository.uploadDocument(fileName, in);
        emailNotificationService.sendEmailToFavorites(result, EmailNotificationActionType.ADD);
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

    public RafVersion getLatestVersion(RafObject object) throws RafException {
        List<RafVersion> versions = rafRepository.getVersionHistory(object);
        versions.sort(new Comparator<RafVersion>() {
            @Override
            public int compare(RafVersion t, RafVersion t1) {
                int versionNum1 = NumberUtils.toInt(t.getName().replaceAll("\\.", ""), 0);
                int versionNum2 = NumberUtils.toInt(t1.getName().replaceAll("\\.", ""), 0);
                return versionNum2 - versionNum1;
            }
        });
        return versions.stream()
                .findFirst()
                .orElse(null);
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

    /**
     * PDF dosyanın ilk sayfasını imaja dönüştürür ve png formatına çevirir.
     *
     * @param id
     * @return
     * @throws RafException
     */
    public InputStream getThumbnailContent(String id) throws RafException {
        return rafRepository.getThumbnailContent(id);
    }

    /**
     * İdsi verilen file için preview hazırlar
     *
     * @param id
     * @throws RafException
     */
    public void regeneratePreview(String id) throws RafException {
        rafRepository.regeneratePreview(id);
    }

    /**
     * Idsi verilen folder için preview hazırlar
     *
     * @param id
     * @throws RafException
     */
    public void regenerateObjectPreviews(String id) throws RafException {
        rafRepository.regeneratePreviews(id, false);
    }

    public void regenerateObjectPreviews(String id, Boolean regenerateOnlyMissingPreviews) throws RafException {
        rafRepository.regeneratePreviews(id, regenerateOnlyMissingPreviews);
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
        //FIXME: Yetki kontrolü
        sendEventLog("DeleteObject", data);
        sendAuditLog(data.getId(), "SAVE_PROPERTIES", data.getPath());
        rafRepository.saveProperties(data);
    }

    public void deleteObject(RafObject data) throws RafException {
        //FIXME: Yetki kontrolü
        deleteObject(data.getId());
    }

    public void deleteObject(String id) throws RafException {
        try{
            //FIXME: Yetki kontrolü
            RafObject obj = rafRepository.getRafObject(id);
            emailNotificationService.sendEmailToFavorites(obj, EmailNotificationActionType.REMOVE);
            sendEventLog("DeleteObject", obj);
            sendAuditLog(id, "DELETE_OBJECT", obj.getPath());
        } catch (RafException ex){
            LOG.warn("[RAF-0044] Failed to create raf object. For this reason, event and audit logs will not be produced. item id: {}",id);
        }
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

    public void copyObject(List<RafObject> from, RafObject to) throws RafException {
        to = checkRafObjectForPasteAction(to);
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

    public void moveObject(List<RafObject> from, RafObject to) throws RafException {
        to = checkRafObjectForPasteAction(to);
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

        String logPath = path;

        //255 den daha uzun olan path lerde veritabanında ilgili sütuna yazılabilmesi için ortasını kesiyoruz.
        String longPathDivider = "...";
        int pathCharCountLimit = 255;
        int longPathPrefixOffset = 100;
        int longPathSuffixOffset = pathCharCountLimit - longPathPrefixOffset - longPathDivider.length();

        if (path.length() > pathCharCountLimit) {
            StringBuilder sb = new StringBuilder();
            sb.append(path, 0, longPathPrefixOffset)
                    .append(longPathDivider)
                    .append(path, path.length() - longPathSuffixOffset, path.length());

            logPath = sb.toString();
        }

        AuditLogCommand command = new AuditLogCommand("RAF", Long.MIN_VALUE, id, action, "RAF", identity.getLoginName(), logPath);
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

    public void extractZipFile(RafObject zipFile) throws RafException {
        rafRepository.extractZipFile(zipFile);
    }

    public Map<String, Long> getRafDefinitionProperties(String absPath) throws RafException {
        return rafRepository.getRafDefinitionProperties(absPath);
    }

    public long getChildCount(String absPath) throws RafException {
        return rafRepository.getChildCount(absPath);
    }

    public InputStream getFullPdfDocument(String id) throws RafException {
        return rafRepository.getFullPdfDocumentContent(id);
    }

    /**
     * Kopyalama ya da Kesme işlemleri için "RafObject" kontrolü yapar. Eğer subclass'ı "RafDocument" ise parent'ını döner.
     *
     * @param to -> Kontrol için kullanılacak paste action hedef objesi.
     * @return
     * @throws RafException -> Obje tipi "RafNode", "RafFolder" ya da "RafDocument" değilse fırlatır.
     */
    private RafObject checkRafObjectForPasteAction(RafObject to) throws RafException {
        // Eğer hedef olarak gelen obje bir raf, klasör ya da döküman değilse cut ya da paste action farketmeksizin
        // methodun çalışmasına izin vermemeliyiz.
        if (!(to instanceof RafNode) && !(to instanceof RafFolder) && !(to instanceof RafDocument)) {
            throw new RafException(String.format("Target object cannot be : %s", to.getClass().getSimpleName()));
        }
        // Hedef olarak bir döküman geldiyse döküman node'ları altında yeni bir node yaratılmaması için parent'ını alıyoruz.
        // Parent mutlaka bir raf ya da klasöre denk gelmeli. Yoksa methodun çalışmasına izin vermemeliyiz.
        if (to instanceof RafDocument) {
            RafObject parent = getRafObject(to.getParentId());
            if (parent instanceof RafNode || parent instanceof RafFolder) {
                return parent;
            } else {
                throw new RafException(String.format("Target parent object cannot be : %s", to.getClass().getSimpleName()));
            }
        }
        return to;
    }

    public void zipFile(RafObject fileToZip, String fileName, ZipOutputStream zipOut) throws IOException, RafException {
        if (fileToZip instanceof RafFolder) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            List<RafObject> children = getCollection(fileToZip.getId()).getItems();
            for (RafObject childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        if (fileToZip instanceof RafRecord) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            List<RafDocument> children = ((RafRecord) fileToZip).getDocuments();
            for (RafObject childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }

        InputStream fis = getDocumentContent(fileToZip.getId());
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        IOUtils.copy(fis, zipOut);
        zipOut.flush();
        fis.close();
    }
    public boolean isBpmnSystemEnabled() {
        return bpmnSystemEnabled;
    }

    public void setBpmnSystemEnabled(boolean bpmnSystemEnabled) {
        this.bpmnSystemEnabled = bpmnSystemEnabled;
    }

}