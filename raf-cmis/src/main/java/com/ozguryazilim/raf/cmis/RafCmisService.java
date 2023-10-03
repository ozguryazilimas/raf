package com.ozguryazilim.raf.cmis;

import com.ozguryazilim.raf.RafException;
import org.apache.chemistry.opencmis.commons.data.*;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.server.support.wrapper.CallContextAwareCmisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class RafCmisService extends AbstractCmisService implements CallContextAwareCmisService {

    private final RafCmisRepositoryManager repositoryManager;
    private CallContext context;
    private static final Logger LOG = LoggerFactory.getLogger(RafCmisRepository.class);

    public RafCmisService(
            final RafCmisRepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    public void setCallContext(CallContext context) {
        this.context = context;
    }

    public CallContext getCallContext() {
        return context;
    }

    public RafCmisRepository getRepository() {
        return repositoryManager.getRepository(getCallContext()
                .getRepositoryId());
    }

    @Override
    public RepositoryInfo getRepositoryInfo(String repositoryId,
                                            ExtensionsData extension) {
        for (RafCmisRepository fsr : repositoryManager.getRepositories()) {
            if (fsr.getRepositoryId().equals(repositoryId)) {
                return fsr.getRepositoryInfo(getCallContext());
            }
        }

        throw new CmisObjectNotFoundException("Repository not found: '" + repositoryId);
    }

    @Override
    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
        List<RepositoryInfo> result = new ArrayList<RepositoryInfo>();

        for (RafCmisRepository fsr : repositoryManager.getRepositories()) {
            result.add(fsr.getRepositoryInfo(getCallContext()));
        }

        return result;
    }

    @Override
    public TypeDefinitionList getTypeChildren(String repositoryId,
                                              String typeId, Boolean includePropertyDefinitions,
                                              BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return getRepository().getTypeChildren(getCallContext(), typeId,
                includePropertyDefinitions, maxItems, skipCount);
    }

    @Override
    public TypeDefinition getTypeDefinition(String repositoryId, String typeId,
                                            ExtensionsData extension) {
        return getRepository().getTypeDefinition(getCallContext(), typeId);
    }

    @Override
    public ObjectInFolderList getChildren(String repositoryId, String folderId,
                                          String filter, String orderBy, Boolean includeAllowableActions,
                                          IncludeRelationships includeRelationships, String renditionFilter,
                                          Boolean includePathSegment, BigInteger maxItems,
                                          BigInteger skipCount, ExtensionsData extension) {
        ObjectInFolderList children = null;
        try {
            children = getRepository().getChildren(getCallContext(), folderId, filter,
                    includeAllowableActions, includePathSegment, maxItems,
                    skipCount, this);
        } catch (RafException e) {
            LOG.error(e.getMessage(), e);
        }

        return children;
    }

    @Override
    public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        List<ObjectInFolderContainer> children = null;
        try {
            children = getRepository().getDescendants(getCallContext(), folderId, depth, filter, includeAllowableActions, includePathSegment, this);
        } catch (RafException e) {
            LOG.error(e.getMessage(), e);
        }

        return children;
    }

    
    
    @Override
    public List<ObjectParentData> getObjectParents(String repositoryId,
                                                   String objectId, String filter, Boolean includeAllowableActions,
                                                   IncludeRelationships includeRelationships, String renditionFilter,
                                                   Boolean includeRelativePathSegment, ExtensionsData extension) {
        List<ObjectParentData> parents = null;
        try {
            parents = getRepository().getObjectParents(getCallContext(), objectId,
                    filter, includeAllowableActions, includeRelativePathSegment,
                    this);
        } catch (RafException e) {
            LOG.warn("Parent nodelar alınırken hata oluştu!");
        }

        return parents;
    }

    @Override
    public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {
        try {
            return getRepository().getFolderParent( getCallContext(), folderId, filter, this, extension);
        } catch (RafException ex) {
            LOG.error("Folder Parent not Found", ex);
            return null;
        }
    }

    
    
    @Override
    public String createDocument(String repositoryId, Properties properties,
                                 String folderId, ContentStream contentStream,
                                 VersioningState versioningState, List<String> policies,
                                 Acl addAces, Acl removeAces, ExtensionsData extension) {
        return getRepository().createDocument(getCallContext(), properties,
                folderId, contentStream, versioningState);
    }

    @Override
    public String createFolder(String repositoryId, Properties properties,
                               String folderId, List<String> policies, Acl addAces,
                               Acl removeAces, ExtensionsData extension) {
        return getRepository().createFolder(getCallContext(), properties,
                folderId);
    }

    @Override
    public ContentStream getContentStream(String repositoryId, String objectId,
                                          String streamId, BigInteger offset, BigInteger length,
                                          ExtensionsData extension) {
        return getRepository().getContentStream(getCallContext(), objectId,
                offset, length);
    }

    @Override
    public ObjectData getObject(String repositoryId, String objectId,
                                String filter, Boolean includeAllowableActions,
                                IncludeRelationships includeRelationships, String renditionFilter,
                                Boolean includePolicyIds, Boolean includeAcl,
                                ExtensionsData extension) {
        ObjectData object = null;

        try {
            object =  getRepository().getObject(getCallContext(), objectId, null,
                    filter, includeAllowableActions, includeAcl, this);
        } catch (RafException e) {
            LOG.error(e.getMessage(), e);
        }

        return object;
    }

    @Override
    public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
        ObjectData object = null;

        try {
            object =  getRepository().getObjectByPath(getCallContext(), path, null, filter, includeAllowableActions, includeAcl, this);
        } catch (RafException e) {
            LOG.error(e.getMessage(), e);
        }

        return object;
    }

    
    
    @Override
    public Properties getProperties(String repositoryId, String objectId,
                                    String filter, ExtensionsData extension) {
        try {
            return getRepository().getObject(getCallContext(),
                    objectId, null, filter, false, false, this)
                    .getProperties();
        } catch (RafException e) {
            LOG.warn("Belge içeriği alınırken hata oluştu!");
            return null;
        }
    }

    @Override
    public void moveObject(String repositoryId, Holder<String> objectId,
                           String targetFolderId, String sourceFolderId,
                           ExtensionsData extension) {
        try {
            getRepository().moveObject(getCallContext(), objectId, targetFolderId,
                    this);
        } catch (RafException e) {
            LOG.warn("Taşıma işlemi sırasında hata oluştu!");
        }
    }

    @Override
    public void deleteObject(String repositoryId, String objectId, Boolean allVersions,
                             ExtensionsData extension) {
        getRepository().deleteObject(getCallContext(), objectId);
    }


}
