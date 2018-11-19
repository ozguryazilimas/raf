package com.ozguryazilim.raf.cmis;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityOrderBy;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CreatablePropertyTypesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectParentDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PartialContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RafCmisRepository {

    private static final String USER_UNKNOWN = "SYSTEM";

    private final String repositoryId;

    private final RafCmisTypeManager typeManager;

    private final Map<String, Boolean> readWriteUserMap;

    private final RepositoryInfo repositoryInfo10;

    private final RepositoryInfo repositoryInfo11;
    
    private RafDefinition rafdef;

    private static final Logger LOG = LoggerFactory.getLogger(RafCmisRepository.class);

    public RafCmisRepository(RafDefinition rafdef, final RafCmisTypeManager typeManager) {

        this.rafdef = rafdef;
        this.repositoryId = rafdef.getCode();

        this.typeManager = typeManager;

        readWriteUserMap = new HashMap<String, Boolean>();

        repositoryInfo10 = createRepositoryInfo(CmisVersion.CMIS_1_0);
        repositoryInfo11 = createRepositoryInfo(CmisVersion.CMIS_1_1);
    }

    private RepositoryInfo createRepositoryInfo(CmisVersion cmisVersion) {
        assert cmisVersion != null;

        RepositoryInfoImpl repositoryInfo = new RepositoryInfoImpl();

        repositoryInfo.setId(repositoryId);
        repositoryInfo.setName(rafdef.getName());
        repositoryInfo.setVendorName("Özgür Yazılım A.Ş.");
        repositoryInfo.setCmisVersion(cmisVersion);
        repositoryInfo.setDescription("Raf CMIS Repository");
        repositoryInfo.setProductName("Raf DMS");
        repositoryInfo.setProductVersion("1.0.0");
        repositoryInfo.setRootFolder(rafdef.getCode());

        List<BaseTypeId> changesOnTypes = new ArrayList<>();
        changesOnTypes.add(BaseTypeId.CMIS_FOLDER);
        changesOnTypes.add(BaseTypeId.CMIS_DOCUMENT);

        /* İleride belki CMIS üzerinden aşağıdaki bilgilerin değişimini destekleriz.
        changesOnTypes.add(BaseTypeId.CMIS_ITEM);
        changesOnTypes.add(BaseTypeId.CMIS_POLICY);
        changesOnTypes.add(BaseTypeId.CMIS_RELATIONSHIP);
         */
        repositoryInfo.setChangesOnType(changesOnTypes);

        repositoryInfo.setPrincipalAnonymous("anonymous");
        repositoryInfo.setPrincipalAnyone("anyone");

        RepositoryCapabilitiesImpl capabilities = new RepositoryCapabilitiesImpl();
        capabilities.setSupportsGetDescendants(Boolean.TRUE);
        capabilities.setSupportsGetFolderTree(Boolean.TRUE);
        capabilities.setSupportsUnfiling(Boolean.FALSE);
        capabilities.setSupportsMultifiling(Boolean.FALSE);
        capabilities.setSupportsVersionSpecificFiling(Boolean.FALSE);

        capabilities.setAllVersionsSearchable(Boolean.FALSE);
        //TODO: ACL kısmına ayrıca bakılacak
        capabilities.setCapabilityAcl(CapabilityAcl.NONE);
        //TODO: Buraya detaylı bakmak lazım. Özellikle entegrasyonlar property değiştirmek isterler.
        capabilities.setCapabilityChanges(CapabilityChanges.NONE);
        //TODO: ChekIn-CheckOut desteği geldiğinde düzelmeli.
        capabilities.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.ANYTIME);
        capabilities.setCapabilityJoin(CapabilityJoin.NONE);
        capabilities.setCapabilityOrderBy(CapabilityOrderBy.NONE);
        //TODO: Bunu da ayrıca destekleyeceğiz. 
        capabilities.setCapabilityQuery(CapabilityQuery.NONE);
        //TODO: Bı da ayrıca desteklenebilecek bir özellik sanırım
        capabilities.setCapabilityRendition(CapabilityRenditions.NONE);

        CreatablePropertyTypesImpl creatablePropertyTypes = new CreatablePropertyTypesImpl();
        //TODO: Buraya biraz bakmak lazım. Özellikle Redmine/Tekir gibi yerlerin aslında ihtiyacı var.
        capabilities.setCreatablePropertyTypes(creatablePropertyTypes);

        //TODO: Chekin ve out için lazım
        capabilities.setIsPwcSearchable(Boolean.FALSE);
        capabilities.setIsPwcUpdatable(Boolean.FALSE);

        repositoryInfo.setCapabilities(capabilities);

        return repositoryInfo;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setUserReadOnly(String user) {
        if (user == null || user.length() == 0) {
            return;
        }

        readWriteUserMap.put(user, true);
    }

    public void setUserReadWrite(String user) {
        if (user == null || user.length() == 0) {
            return;
        }

        readWriteUserMap.put(user, false);
    }

    public RepositoryInfo getRepositoryInfo(CallContext context) {
        if (context.getCmisVersion() == CmisVersion.CMIS_1_0) {
            return repositoryInfo10;
        } else {
            return repositoryInfo11;
        }
    }

    public TypeDefinitionList getTypeChildren(CallContext context,
            String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount) {
        return typeManager.getTypeChildren(context, typeId,
                includePropertyDefinitions, maxItems, skipCount);
    }

    public TypeDefinition getTypeDefinition(CallContext context, String typeId) {

        return typeManager.getTypeDefinition(context, typeId);
    }

    public String createDocument(CallContext context, Properties properties,
            String folderId, ContentStream contentStream,
            VersioningState versioningState) {

        String name = RafCmisUtils.getStringProperty(properties, PropertyIds.NAME);
        String fileName = name.substring(name.lastIndexOf(File.separatorChar));

        RafDocument document = null;

        try {
            String path = getRafService().getCollection(folderId).getPath() + "/" + fileName;
            LOG.info("File Path: {}", path);
            document = getRafService().uploadDocument(path, contentStream.getStream());
        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
        }

        return document.getId();
    }

    public String createFolder(CallContext context, Properties properties,
            String folderId) {
        RafFolder folder = new RafFolder();
        folder.setParentId(folderId);

        try {
            folder.setPath(getRafService().getCollection(folderId).getPath() + "/" + folder.getName());
        } catch (RafException ex) {
            LOG.error("Collection alınırken hata ile karşılaşıldı!", ex.getMessage());
        }

        try {
            getRafService().createFolder(folder);
        } catch (RafException ex) {
            LOG.error("Raf Tanımlaması Yapılamadı", ex.getMessage());
        }

        return folder.getId();
    }

    public ObjectData moveObject(CallContext context, Holder<String> objectId,
            String targetFolderId, ObjectInfoHandler objectInfos) throws RafException {

        RafObject from = getRafService().getRafObject(objectId.getValue());

        String path = getRafService().getCollection(targetFolderId).getPath();

        RafFolder to = getRafService().getFolder(path);

        List<RafObject> ls = new ArrayList<>();
        ls.add(from);
        getRafService().moveObject(ls, to);

        return compileObjectData(context, from, null, false, false,
                false, objectInfos);
    }

    public void deleteObject(CallContext context, String objectId) {
        try {
            getRafService().deleteObject(objectId);
        } catch (RafException e) {
            LOG.info("Silme işlemi sırasında hata oluştu!");
        }
    }

    public ObjectData getObject(CallContext context, String objectId,
            String versionServicesId, String filter,
            Boolean includeAllowableActions, Boolean includeAcl,
            ObjectInfoHandler objectInfos) throws RafException {

        boolean iaa = RafCmisUtils.getBooleanParameter(
                includeAllowableActions, false);
        boolean iacl = RafCmisUtils.getBooleanParameter(includeAcl, false);

        Set<String> filterCollection = RafCmisUtils.splitFilter(filter);

        RafObject rafObject = null;
        if (rafdef.getCode().equals(objectId)) {
            //FIXME: aslında burada sanal bir Folder oluşturmak lazım. Bunun içeriğine de WebDAV'da yaptığımız gibi kullanıcı raflarını koymak lazım.
            //rafObject = getRafService().getRafObjectByPath("/SHARED");
            return getRootFolder(context, objectId, versionServicesId, filter, includeAllowableActions, includeAcl, objectInfos);
        } else {
            rafObject = getRafService().getRafObject(objectId);
        }

        return compileObjectData(context, rafObject, filterCollection, iaa, iacl,
                false, objectInfos);
    }

    public ContentStream getContentStream(CallContext context, String objectId,
            BigInteger offset, BigInteger length) {

        InputStream inputStream = null;
        try {
            inputStream = getRafService().getDocumentContent(objectId);
        } catch (RafException e) {
            LOG.warn("Belge içeriği alınırken hata oluştu!");
        }
        RafObject rafObject = null;
        try {
            rafObject = getRafService().getRafObject(objectId);
        } catch (RafException e) {
            LOG.warn("Raf objesi alınırken hata oluştu!");
        }

        // compile data
        ContentStreamImpl result;
        if ((offset != null && offset.longValue() > 0) || length != null) {
            result = new PartialContentStreamImpl();
        } else {
            result = new ContentStreamImpl();
        }

        result.setFileName(rafObject.getName());
        try {
            result.setLength(BigInteger.valueOf(inputStream.available()));
        } catch (IOException e) {
            LOG.warn("Belge içeriği alınırken hata oluştu!");
        }
        result.setMimeType(MimeTypes.getMIMEType(rafObject.getMimeType()));
        result.setStream(inputStream);

        return result;
    }

    public ObjectInFolderList getChildren(CallContext context, String folderId,
            String filter, Boolean includeAllowableActions,
            Boolean includePathSegment, BigInteger maxItems,
            BigInteger skipCount, ObjectInfoHandler objectInfos) throws RafException {

        LOG.debug("Ask children for '{}'", folderId);

        /* Şu anda her raf ayrı repo olduğuna göre aslında içerikleri normal birer folder'a dönüşmüş durumda.
        if (rafdef.getCode().equals(folderId)) {
            return getRootChildren(context, folderId, filter, includeAllowableActions,
                    includePathSegment, maxItems, skipCount, objectInfos);
        }
        */
        
        //Eğer root folder ise node id bulmak lazım
        if (rafdef.getCode().equals(folderId)) {
            folderId = rafdef.getNode().getId();
        }
        
        Set<String> filterCollection = RafCmisUtils.splitFilter(filter);

        boolean iaa = RafCmisUtils.getBooleanParameter(
                includeAllowableActions, false);
        boolean ips = RafCmisUtils.getBooleanParameter(includePathSegment,
                false);

        int skip = (skipCount == null ? 0 : skipCount.intValue());
        if (skip < 0) {
            skip = 0;
        }

        int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
        if (max < 0) {
            max = Integer.MAX_VALUE;
        }

        RafObject rafObject = getRafService().getRafObject(folderId);

        if (context.isObjectInfoRequired()) {
            compileObjectData(context, rafObject, null, false, false,
                    false, objectInfos);
        }

        ObjectInFolderListImpl result = new ObjectInFolderListImpl();
        result.setObjects(new ArrayList<ObjectInFolderData>());
        result.setHasMoreItems(false);
        int count = 0;

        RafCollection collection = getRafService().getCollection(folderId);

        for (RafObject child : collection.getItems()) {

            count++;

            if (skip > 0) {
                skip--;
                continue;
            }

            if (result.getObjects().size() >= max) {
                result.setHasMoreItems(true);
                continue;
            }

            ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
            objectInFolder.setObject(compileObjectData(context, child,
                    filterCollection, iaa, false, false, objectInfos));
            if (ips) {
                objectInFolder.setPathSegment(child.getName());
            }

            result.getObjects().add(objectInFolder);
        }

        result.setNumItems(BigInteger.valueOf(count));

        return result;
    }

    /**
     * Geriye Raf için Root folder altında bulunan sanal klasörleri döndürür.
     *
     * PRIVATE, SHARED ve yetkili olunan raf listesi
     *
     *
     * @param context
     * @param folderId
     * @param filter
     * @param includeAllowableActions
     * @param includePathSegment
     * @param maxItems
     * @param skipCount
     * @param objectInfos
     * @return
     * @throws RafException
     */
    private ObjectInFolderList getRootChildren(CallContext context, String folderId,
            String filter, Boolean includeAllowableActions,
            Boolean includePathSegment, BigInteger maxItems,
            BigInteger skipCount, ObjectInfoHandler objectInfos) throws RafException {
        
        ObjectInFolderListImpl result = new ObjectInFolderListImpl();
        result.setObjects(new ArrayList<ObjectInFolderData>());
        result.setHasMoreItems(false);

        Set<String> filterCollection = RafCmisUtils.splitFilter(filter);

        boolean iaa = RafCmisUtils.getBooleanParameter( includeAllowableActions, false);
        boolean ips = RafCmisUtils.getBooleanParameter(includePathSegment, false);

        //FIXME: kullanıcı adı ve yetkisi kontrol edilerek gelmiş olamalı
        List<RafDefinition> rafDefs = getRafDefinitionService().getRafsForUser("telve", true);
        
        for( RafDefinition rafDef : rafDefs ){
            ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
            objectInFolder.setObject(compileObjectData(context, rafDef.getNode(), filterCollection, iaa, false, false, objectInfos));
            objectInFolder.setPathSegment(rafDef.getName());
            result.getObjects().add(objectInFolder);
        }
        
        //SHARED folder
        ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
        RafObject sharedRaf = getRafService().getRafObjectByPath("/SHARED");
        objectInFolder.setObject(compileObjectData(context, sharedRaf, filterCollection, iaa, false, false, objectInfos));
        objectInFolder.setPathSegment(sharedRaf.getName());
        result.getObjects().add(objectInFolder);
        
        //TODO: Burada PRIVATE folder ve Yetki dahilindeki raf'ların listesi eklenecek
        
        
        result.setNumItems(BigInteger.valueOf(result.getObjects().size()));
        return result;
    }

    public List<ObjectParentData> getObjectParents(CallContext context,
            String objectId, String filter,
            Boolean includeAllowableActions,
            Boolean includeRelativePathSegment,
            ObjectInfoHandler objectInfos) throws RafException {

        Set<String> filterCollection = RafCmisUtils.splitFilter(filter);

        boolean iaa = RafCmisUtils.getBooleanParameter(
                includeAllowableActions, false);
        boolean irps = RafCmisUtils.getBooleanParameter(
                includeRelativePathSegment, false);

        RafObject rafObject = getRafService().getRafObject(objectId);

        String rootPath = "/";

        if (rootPath.equals(rafObject.getPath())) {
            return Collections.emptyList();
        }

        if (context.isObjectInfoRequired()) {
            compileObjectData(context, rafObject, null, false, false, false,
                    objectInfos);
        }

        RafObject parent = getRafService().getRafObject(rafObject.getParentId());
        ObjectData object = compileObjectData(context, parent,
                filterCollection, iaa, false, false, objectInfos);

        ObjectParentDataImpl result = new ObjectParentDataImpl();
        result.setObject(object);
        if (irps) {
            result.setRelativePathSegment(rafObject.getName());
        }

        return Collections.<ObjectParentData>singletonList(result);
    }

    private ObjectData getRootFolder(CallContext context, String objectId,
            String versionServicesId, String filter,
            Boolean includeAllowableActions, Boolean includeAcl,
            ObjectInfoHandler objectInfos) {
        ObjectDataImpl result = new ObjectDataImpl();
        ObjectInfoImpl objectInfo = new ObjectInfoImpl();

        String typeId = BaseTypeId.CMIS_FOLDER.value();
        objectInfo.setBaseType(BaseTypeId.CMIS_FOLDER);

        PropertiesImpl prop = new PropertiesImpl();

        String id = rafdef.getCode();
        addPropertyId(prop, typeId, null, PropertyIds.OBJECT_ID, id);
        objectInfo.setId(id);

        String name = rafdef.getCode();
        addPropertyString(prop, typeId, null, PropertyIds.NAME, name);
        objectInfo.setName(name);

        addPropertyString(prop, typeId, null, PropertyIds.PATH, "/");
        objectInfo.setHasParent(false);

        if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
            addPropertyString(prop, typeId, null, PropertyIds.DESCRIPTION, null);
            addPropertyIdList(prop, typeId, null, PropertyIds.SECONDARY_OBJECT_TYPE_IDS, null);
        }

        addPropertyId(prop, typeId, null, PropertyIds.BASE_TYPE_ID, typeId);
        addPropertyId(prop, typeId, null, PropertyIds.OBJECT_TYPE_ID, typeId);

        addPropertyString(prop, typeId, null, PropertyIds.CREATED_BY, USER_UNKNOWN);
        addPropertyString(prop, typeId, null, PropertyIds.LAST_MODIFIED_BY, USER_UNKNOWN);
        objectInfo.setCreatedBy(USER_UNKNOWN);

        GregorianCalendar lastModified = RafCmisUtils.millisToCalendar((new Date()).getTime());
        addPropertyDateTime(prop, typeId, null, PropertyIds.CREATION_DATE, lastModified);
        addPropertyDateTime(prop, typeId, null, PropertyIds.LAST_MODIFICATION_DATE, lastModified);

        objectInfo.setCreationDate(lastModified);
        objectInfo.setLastModificationDate(lastModified);

        addPropertyString(prop, typeId, null, PropertyIds.CHANGE_TOKEN, null);

        objectInfo.setObject(result);
        objectInfos.addObjectInfo(objectInfo);

        result.setProperties(prop);

        
         Set<Action> aas = EnumSet.noneOf(Action.class);

        addAction(aas, Action.CAN_GET_OBJECT_PARENTS, false);
        addAction(aas, Action.CAN_GET_PROPERTIES, true);
        addAction(aas, Action.CAN_MOVE_OBJECT, false);
        addAction(aas, Action.CAN_GET_ACL, true);
        addAction(aas, Action.CAN_GET_CONTENT_STREAM, false);
        addAction(aas, Action.CAN_GET_ALL_VERSIONS, false);
        addAction(aas, Action.CAN_GET_CHILDREN, true);
        addAction(aas, Action.CAN_CREATE_DOCUMENT, false);
        addAction(aas, Action.CAN_CREATE_FOLDER, false);
        addAction(aas, Action.CAN_CREATE_ITEM, false);
        addAction(aas, Action.CAN_CREATE_RELATIONSHIP, false);
        addAction(aas, Action.CAN_DELETE_OBJECT, false);
        addAction(aas, Action.CAN_DELETE_TREE, false);

        AllowableActionsImpl actions = new AllowableActionsImpl();
        actions.setAllowableActions(aas);
        result.setAllowableActions(actions);
        
        
        AccessControlListImpl acls = new AccessControlListImpl();
        acls.setExact(Boolean.TRUE);
        result.setAcl(acls);
        
        return result;
    }

    private ObjectData compileObjectData(CallContext context, RafObject rafObject,
            Set<String> filter, boolean includeAllowableActions,
            boolean includeAcl, boolean userReadOnly,
            ObjectInfoHandler objectInfos) {
        ObjectDataImpl result = new ObjectDataImpl();
        ObjectInfoImpl objectInfo = new ObjectInfoImpl();

        result.setProperties(compileProperties(context, rafObject, filter,
                objectInfo));

        if (includeAllowableActions) {
            result.setAllowableActions(compileAllowableActions(rafObject,
                    userReadOnly));
        }

        if (includeAcl) {
            result.setAcl(compileAcl(rafObject));
            result.setIsExactAcl(true);
        }

        if (context.isObjectInfoRequired()) {
            objectInfo.setObject(result);
            objectInfos.addObjectInfo(objectInfo);
        }

        return result;
    }

    private Properties compileProperties(CallContext context, RafObject rafObject,
            Set<String> orgfilter, ObjectInfoImpl objectInfo) {
        if (rafObject == null) {
            throw new IllegalArgumentException("Hata oluştu!");
        }

        Set<String> filter = (orgfilter == null ? null : new HashSet<>(
                orgfilter));

        String typeId = null;

        if (rafObject instanceof RafFolder) {
            typeId = BaseTypeId.CMIS_FOLDER.value();
            objectInfo.setBaseType(BaseTypeId.CMIS_FOLDER);
        } else {
            typeId = BaseTypeId.CMIS_DOCUMENT.value();
            objectInfo.setBaseType(BaseTypeId.CMIS_DOCUMENT);
        }
        objectInfo.setTypeId(typeId);
        objectInfo.setHasAcl(true);
        objectInfo.setHasContent(true);
        objectInfo.setHasParent(true);
        objectInfo.setVersionSeriesId(null);
        objectInfo.setIsCurrentVersion(true);
        objectInfo.setRelationshipSourceIds(null);
        objectInfo.setRelationshipTargetIds(null);
        objectInfo.setRenditionInfos(null);
        objectInfo.setSupportsDescendants(false);
        objectInfo.setSupportsFolderTree(false);
        objectInfo.setSupportsPolicies(false);
        objectInfo.setSupportsRelationships(false);
        objectInfo.setWorkingCopyId(null);
        objectInfo.setWorkingCopyOriginalId(null);

        try {
            PropertiesImpl result = new PropertiesImpl();

            String id = rafObject.getId();
            addPropertyId(result, typeId, filter, PropertyIds.OBJECT_ID, id);
            objectInfo.setId(id);

            addPropertyString(result, typeId, filter, PropertyIds.PARENT_ID, rafObject.getParentId());
            
            String name = rafObject.getName();
            addPropertyString(result, typeId, filter, PropertyIds.NAME, name);
            objectInfo.setName(name);

            //FIXME: Buraa PATH işini bir hal yola koymak lazım
            addPropertyString(result, typeId, filter, PropertyIds.PATH, rafObject.getPath());

            addPropertyString(result, typeId, filter, PropertyIds.CREATED_BY,
                    USER_UNKNOWN);
            addPropertyString(result, typeId, filter,
                    PropertyIds.LAST_MODIFIED_BY, USER_UNKNOWN);
            objectInfo.setCreatedBy(USER_UNKNOWN);

            GregorianCalendar lastModified = RafCmisUtils
                    .millisToCalendar(rafObject.getUpdateDate() != null ? rafObject.getUpdateDate().getTime() : (new Date()).getTime());
            addPropertyDateTime(result, typeId, filter,
                    PropertyIds.CREATION_DATE, lastModified);
            addPropertyDateTime(result, typeId, filter,
                    PropertyIds.LAST_MODIFICATION_DATE, lastModified);
            objectInfo.setCreationDate(lastModified);
            objectInfo.setLastModificationDate(lastModified);

            addPropertyString(result, typeId, filter, PropertyIds.CHANGE_TOKEN,
                    null);

            if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
                addPropertyString(result, typeId, filter,
                        PropertyIds.DESCRIPTION, null);
                addPropertyIdList(result, typeId, filter,
                        PropertyIds.SECONDARY_OBJECT_TYPE_IDS, null);
            }

            addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID,
                    typeId);
            addPropertyId(result, typeId, filter,
                    PropertyIds.OBJECT_TYPE_ID,
                    typeId);

            /*
            addPropertyBoolean(result, typeId, filter,
                    PropertyIds.IS_IMMUTABLE, false);
             */
            addPropertyBoolean(result, typeId, filter,
                    PropertyIds.IS_LATEST_VERSION, true);
            addPropertyBoolean(result, typeId, filter,
                    PropertyIds.IS_MAJOR_VERSION, true);
            addPropertyBoolean(result, typeId, filter,
                    PropertyIds.IS_LATEST_MAJOR_VERSION, true);
            addPropertyString(result, typeId, filter,
                    PropertyIds.VERSION_LABEL, rafObject.getName());
            addPropertyId(result, typeId, filter,
                    PropertyIds.VERSION_SERIES_ID, rafObject.getId());
            addPropertyBoolean(result, typeId, filter,
                    PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false);
            addPropertyString(result, typeId, filter,
                    PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, null);
            addPropertyString(result, typeId, filter,
                    PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, null);
            addPropertyString(result, typeId, filter,
                    PropertyIds.CHECKIN_COMMENT, "");
            if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
                addPropertyBoolean(result, typeId, filter,
                        PropertyIds.IS_PRIVATE_WORKING_COPY, false);
            }

            /*
            InputStream inputStream = getRafService().getDocumentContent(rafObject.getId());
            int length = inputStream.available();
             */
            if (rafObject.getLength() == 0) {
                addPropertyBigInteger(result, typeId, filter,
                        PropertyIds.CONTENT_STREAM_LENGTH, null);
                addPropertyString(result, typeId, filter,
                        PropertyIds.CONTENT_STREAM_MIME_TYPE, null);
                addPropertyString(result, typeId, filter,
                        PropertyIds.CONTENT_STREAM_FILE_NAME, null);

                objectInfo.setHasContent(false);
                objectInfo.setContentType(null);
                objectInfo.setFileName(null);
            } else {
                addPropertyInteger(result, typeId, filter,
                        PropertyIds.CONTENT_STREAM_LENGTH, rafObject.getLength());
                addPropertyString(result, typeId, filter,
                        PropertyIds.CONTENT_STREAM_MIME_TYPE,
                        MimeTypes.getMIMEType(rafObject.getMimeType()));
                addPropertyString(result, typeId, filter,
                        PropertyIds.CONTENT_STREAM_FILE_NAME,
                        rafObject.getName());

                objectInfo.setHasContent(true);
                objectInfo.setContentType(MimeTypes.getMIMEType(rafObject.getMimeType()));
                objectInfo.setFileName(rafObject.getName());
            }

            addPropertyId(result, typeId, filter,
                    PropertyIds.CONTENT_STREAM_ID, null);

            return result;
        } catch (CmisBaseException cbe) {
            throw cbe;
        } catch (Exception e) {
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    private void addPropertyId(PropertiesImpl props, String typeId,
            Set<String> filter, String id, String value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyIdImpl(id, value));
    }

    private void addPropertyIdList(PropertiesImpl props, String typeId,
            Set<String> filter, String id, List<String> value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyIdImpl(id, value));
    }

    private void addPropertyString(PropertiesImpl props, String typeId,
            Set<String> filter, String id, String value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyStringImpl(id, value));
    }

    private void addPropertyInteger(PropertiesImpl props, String typeId,
            Set<String> filter, String id, long value) {
        addPropertyBigInteger(props, typeId, filter, id,
                BigInteger.valueOf(value));
    }

    private void addPropertyBigInteger(PropertiesImpl props, String typeId,
            Set<String> filter, String id, BigInteger value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyIntegerImpl(id, value));
    }

    private void addPropertyBoolean(PropertiesImpl props, String typeId,
            Set<String> filter, String id, boolean value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyBooleanImpl(id, value));
    }

    private void addPropertyDateTime(PropertiesImpl props, String typeId,
            Set<String> filter, String id, GregorianCalendar value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyDateTimeImpl(id, value));
    }

    private boolean checkAddProperty(Properties properties, String typeId,
            Set<String> filter, String id) {
        if ((properties == null) || (properties.getProperties() == null)) {
            throw new IllegalArgumentException("Properties null olmamalı!");
        }

        if (id == null) {
            throw new IllegalArgumentException("Id null olmamalı!");
        }

        TypeDefinition type = typeManager.getInternalTypeDefinition(typeId);
        if (type == null) {
            throw new IllegalArgumentException("Bilinmeyen type id: " + typeId);
        }
        if (!type.getPropertyDefinitions().containsKey(id)) {
            //throw new IllegalArgumentException("Bilinmeyen property: " + id);
            LOG.warn("Unknown CMIS property : {}", id);
            return false;
        }

        String queryName = type.getPropertyDefinitions().get(id).getQueryName();

        if ((queryName != null) && (filter != null)) {
            if (!filter.contains(queryName)) {
                return false;
            } else {
                filter.remove(queryName);
            }
        }

        return true;
    }

    private AllowableActions compileAllowableActions(RafObject rafObject,
            boolean userReadOnly) {
        if (rafObject == null) {
            throw new IllegalArgumentException("Dosya null olmamalı!");
        }

        //FIXME: Root davranışını kontrol etmemiz lazım. Sadece RafObject ile yapabili rmiyiz bilmiyorum.
        boolean isRoot = false; //root.equals(rafObject);

        Set<Action> aas = EnumSet.noneOf(Action.class);

        addAction(aas, Action.CAN_GET_OBJECT_PARENTS, !isRoot);
        addAction(aas, Action.CAN_GET_PROPERTIES, true);
        addAction(aas, Action.CAN_MOVE_OBJECT, !userReadOnly && !isRoot);
        addAction(aas, Action.CAN_GET_ACL, true);

        addAction(aas, Action.CAN_GET_CONTENT_STREAM, rafObject.getLength() > 0);

        addAction(aas, Action.CAN_GET_ALL_VERSIONS, true);

        AllowableActionsImpl result = new AllowableActionsImpl();
        result.setAllowableActions(aas);

        return result;
    }

    private void addAction(Set<Action> aas, Action action, boolean condition) {
        if (condition) {
            aas.add(action);
        }
    }

    private Acl compileAcl(RafObject rafObject) {
        AccessControlListImpl result = new AccessControlListImpl();
        return result;
    }

    private RafService getRafService() {
        return BeanProvider.getContextualReference(RafService.class, true);
    }
    
    private RafDefinitionService getRafDefinitionService(){
        return BeanProvider.getContextualReference(RafDefinitionService.class, true);
    }
}
