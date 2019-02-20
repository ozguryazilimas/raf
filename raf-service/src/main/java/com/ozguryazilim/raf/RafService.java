package com.ozguryazilim.raf;

import com.ozguryazilim.raf.category.RafCategoryService;
import com.ozguryazilim.raf.entities.RafCategory;
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
import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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

    public List<RafFolder> getFolderList(String rafPath) throws RafException {
        //FIXME: yetki kontrolleri, sıralama v.b.
        sendAuditLog("", "GET_FOLDER_LIST", rafPath );
        return rafRepository.getFolderList(rafPath);
    }

    public RafCollection getCollection(String id) throws RafException {
        //FIXME: yetki kontrolleri
        RafCollection result = rafRepository.getCollectionById(id);

        if( isReadLogEnabled() ){
            sendAuditLog(id, "GET_FOLDER_CONTENT", result.getPath() );
        }
        return result;
    }
    
    public RafCollection getCategoryCollection(Long categoryId, String category, String categoryPath, String rootPath ) throws RafException {
        //FIXME: sıralama, yetki v.s.
        if( isReadLogEnabled() ){
            sendAuditLog( category, "GET_CATEGORY_CONTENT", rootPath );
        }
        return rafRepository.getCategoryCollection(categoryId, category, categoryPath, rootPath, false);
    }
    
    public RafCollection getTagCollection( String tag, String rootPath ) throws RafException {
        //FIXME: sıralama, yetki v.s.
        if( isReadLogEnabled() ){
            sendAuditLog(tag, "GET_TAG_CONTENT", rootPath );
        }
        return rafRepository.getTagCollection(tag, rootPath);
    }
    
    public RafCollection getCategoryCollectionById(Long categoryId, String rootPath ) throws RafException {
        //FIXME: sıralama, yetki v.s.
        //FIXME: NPE kontorol
        RafCategory cat = categoryService.findById(categoryId);
        if( isReadLogEnabled() ){
            sendAuditLog( cat.getName(), "GET_CATEGORY_CONTENT", rootPath );
        }
        return getCategoryCollection( cat.getId(), cat.getName(), cat.getPath(), rootPath);
    }

    /**
     * Verilen RafFolder modeli örnek olarak kullanılır.
     * 
     * path, title, description gibi alanlar üzerinden yeni bir RafFolder oluşturulur.
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
        sendAuditLog( result.getId(), "CREATE_FOLDER", result.getPath() );
        return result;
        
    }
    
    public RafRecord createRecord(RafRecord record) throws RafException {
        //FIXME: yetki kontrolü
        //TODO: record eklendiğine dair burada bir event fırlatmak lazım.
        RafRecord result = rafRepository.createRecord(record);
        
        sendEventLog("CreateRecord", result);
        sendAuditLog( result.getId(), "CREATE_RECORD", result.getPath() );
        return result;
    }
    
    public void saveRecord(RafRecord record) throws RafException {
        //FIXME: yetki kontrolü
        //TODO: record eklendiğine dair burada bir event fırlatmak lazım.
        
        
        
        rafRepository.saveRecord(record);
        
        sendEventLog("Saverecord", record);
        sendAuditLog( record.getId(), "SAVE_RECORD", record.getPath() );

    }
    
    /**
     * Verilen path için folder oluşturur.
     * 
     * Title, Desctription gibi alanlar doğal olarak doldurulmazlar
     * @param folderPath
     * @return
     * @throws RafException 
     */
    public RafFolder createFolder(String folderPath) throws RafException {
        //FIXME: yetki kontrolü
        //TODO: klasör eklendiğine dair burada bir event fırlatmak lazım.
        RafFolder result = rafRepository.createFolder(folderPath);
        
        sendEventLog("CreateFolder", result);
        sendAuditLog( result.getId(), "CREATE_FOLDER", result.getPath() );
        return result;
        
    }
    
    public RafDocument checkin(String fileName, InputStream in) throws RafException {
        //FIXME: yetki kontrolü
        RafDocument result = (RafDocument) rafRepository.checkin(fileName, in);
        
        sendEventLog("CheckInDocument", result);
        sendAuditLog( result.getId(), "CHECKIN_DOCUMENT", result.getPath() );
        return result;
    }
    
    public RafDocument uploadDocument(String fileName, InputStream in) throws RafException {
        //FIXME: yetki kontrolü
        RafDocument result = rafRepository.uploadDocument(fileName, in);
        
        sendEventLog("UploadDocument", result);
        sendAuditLog( result.getId(), "UPLOAD_DOCUMENT", result.getPath() );
        return result;
    }

    public RafObject getRafObject(String id) throws RafException {
        //FIXME: yetki kontrolü gerekli.
        //FIXME: event fırlatalım ki log yazılabilsin v.b.
        RafObject result = rafRepository.getRafObject(id);
        if( isReadLogEnabled() ){
            sendAuditLog( result.getId(), "READ_OBJECT", result.getPath() );
        }
        return result;
    }
    
    public RafObject getRafObjectByPath(String path) throws RafException {
        //FIXME: yetki kontrolü gerekli.
        //FIXME: event fırlatalım ki log yazılabilsin v.b.
        RafObject result = rafRepository.getRafObjectByPath(path);
        if( isReadLogEnabled() ){
            sendAuditLog( result.getId(), "READ_OBJECT", result.getPath() );
        }
        return result;
    }

    /**
     * Geriye verilen RafDocument için eğer varsa Sürüm Tarihçesini döndürür.
     * @param object
     * @return
     * @throws RafException 
     */
    public List<RafVersion> getVersionHistory(RafDocument object) throws RafException {
        return rafRepository.getVersionHistory(object);
    }
    
    public InputStream getDocumentContent(String id) throws RafException {
        if( isReadLogEnabled() ){
            sendAuditLog( id, "READ_DOCUMENT_CONTENT", "" );
        }
        return rafRepository.getDocumentContent(id);
    }
    
    public InputStream getPreviewContent(String id) throws RafException {
        if( isReadLogEnabled() ){
            sendAuditLog( id, "READ_PREVIEW_CONTENT", "" );
        }
        return rafRepository.getPreviewContent(id);
    }

    public void saveMetadata(String id, RafMetadata data) throws RafException {
        //FIXME: Yetki kontrolü + event
        sendAuditLog( id, "SAVE_METADATA", "" );
        rafRepository.saveMetadata(id, data);
    }

    /**
     * RafObject üzerinde bulunan title, info v.b. alanları günceller.
     *
     * @param data
     * @throws RafException
     */
    public void saveProperties(RafObject data) throws RafException {
        //FIXME: Yetki kontrolü + event
        sendAuditLog( data.getId(), "SAVE_PROPERTIES", data.getPath() );
        rafRepository.saveProperties(data);
    }

    public void deleteObject(RafObject data) throws RafException {
        //FIXME: Yetki kontrolü
        sendAuditLog( data.getId(), "DELETE_OBJECT", data.getPath() );
        deleteObject(data.getId());
    }

    public void deleteObject(String id) throws RafException {
        //FIXME: Yetki kontrolü
        RafObject obj = rafRepository.getRafObject(id);
        sendEventLog("DeleteObject", obj);
        sendAuditLog( id, "DELETE_OBJECT", "" );
        rafRepository.deleteObject(id);
    }

    public void copyObject(RafObject from, RafFolder to) throws RafException {
        //FIXME: yetki kontrolü
        sendAuditLog( from.getId(), "COPY_OBJECT_FROM", from.getPath() );
        sendAuditLog( to.getId(), "COPY_OBJECT_TO", to.getPath() );
        rafRepository.copyObject(from, to);
    }
    
    public void copyObject(RafObject from, RafRecord to) throws RafException {
        //FIXME: yetki kontrolü
        sendAuditLog( from.getId(), "COPY_OBJECT_FROM", from.getPath() );
        sendAuditLog( to.getId(), "COPY_OBJECT_TO", to.getPath() );
        rafRepository.copyObject(from, to);
    }
    
    public void copyObject(List<RafObject> from, RafFolder to) throws RafException {
        //FIXME: yetki kontrolü
        for( RafObject o : from ){
            sendAuditLog( o.getId(), "COPY_OBJECT_FROM", o.getPath() );
        }
        sendAuditLog( to.getId(), "COPY_OBJECT_TO", to.getPath() );
        rafRepository.copyObject(from, to);
    }
    
    public void copyObject(List<RafObject> from, RafRecord to) throws RafException {
        //FIXME: yetki kontrolü
        for( RafObject o : from ){
            sendAuditLog( o.getId(), "COPY_OBJECT_FROM", o.getPath() );
        }
        sendAuditLog( to.getId(), "COPY_OBJECT_TO", to.getPath() );
        rafRepository.copyObject(from, to);
    }

    public void moveObject(List<RafObject> from, RafFolder to) throws RafException {
        //FIXME: yetki kontrolü
        for( RafObject o : from ){
            sendAuditLog( o.getId(), "MOVE_OBJECT_FROM", o.getPath() );
        }
        sendAuditLog( to.getId(), "MOVE_OBJECT_TO", to.getPath() );
        rafRepository.moveObject(from, to);
    }

    public RafFolder getFolder(String rafPath) throws RafException {
        //FIXME: yetki kontrolleri, sıralama v.b.
        return rafRepository.getFolder(rafPath);
    }

    public List<RafFolder> getChildren(String path) throws RafException {
        return rafRepository.getChildren(path);
    }

    public RafNode getProcessRafNode() throws RafException{
        return rafRepository.getProcessRafNode();
    }
    
    protected void sendEventLog( String eventType, RafObject object ){

        //FIXME: ıdentity
        EventLogCommand command = EventLogCommandBuilder.forRaf("RAF")
                    .eventType(eventType)
                    .forRafObject(object)
                    .message("event." + eventType + "$%&" + ( identity != null ?  identity.getUserName() : "Sistem" ) + "$%&" + object.getTitle())
                    .user(identity != null ? identity.getLoginName() : "SYSTEM")
                    .build();
        
        commandSender.sendCommand(command);
        
    }
    
    protected void sendAuditLog( String id, String action, String path ){
        AuditLogCommand command = new AuditLogCommand("RAF", Long.MIN_VALUE, id, action, "RAF", identity.getLoginName(), path);
        commandSender.sendCommand(command);
    }

    protected boolean isReadLogEnabled(){
        if( readLogEnabled == null ){
            readLogEnabled = "true".equals( ConfigResolver.getPropertyValue("auditLog.read", "false"));
        }
        
        return readLogEnabled;
    }
}
