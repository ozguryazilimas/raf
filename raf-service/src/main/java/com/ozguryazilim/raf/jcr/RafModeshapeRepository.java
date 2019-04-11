package com.ozguryazilim.raf.jcr;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.MetadataConverter;
import com.ozguryazilim.raf.MetadataConverterRegistery;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.encoder.RafEncoder;
import com.ozguryazilim.raf.encoder.RafEncoderFactory;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.models.RafMimeTypes;
import com.ozguryazilim.raf.models.RafNode;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.models.RafVersion;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;
import org.apache.commons.io.IOUtils;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.modeshape.jcr.api.JcrTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@RequestScoped
public class RafModeshapeRepository implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RafModeshapeRepository.class);

    private static final String RAF_ROOT = "/RAF/";
    private static final String PRIVATE_ROOT = "/PRIVATE/";
    private static final String SHARED_ROOT = "/SHARED";
    private static final String PROCESS_ROOT = "/PROCESS";

    private static final String NODE_HIERARCHY = "nt:hierarchyNode";
    private static final String NODE_FOLDER = "nt:folder";
    private static final String NODE_FILE = "nt:file";
    private static final String NODE_CONTENT = "jcr:content";

    private static final String NODE_SEARCH = "nt:base";

    private static final String MIXIN_RECORD = "raf:record";
    private static final String MIXIN_VERSIONABLE = "mix:versionable";

    private static final String MIXIN_TITLE = "mix:title";
    private static final String MIXIN_TAGGABLE = "raf:taggable";
    private static final String MIXIN_RAF = "raf:raf";
    private static final String MIXIN_METADATA = "raf:metadata";

    private static final String PROP_TITLE = "jcr:title";
    private static final String PROP_DESCRIPTON = "jcr:description";
    private static final String PROP_CATEGORY = "raf:category";
    private static final String PROP_CATEGORY_PATH = "raf:categoryPath";
    private static final String PROP_CATEGORY_ID = "raf:categoryId";
    private static final String PROP_TAG = "raf:tags";
    private static final String PROP_RAF_TYPE = "raf:type";
    private static final String PROP_DATA = "jcr:data";

    private static final String PROP_RECORD_TYPE = "raf:recordType";
    private static final String PROP_DOCUMENT_TYPE = "raf:documentType";
    private static final String PROP_MAIN_DOCUMENT = "raf:mainDocument";
    private static final String PROP_ELECTRONIC_DOCUMENT = "raf:electronicDocument";
    private static final String PROP_LOCATION = "raf:location";
    private static final String PROP_PROCESS_ID = "raf:processId";
    private static final String PROP_PROCESS_INST_ID = "raf:processIntanceId";
    private static final String PROP_STATUS = "raf:status";
    private static final String PROP_RECORD_NO = "raf:recordNo";

    private static final String RAF_TYPE_DEFAULT = "DEFAULT";
    private static final String RAF_TYPE_PRIVATE = "PRIVATE";
    private static final String RAF_TYPE_SHARED = "SHARED";
    private static final String RAF_TYPE_PROCESS = "PROCESS";

    private RafEncoder encoder;
    JcrTools jcrTools = new JcrTools();

    @PostConstruct
    public void init() {
        try {
            start();
            String debugMode = ConfigResolver.getProjectStageAwarePropertyValue("raf.repository.debug", "false");
            jcrTools.setDebug("true".equals(debugMode));
        } catch (RafException ex) {
            LOG.error("ModeShape cannot started", ex);
        }
    }

    public void start() throws RafException {
        try {
            encoder = RafEncoderFactory.getEncoder();

            //Engine'de başlatılsın
            Session session = ModeShapeRepositoryFactory.getSession();
            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0000] Rafrepository cannot start", ex);
        }
    }

    public void stop() throws RafException {
        ModeShapeRepositoryFactory.shutdown();
    }

    public RafNode createRafNode(RafDefinition definition) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(RAF_ROOT + definition.getCode());

            Node node = jcrTools.findOrCreateNode(session, fullPath, NODE_FOLDER);

            node.addMixin(MIXIN_RAF);
            node.addMixin(MIXIN_TITLE);
            node.addMixin(MIXIN_TAGGABLE);

            node.setProperty(PROP_RAF_TYPE, RAF_TYPE_DEFAULT);
            node.setProperty(PROP_TITLE, definition.getName());
            node.setProperty(PROP_DESCRIPTON, definition.getInfo());

            RafNode result = nodeToRafNode(node);

            session.save();
            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0001] Raf node cannot create", ex);
        }
    }

    public RafNode updateRafNode(RafDefinition definition) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(RAF_ROOT + definition.getCode());

            Node node = session.getNode(fullPath);

            node.setProperty(PROP_TITLE, definition.getName());
            node.setProperty(PROP_DESCRIPTON, definition.getInfo());

            RafNode result = nodeToRafNode(node);

            session.save();
            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0002] Raf node cannot update", ex);
        }
    }

    /**
     * FIXME: findorCreateNode kullanamayız. Yetkisiz Raf oluşur.
     *
     * @param code
     * @return
     * @throws RafException
     */
    public RafNode getRafNode(String code) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(RAF_ROOT + code);

            Node node = jcrTools.findOrCreateNode(session, fullPath, NODE_FOLDER);

            RafNode result = nodeToRafNode(node);

            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0003] Raf node not found", ex);
        }
    }

    /**
     * FIXME: findorCreateNode kullanamayız. Yetkisiz Raf oluşur.
     *
     * @param username
     * @return
     * @throws RafException
     */
    public RafNode getPrivateRafNode(String username) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(PRIVATE_ROOT + username);

            Node node = jcrTools.findOrCreateNode(session, fullPath, NODE_FOLDER);

            node.addMixin(MIXIN_RAF);
            node.addMixin(MIXIN_TITLE);
            node.addMixin(MIXIN_TAGGABLE);

            node.setProperty(PROP_RAF_TYPE, RAF_TYPE_PRIVATE);
            //i18n
            node.setProperty(PROP_TITLE, "Kişisel");
            //node.setProperty(PROP_DESCRIPTON, definition.getInfo());

            RafNode result = nodeToRafNode(node);

            session.save();
            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0003] Raf node not found", ex);
        }

    }

    /**
     * FIXME: findorCreateNode kullanamayız. Yetkisiz Raf oluşur.
     *
     * @return
     * @throws RafException
     */
    public RafNode getSharedRafNode() throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(SHARED_ROOT);

            Node node = jcrTools.findOrCreateNode(session, fullPath, NODE_FOLDER);

            node.addMixin(MIXIN_RAF);
            node.addMixin(MIXIN_TITLE);
            node.addMixin(MIXIN_TAGGABLE);

            node.setProperty(PROP_RAF_TYPE, RAF_TYPE_SHARED);
            //i18n
            node.setProperty(PROP_TITLE, "Ortak");
            //node.setProperty(PROP_DESCRIPTON, definition.getInfo());

            RafNode result = nodeToRafNode(node);

            session.save();
            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0003] Raf node not found", ex);
        }
    }

    /**
     * FIXME: findorCreateNode kullanamayız. Yetkisiz Raf oluşur.
     *
     * @return
     * @throws RafException
     */
    public RafNode getProcessRafNode() throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(PROCESS_ROOT);

            Node node = jcrTools.findOrCreateNode(session, fullPath, NODE_FOLDER);

            node.addMixin(MIXIN_RAF);
            node.addMixin(MIXIN_TITLE);
            node.addMixin(MIXIN_TAGGABLE);

            node.setProperty(PROP_RAF_TYPE, RAF_TYPE_PROCESS);
            //i18n
            node.setProperty(PROP_TITLE, "Processes");
            //node.setProperty(PROP_DESCRIPTON, definition.getInfo());

            RafNode result = nodeToRafNode(node);

            session.save();
            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0003] Raf node not found", ex);
        }
    }

    public List<RafFolder> getFolderList(RafNode rafNode) throws RafException {
        return getFolderList(rafNode.getPath());
    }

    /**
     * @param rafCode
     * @return
     * @throws RafException
     */
    public RafFolder getFolder(String rafPath) throws RafException {

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = rafPath;//getEncodedPath(RAF_ROOT + rafCode);

            Node node = session.getNode(fullPath);

            //Root'u ekleyecek miyiz? Aslında bu bir RafNode ama aynı zamanda bir folder.
            //RootNode'un parentId'sini saklıyoruz. Ayrıca # ile UI tarafında ağaç da düzgün olacak.
            RafFolder f = nodeToRafFolder(node);
            f.setParentId("#");

            session.logout();

            return f;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0004] Raf Folders not found",ex);
        }

    }

    public List<RafFolder> getChildren(String rafPath) throws RafException {

        List<RafFolder> folderList = new ArrayList<RafFolder>();

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = rafPath;//getEncodedPath(RAF_ROOT + rafCode);

            Node node = session.getNode(fullPath);

            for (NodeIterator iter = node.getNodes(); iter.hasNext();) {
                Node child = iter.nextNode();
                RafFolder f = nodeToRafFolder(node);
                folderList.add(f);
            }
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0004] Raf Folders not found", ex);
        }

        return folderList;
    }

    /**
     * FIXME: findorCreateNode kullanamayız. Yetkisiz Raf oluşur.
     *
     * @param rafCode
     * @return
     * @throws RafException
     */
    public List<RafFolder> getFolderList(String rafPath) throws RafException {

        List<RafFolder> result = new ArrayList<>();

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = rafPath;//getEncodedPath(RAF_ROOT + rafCode);

            Node node = session.getNode(fullPath);

            //Root'u ekleyecek miyiz? Aslında bu bir RafNode ama aynı zamanda bir folder.
            //RootNode'un parentId'sini saklıyoruz. Ayrıca # ile UI tarafında ağaç da düzgün olacak.
            RafFolder f = nodeToRafFolder(node);
            f.setParentId("#");
            result.add(f);

            populateFolders(node, result);

            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0004] Raf Folders not found", ex);
        }

    }

    public RafCollection getCollection(String path) throws RafException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public RafCollection getCollectionById(String id) throws RafException {
        RafCollection result = new RafCollection();

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            Node node = session.getNodeByIdentifier(id);

            if (node == null) {
                throw new RafException("[RAF-0005] Raf node not found");
            }

            result.setId(node.getIdentifier());
            result.setMimeType(RafMimeTypes.RAF_FOLDER);
            result.setName(node.getName());
            result.setPath(node.getPath());
            //FIXME: Burada aslında raf'a denk geldiysel parentId null dönmeliyiz.
            result.setParentId(node.getParent().getIdentifier());

            result.setTitle(getPropertyAsString(node, PROP_TITLE));

            NodeIterator it = node.getNodes();
            while (it.hasNext()) {
                Node n = it.nextNode();

                //Node tipine göre doğru conversion.
                if (n.isNodeType(NODE_FOLDER)) {
                    //Recod Tipindekileri ayrıca toparlıyoruz.
                    if (n.isNodeType(MIXIN_RECORD)) {
                        result.getItems().add(nodeToRafRecord(n));
                    } else {
                        result.getItems().add(nodeToRafFolder(n));
                    }
                } else if (n.isNodeType(NODE_FILE)) {
                    result.getItems().add(nodeToRafDocument(n));
                }
            }

            jcrTools.printSubgraph(node);

            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0006] Raf collection not found", ex);
        }

    }

    public RafCollection getCategoryCollection(Long categoryId, String category, String categoryPath, String rootPath, boolean rescursive) throws RafException {
        RafCollection result = new RafCollection();
        result.setId(categoryId.toString());
        result.setMimeType("raf/categories");
        result.setTitle(category);
        result.setPath(categoryPath);
        result.setName(category);

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            QueryManager queryManager = session.getWorkspace().getQueryManager();

            //FIXME: recursive olduğunda = yerine like olacak, path sonuna % eklenecek
            String expression = "SELECT * FROM [" + MIXIN_TAGGABLE + "] WHERE [" + PROP_CATEGORY_PATH + "] = '" + categoryPath + "' AND  ISDESCENDANTNODE('" + rootPath + "')";

            Query query = queryManager.createQuery(expression, Query.JCR_SQL2);
            QueryResult queryResult = query.execute();

            NodeIterator it = queryResult.getNodes();
            while (it.hasNext()) {
                Node n = it.nextNode();

                //Node tipine göre doğru conversion.
                if (n.isNodeType(NODE_FOLDER)) {
                    if (n.isNodeType(MIXIN_RECORD)) {
                        result.getItems().add(nodeToRafRecord(n));
                    } else {
                        result.getItems().add(nodeToRafFolder(n));
                    }
                } else if (n.isNodeType(NODE_FILE)) {
                    result.getItems().add(nodeToRafDocument(n));
                }
            }

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0007] Raf Query Error", ex);
        }

        return result;
    }

    public RafCollection getTagCollection(String tag, String rootPath) throws RafException {
        RafCollection result = new RafCollection();
        result.setId(tag);
        result.setMimeType("raf/tags");
        result.setTitle(tag);
        result.setPath(tag);
        result.setName(tag);

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            QueryManager queryManager = session.getWorkspace().getQueryManager();

            //FIXME: recursive olduğunda = yerine like olacak, path sonuna % eklenecek
            String expression = "SELECT * FROM [" + MIXIN_TAGGABLE + "] WHERE [" + PROP_TAG + "] = '" + tag + "' AND  ISDESCENDANTNODE('" + rootPath + "')";

            Query query = queryManager.createQuery(expression, Query.JCR_SQL2);
            QueryResult queryResult = query.execute();

            NodeIterator it = queryResult.getNodes();
            while (it.hasNext()) {
                Node n = it.nextNode();

                //Node tipine göre doğru conversion.
                if (n.isNodeType(NODE_FOLDER)) {
                    if (n.isNodeType(MIXIN_RECORD)) {
                        result.getItems().add(nodeToRafRecord(n));
                    } else {
                        result.getItems().add(nodeToRafFolder(n));
                    }
                } else if (n.isNodeType(NODE_FILE)) {
                    result.getItems().add(nodeToRafDocument(n));
                }
            }

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0007] Raf Query Error", ex);
        }

        return result;
    }

    public RafCollection getSearchCollection(String searchText, String rootPath) throws RafException {
        RafCollection result = new RafCollection();
        result.setId("SEARCH");
        result.setMimeType("raf/search");
        result.setTitle(searchText);
        result.setPath("SEARCH");
        result.setName(searchText);

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            QueryManager queryManager = session.getWorkspace().getQueryManager();

            //FIXME: Burada search textin için temizlenmeli. Kuralları bozacak bişiler olmamalı
            String expression = "SELECT * FROM [" + NODE_SEARCH + "] as nodes WHERE CONTAINS(nodes.*, '" + searchText + "') AND  ISDESCENDANTNODE('" + rootPath + "')";

            Query query = queryManager.createQuery(expression, Query.JCR_SQL2);
            QueryResult queryResult = query.execute();

            NodeIterator it = queryResult.getNodes();
            while (it.hasNext()) {
                Node n = it.nextNode();

                Node sn = n;

                if (n.isNodeType("nt:resource")) {
                    sn = n.getParent();
                } else if (n.getName().endsWith(":metadata")) {
                    sn = n.getParent();
                }

                //Node tipine göre doğru conversion.
                if (sn.isNodeType(NODE_FOLDER)) {
                    if (sn.isNodeType(MIXIN_RECORD)) {
                        result.getItems().add(nodeToRafRecord(sn));
                    } else {
                        result.getItems().add(nodeToRafFolder(sn));
                    }
                } else if (sn.isNodeType(NODE_FILE)) {
                    result.getItems().add(nodeToRafDocument(sn));
                }
            }

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0007] Raf Query Error", ex);
        }

        return result;
    }

    /**
     * Yeni oluşturulan RafFolder geriye döner.
     *
     * Parametre olrak gönderilen RafFolder by example içindir.
     *
     * @param folder
     * @return
     * @throws RafException
     */
    public RafFolder createFolder(RafFolder folder) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(folder.getPath());

            Node node = jcrTools.findOrCreateNode(session, fullPath, NODE_FOLDER);

            node.addMixin(MIXIN_TITLE);
            node.addMixin(MIXIN_TAGGABLE);

            node.setProperty(PROP_TITLE, folder.getTitle());
            node.setProperty(PROP_DESCRIPTON, folder.getInfo());

            session.save();

            RafFolder result = nodeToRafFolder(node);

            session.logout();

            return result;

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0008] Raf Folder cannot create", ex);
        }
    }

    /**
     * Verilen path için folder oluşturur.
     *
     * Title, Description gibi alanlar doğal olarak doldurulmazlar.
     *
     * @param folderPath
     * @return
     * @throws RafException
     */
    public RafFolder createFolder(String folderPath) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(folderPath);

            Node node = jcrTools.findOrCreateNode(session, fullPath, NODE_FOLDER);

            node.addMixin(MIXIN_TITLE);
            node.addMixin(MIXIN_TAGGABLE);

            node.setProperty(PROP_TITLE, node.getName());
            //node.setProperty(PROP_DESCRIPTON, folder.getInfo());

            session.save();

            RafFolder result = nodeToRafFolder(node);
            session.logout();

            return result;

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0008] Raf Folder cannot create", ex);
        }
    }

    /**
     * Örnek veri ile dolu rafRecod objesinden JCR nodunu hazırlayıp gerçek
     * olanı geri döndürür.
     *
     * @param record
     * @return
     * @throws RafException
     */
    public RafRecord createRecord(RafRecord record) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(record.getPath() + "/" + record.getName());

            Node node = jcrTools.findOrCreateNode(session, fullPath, NODE_FOLDER);

            node.addMixin(MIXIN_TITLE);
            node.addMixin(MIXIN_TAGGABLE);
            node.addMixin(MIXIN_RECORD);
            node.addMixin(MIXIN_METADATA);

            node.setProperty(PROP_TITLE, record.getTitle());
            node.setProperty(PROP_DESCRIPTON, record.getInfo());
            node.setProperty(PROP_RECORD_TYPE, record.getRecordType());
            node.setProperty(PROP_DOCUMENT_TYPE, record.getDocumentType());

            node.setProperty(PROP_MAIN_DOCUMENT, record.getMainDocument());
            node.setProperty(PROP_PROCESS_ID, record.getProcessId());
            node.setProperty(PROP_PROCESS_INST_ID, record.getProcessIntanceId());
            node.setProperty(PROP_LOCATION, record.getLocation());
            node.setProperty(PROP_RECORD_NO, record.getRecordNo());

            session.save();

            RafRecord result = nodeToRafRecord(node);
            session.logout();

            return result;

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0009] Raf Record cannot create", ex);
        }
    }

    public RafObject checkout(String id) throws RafException {
        RafObject result = null;

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            Node node = session.getNodeByIdentifier(id);
            if (node.isNodeType(NODE_FOLDER) && !node.isNodeType(MIXIN_RECORD)) {
                //Folder'lar versionlanmaz! Dolayısı ile checkout edilmez
                throw new RafException("[RAF-00010] Folder node cannot checkout");
            }

            if (node.isNodeType(MIXIN_VERSIONABLE)) {

                VersionManager vm = session.getWorkspace().getVersionManager();
                vm.checkout(node.getPath());
                //FIXME: devamında ne olacak?
                session.save();
            }

            jcrTools.printSubgraph(node);

            return result;

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-00011] Raf Node cannot checkout", ex);
        }
    }

    public RafObject checkin(String path, InputStream in) throws RafException {
        RafObject result = null;

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            if (!session.nodeExists(path)) {
                //Olmayan bir dosya için chekin yapılamaz!
                throw new RafException("[RAF-00012] Raf Node not exist");
            }

            Node node = session.getNode(path);
            //TODO: Record'lar için sürüm desteği biraz sorunlu, bunu bir düşünelim.
            if (node.isNodeType(NODE_FOLDER) && !node.isNodeType(MIXIN_RECORD)) {
                //Folder'lar versionlanmaz! Dolayısı ile checkin yapılamaz!
                throw new RafException("[RAF-00013] Raf Folder cannot checked in");
            }

            VersionManager versionManager = session.getWorkspace().getVersionManager();
            Node content = node.getNode(NODE_CONTENT);
            if (!content.isNodeType(MIXIN_VERSIONABLE)) {
                //Eğer daha öncesinde version eklenmiş ise önce onu ekliyoruz!
                content.addMixin(MIXIN_VERSIONABLE);
                session.save();
                //Artık sürüm takibi yapılabilir!
                //Ve ilk sürümü de bir kenara alalım
                versionManager.checkin(content.getPath());

            }

            jcrTools.printSubgraph(node);

            //şimdi yeni belgeyi ekleyelim
            versionManager.checkout(content.getPath());
            node = jcrTools.uploadFile(session, path, in);
            session.save();
            versionManager.checkin(content.getPath());

            jcrTools.printSubgraph(node);

            result = nodeToRafDocument(node);

            return result;

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-00014] Raf Node cannot checkin", ex);
        } catch (IOException ex) {
            throw new RafException("[RAF-00015] Raf Node cannot checkin", ex);
        }
    }

    public List<RafVersion> getVersionHistory(RafDocument object) throws RafException {
        try {
            
            List<RafVersion> result = new ArrayList<>();
            
            Session session = ModeShapeRepositoryFactory.getSession();

            Node node = session.getNodeByIdentifier(object.getId());
            

            if (node == null) {
                throw new RafException("[RAF-0005] Raf node not found");
            }
            
            if( !node.isNodeType(NODE_FILE)){
                throw new RafException("[RAF-0029] Not a document type");
            }
            
            Node content = node.getNode(NODE_CONTENT);
            if( content.isNodeType(MIXIN_VERSIONABLE)){
                VersionManager versionManager = session.getWorkspace().getVersionManager();
                VersionHistory vh = versionManager.getVersionHistory(content.getPath());
                VersionIterator vit = vh.getAllVersions();
                while( vit.hasNext() ){
                    Version v = vit.nextVersion();
                    RafVersion rv = new RafVersion();
                    rv.setId(v.getIdentifier());
                    rv.setName(v.getName());
                    rv.setCreatedBy(getPropertyAsString(v.getFrozenNode(), "jcr:lastModifiedBy"));
                    rv.setCreated(v.getProperty("jcr:created").getDate().getTime());
                    rv.setPath(v.getPath());
                    
                    //FIXME: version comment için alan eklendiğinde oda RafVersion'a alınacak
                    
                    result.add(rv);
                }
            }

            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0020] Raf Object not found", ex);
        }
    }

    public RafDocument uploadDocument(String fileName, InputStream in) throws RafException {
        if (Strings.isNullOrEmpty(fileName)) {
            throw new RafException("[RAF-00016] Filename cannot be null");
        }

        RafDocument result = null;

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullName = getEncodedPath(fileName);
            LOG.debug("Encoded FileName : {}", fileName);

            Node n = jcrTools.uploadFile(session, fullName, in);

            n.addMixin(MIXIN_TITLE);
            n.addMixin(MIXIN_TAGGABLE);
            n.addMixin(MIXIN_METADATA);

            String[] fa = fileName.split("/");
            n.setProperty(PROP_TITLE, fa[fa.length - 1]);

            session.save();

            //FIXME: Bazı durumlarda upload sırasında mimeType bulunamıyor. Bu durumda null gelmesi yerine "raf/binary atadık. Buna daha iyi bir çözüm lazım.
            Node nc = n.getNode(NODE_CONTENT);
            String mimeType = getPropertyAsString(nc, "jcr:mimeType");
            if (Strings.isNullOrEmpty(mimeType)) {
                nc.setProperty("jcr:mimeType", "raf/binary");
            }

            //Normalde mimeType application/xml geliyor BPMN için bunu değiştiriyoruz.
            //TODO: MimeType dedection için aslında daha düzgün bir şey gerek. 
            if (fileName.endsWith(".bpmn") || fileName.endsWith(".bpmn2")) {
                nc.setProperty("jcr:mimeType", "application/bpmn-xml");
            }

            session.save();

            result = nodeToRafDocument(n);
            session.logout();
            LOG.debug("Dosya JCR'e kondu : {}", fullName);
        } catch (RepositoryException ex) {
            LOG.error("Reporsitory Exception", ex);
            throw new RafException("[RAF-00017] File cannot uploaded", ex);
        } catch (IOException ex) {
            LOG.error("IO Exception", ex);
            throw new RafException("[RAF-00018] File cannot uploaded", ex);
        }
        return result;
    }

    public RafObject getRafObject(String id) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            Node node = session.getNodeByIdentifier(id);

            RafObject result = null;

            if (node == null) {
                //FIXME: bu exception nedir söylemek lazım.
                throw new RafException("[RAF-0005] Raf node not found");
            }

            jcrTools.printSubgraph(node);

            if (node.isNodeType(NODE_FOLDER)) {
                if (node.isNodeType(MIXIN_RECORD)) {
                    result = nodeToRafRecord(node);
                } else {
                    result = nodeToRafFolder(node);
                }
            } else if (node.isNodeType(NODE_FILE)) {
                result = nodeToRafDocument(node);
            } else {
                //FIXME:Bilinen bir node bulunamadı.
                throw new RafException("[RAF-0019] Unknow Node Type");
            }

            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0020] Raf Object not found", ex);
        }
    }

    public RafObject getRafObjectByPath(String path) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            Node node = session.getNode(path);

            RafObject result = null;

            if (node == null) {
                //FIXME: bu exception nedir söylemek lazım.
                throw new RafException("[RAF-0005] Raf node not found");
            }

            jcrTools.printSubgraph(node);

            if (node.isNodeType(NODE_FOLDER)) {
                if (node.isNodeType(MIXIN_RECORD)) {
                    result = nodeToRafRecord(node);
                } else {
                    result = nodeToRafFolder(node);
                }
            } else if (node.isNodeType(NODE_FILE)) {
                result = nodeToRafDocument(node);
            } else {
                //FIXME:Bilinen bir node bulunamadı.
                throw new RafException("[RAF-0019] Unknow Node Type");
            }

            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0020] Raf Object not found", ex);
        }
    }

    /**
     * RafObject üzerinde bulunan title, info, category, tags gibi alanları
     * günceller.
     *
     * @param object
     * @throws RafException
     */
    public void saveProperties(RafObject object) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            Node node = session.getNodeByIdentifier(object.getId());

            if (node == null) {
                throw new RafException("[RAF-0005] Raf node not found");
            }

            //Gerekli mixinler yoksa ekleyelim. Aslında bu kontrol ne kadar gerekli bilemedim.
            if (!node.isNodeType(MIXIN_TITLE)) {
                node.addMixin(MIXIN_TITLE);
            }

            if (!node.isNodeType(MIXIN_TAGGABLE)) {
                node.addMixin(MIXIN_TAGGABLE);
            }

            node.setProperty(PROP_TITLE, object.getTitle());
            node.setProperty(PROP_DESCRIPTON, object.getInfo());

            node.setProperty(PROP_CATEGORY, object.getCategory());
            node.setProperty(PROP_CATEGORY_PATH, object.getCategoryPath());
            //Eğer id null gelir ise primitive çevrimi hata veriyor
            node.setProperty(PROP_CATEGORY_ID, object.getCategoryId() == null ? 0 : object.getCategoryId());
            node.setProperty(PROP_TAG, object.getTags().toArray(new String[]{}));
            //node.setProperty(PROP_TAG, "");

            session.save();

            //FIXME: Buradan geriye RafObject'in yeni halini dönmek lazım ki UI düzgün render edilebilsin!
            session.logout();

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0021] Raf properties cannot saved", ex);
        }
    }

    /**
     * RafRecord nesnesinin üzerinde bulunan property ve metadata'ları kaydeder.
     *
     * @param object
     * @throws RafException
     */
    public void saveRecord(RafRecord object) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            Node node = session.getNodeByIdentifier(object.getId());

            if (node == null) {
                //FIXME: bu exception nedir söylemek lazım.
                throw new RafException("[RAF-0005] Raf node not found");
            }

            //Gerekli mixinler yoksa ekleyelim. Aslında bu kontrol ne kadar gerekli bilemedim.
            if (!node.isNodeType(MIXIN_TITLE)) {
                node.addMixin(MIXIN_TITLE);
            }

            if (!node.isNodeType(MIXIN_TAGGABLE)) {
                node.addMixin(MIXIN_TAGGABLE);
            }

            node.setProperty(PROP_TITLE, object.getTitle());
            node.setProperty(PROP_DESCRIPTON, object.getInfo());

            node.setProperty(PROP_CATEGORY, object.getCategory());
            node.setProperty(PROP_CATEGORY_PATH, object.getCategoryPath());
            //Eğer id null gelir ise primitive çevrimi hata veriyor
            node.setProperty(PROP_CATEGORY_ID, object.getCategoryId() == null ? 0 : object.getCategoryId());
            node.setProperty(PROP_TAG, object.getTags().toArray(new String[]{}));
            //node.setProperty(PROP_TAG, "");

            node.setProperty(PROP_RECORD_TYPE, object.getRecordType());
            node.setProperty(PROP_DOCUMENT_TYPE, object.getDocumentType());

            node.setProperty(PROP_MAIN_DOCUMENT, object.getMainDocument());
            node.setProperty(PROP_PROCESS_ID, object.getProcessId());
            node.setProperty(PROP_PROCESS_INST_ID, object.getProcessIntanceId());
            node.setProperty(PROP_LOCATION, object.getLocation());
            node.setProperty(PROP_RECORD_NO, object.getRecordNo());
            node.setProperty(PROP_STATUS, object.getStatus());

            for (RafMetadata m : object.getMetadatas()) {
                saveMetadata(object.getId(), m, session);
            }

            session.save();
            session.logout();

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0022] Raf Record cannot saved", ex);
        }
    }

    public void saveMetadata(String id, RafMetadata metadata) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            saveMetadata(id, metadata, session);

            session.logout();
            session.save();
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0023] Raf Metadata cannot saved", ex);
        }
    }

    /**
     * Asıl metada saklama işini yapar.
     *
     * Save ve logout işlemleri çağıran yerin sorumluluğunda
     *
     * @param id
     * @param metadata
     * @param session
     * @throws RafException
     */
    protected void saveMetadata(String id, RafMetadata metadata, Session session) throws RafException {

        try {
            Node metaNode = null;

            //Demek ki ilk kez yazılacak.
            if (Strings.isNullOrEmpty(metadata.getNodeId())) {
                Node node = session.getNodeByIdentifier(id);

                if (node == null) {
                    //FIXME: bu exception nedir söylemek lazım.
                    throw new RafException("[RAF-0005] Raf node not found");
                }

                metaNode = node.addNode(metadata.getType(), metadata.getType());
            } else {
                //MetadataNode var update için kendisini bulalım
                metaNode = session.getNodeByIdentifier(metadata.getNodeId());

                if (metaNode == null) {
                    //FIXME: bu exception nedir söylemek lazım.
                    throw new RafException("[RAF-0005] Raf node not found");
                }
            }

            MetadataConverter converter = MetadataConverterRegistery.getConverter(metadata.getType());

            converter.modelToNode(metadata, metaNode);

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0023] Raf Metadata cannot saved", ex);
        }
    }

    public InputStream getDocumentContent(String id) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            Node node = session.getNodeByIdentifier(id);

            LOG.debug("Document Content Requested: {}", node.getPath());

            Node content = node.getNode(NODE_CONTENT);

            //FIXME: Burada böyle bi rtakla gerçekten lazım mı? Bütün veriyi memory'e okumak dert olcaktır...
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(content.getProperty(PROP_DATA).getBinary().getStream(), bos);

            session.logout();

            ByteArrayInputStream result = new ByteArrayInputStream(bos.toByteArray());

            return result;

        } catch (RepositoryException | IOException ex) {
            LOG.error("RAfException", ex);
            throw new RafException("[RAF-0024] Raf Node content cannot found", ex);
        }
    }
    
    
    public InputStream getPreviewContent(String id) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            Node node = session.getNodeByIdentifier(id);

            LOG.debug("Document Preview Content Requested: {}", node.getPath());

            if( !node.hasNode("raf:preview")){
                throw new RafException("[RAF-0035] Raf Node preview cannot found");
            }
            
            Node content = node.getNode("raf:preview");

            //FIXME: Burada böyle bi rtakla gerçekten lazım mı? Bütün veriyi memory'e okumak dert olcaktır...
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(content.getProperty("jcr:data").getBinary().getStream(), bos);

            session.logout();

            ByteArrayInputStream result = new ByteArrayInputStream(bos.toByteArray());

            return result;

        } catch (RepositoryException | IOException ex) {
            LOG.error("RAfException", ex);
            throw new RafException("[RAF-0024] Raf Node content cannot found", ex);
        }
    }

    /**
     * ID'si verilen nodu siler.
     *
     * @param id
     */
    public void deleteObject(String id) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            Node node = session.getNodeByIdentifier(id);

            //Burada tarihçe silmek gerek. Aksi halde bütün dosyalar sistem de kalacak
            //TODO: bu aslında bir sistem ayarı olabilir. Bazı müşteriler gerçekten hiç bir şey silmek istemezler!
            deleteVersionHistory(node);

            jcrTools.removeAllChildren(node);
            node.remove();

            session.save();
            session.logout();

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0025] Raf Node cannot delete", ex);
        }

    }

    /**
     * Node ağacı üzerinde yürüyerek eğer varsa bütün version history'i siler.
     *
     * @param node
     * @throws RepositoryException
     */
    private void deleteVersionHistory(Node node) throws RepositoryException {
        if (node.isNodeType(MIXIN_VERSIONABLE)) {
            org.modeshape.jcr.api.version.VersionManager vm = (org.modeshape.jcr.api.version.VersionManager) node.getSession().getWorkspace().getVersionManager();
            vm.remove(node.getPath());
        }

        NodeIterator it = node.getNodes();
        while (it.hasNext()) {
            Node n = it.nextNode();
            deleteVersionHistory(n);
        }
    }

    public void copyObject(RafObject from, RafFolder to) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            if (session.hasPendingChanges()) {
                LOG.warn("Saklanmamış değişlikler var");
                session.save();
            }

            //FIXME: burada hedef ismin olup olmadığı kontrol edilecek. Varsa isimde (1) gibi ekler yapılacak.
            //FIXME: url encoding
            copy(session.getWorkspace(), from.getPath(), targetPath(session, from, to.getPath()));

            session.save();
            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0026] Raf Node cannot copied", ex);
        }
    }

    public void copyObject(RafObject from, RafRecord to) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            //FIXME: burada hedef ismin olup olmadığı kontrol edilecek. Varsa isimde (1) gibi ekler yapılacak.
            //FIXME: url encoding
            copy(session.getWorkspace(), from.getPath(), targetPath(session, from, to.getPath()));

            session.save();
            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0026] Raf Node cannot copied", ex);
        }
    }

    public void copyObject(List<RafObject> from, RafFolder to) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            //FIXME: burada hedef ismin olup olmadığı kontrol edilecek. Varsa isimde (1) gibi ekler yapılacak.
            //FIXME: url encoding
            for (RafObject o : from) {
                copy(session.getWorkspace(), o.getPath(), targetPath(session, o, to.getPath()));
            }

            session.save();
            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0026] Raf Node cannot copied", ex);
        }
    }

    public void copyObject(List<RafObject> from, RafRecord to) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            //FIXME: burada hedef ismin olup olmadığı kontrol edilecek. Varsa isimde (1) gibi ekler yapılacak.
            //FIXME: url encoding
            for (RafObject o : from) {
                copy(session.getWorkspace(), o.getPath(), targetPath(session, o, to.getPath()));
            }

            session.save();
            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0026] Raf Node cannot copied", ex);
        }
    }

    public void moveObject(RafObject from, RafFolder to) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            //FIXME: burada hedef ismin olup olmadığı kontrol edilecek. Varsa isimde (1) gibi ekler yapılacak.
            //FIXME: url encoding
            move(session.getWorkspace(), from.getPath(), targetPath(session, from, to.getPath()));

            session.save();
            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0026] Raf Node cannot copied", ex);
        }
    }

    public void moveObject(List<RafObject> from, RafFolder to) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            //FIXME: burada hedef ismin olup olmadığı kontrol edilecek. Varsa isimde (1) gibi ekler yapılacak.
            //FIXME: url encoding
            for (RafObject o : from) {
                move(session.getWorkspace(), o.getPath(), targetPath(session, o, to.getPath()));
            }

            session.save();
            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0026] Raf Node cannot copied", ex);
        }
    }

    protected String targetPath(Session session, RafObject o, String targetBase) throws RepositoryException, RafException {
        //jcrTools.findOrCreateNode(session, PROP_TAG)
        //Önce folder var mı bakalım yoksa yoksa zaten exception ile çıkacağız.
        Node folderNode = session.getNode(targetBase);

        //Node tipi folder değil ise hata verelim. Bunun üzerine kopyalama yapamayız
        if (!folderNode.isNodeType(NODE_FOLDER)) {
            //FIXME: doğru bir hata üretelim.
            throw new RafException("[RAF-0027] Raf Node type not folder");
        }

        String result = targetBase + "/" + o.getName();

        if (!session.itemExists(result)) {
            return result;
        }

        //Demekki hedef var. Dolayısı ile ismini değiştirmek lazım. 
        result = targetBase + "/" + o.getName() + "(" + folderNode.getNodes(o.getName() + "*").getSize() + ")";

        return result;
    }

    /**
     * Asıl copy işlemi
     *
     * @param workspace
     * @param fromPath
     * @param toPath
     * @throws RepositoryException
     */
    protected void copy(Workspace workspace, String fromPath, String toPath) throws RepositoryException {
        workspace.copy(fromPath, toPath);

        //Copy sonrasında sürümlü dosyalar checkedout kalıyor. Checkedin yapalım.
        Node n = workspace.getSession().getNode(toPath);
        if (n.isNodeType(NODE_FILE)) {
            checkinCopiedNode(n);
        } else if (n.isNodeType(NODE_FOLDER)) {
            chekinCopiedFolderNodes(n);
        }

    }

    /**
     * Folder ağacı üzerinde yürüyerek nt:file tipindekileri tespit ederek
     * checkin kontrolüne gönderir.
     *
     * @param n
     * @throws RepositoryException
     */
    private void chekinCopiedFolderNodes(Node n) throws RepositoryException {
        NodeIterator it = n.getNodes();
        while (it.hasNext()) {
            Node nn = it.nextNode();
            if (nn.isNodeType(NODE_FILE)) {
                checkinCopiedNode(nn);
            } else if (nn.isNodeType(NODE_FOLDER)) {
                chekinCopiedFolderNodes(nn);
            }
        }
    }

    /**
     * nt:file tipinde olan nodelar için jcr:content'in mix:versionable olmasına
     * bakar.
     *
     * Gönderilen node da doğrudan checkin alınabilir.
     *
     * @param n
     * @throws RepositoryException
     */
    private void checkinCopiedNode(Node n) throws RepositoryException {
        Node cn = null;

        //Önce doğru tipi bir bulalım. File ise bir alt node olsa gerek
        if (n.isNodeType(NODE_FILE)) {
            Node c = n.getNode(NODE_CONTENT);
            if (c.isNodeType(MIXIN_VERSIONABLE)) {
                cn = c;

            }
        } else if (n.isNodeType(MIXIN_VERSIONABLE)) {
            //Nodun kendisi imiş
            cn = n;
        }

        if (cn != null) {
            VersionManager vm = cn.getSession().getWorkspace().getVersionManager();
            if (vm.isCheckedOut(cn.getPath())) {
                vm.checkin(cn.getPath());
            }
        }
    }

    /**
     * Asıl move işlemi
     *
     * @param workspace
     * @param fromPath
     * @param toPath
     * @throws RepositoryException
     */
    protected void move(Workspace workspace, String fromPath, String toPath) throws RepositoryException {
        workspace.move(fromPath, toPath);
    }

    //////////////////////////////////////////
    //Util Functions
    /**
     * Türkçe ya da path'de kabul edilmeyecek karakterler temizleniyor
     *
     * @param path
     * @return
     */
    protected String getEncodedPath(String path) {
        return encoder.encode(path);
    }

    /**
     * Orjinal haline geri çevriliyor
     *
     * @param path
     * @return
     */
    protected String getDecodedPath(String path) {
        return encoder.decode(path);
    }

    protected RafNode nodeToRafNode(Node node) throws RepositoryException {
        RafNode result = new RafNode();

        result.setId(node.getIdentifier());
        result.setPath(node.getPath());
        result.setName(node.getName());

        return result;
    }

    protected RafFolder nodeToRafFolder(Node node) throws RepositoryException {
        RafFolder result = new RafFolder();

        result.setId(node.getIdentifier());
        result.setPath(node.getPath());
        result.setName(node.getName());
        result.setParentId(node.getParent().getIdentifier());

        result.setCreateBy(node.getProperty("jcr:createdBy").getString());
        result.setCreateDate(node.getProperty("jcr:created").getDate().getTime());

        if (node.isNodeType(MIXIN_TITLE)) {
            result.setTitle(getPropertyAsString(node, PROP_TITLE));
            result.setInfo(getPropertyAsString(node, PROP_DESCRIPTON));
        }
        
        if( Strings.isNullOrEmpty(result.getTitle())){
            result.setTitle(result.getName());
        }

        if (node.isNodeType(MIXIN_TAGGABLE)) {
            //result.setInfo(node.getProperty("raf:tags").getString());
            result.setCategory(getPropertyAsString(node, PROP_CATEGORY));
            result.setCategoryPath(getPropertyAsString(node, PROP_CATEGORY_PATH));
            result.setCategoryId(getPropertyAsLong(node, PROP_CATEGORY_ID));
            result.setTags(getPropertyAsStringList(node, PROP_TAG));
        }

        return result;
    }

    protected RafRecord nodeToRafRecord(Node node) throws RepositoryException, RafException {
        RafRecord result = new RafRecord();

        result.setId(node.getIdentifier());
        result.setPath(node.getPath());
        result.setName(node.getName());
        result.setParentId(node.getParent().getIdentifier());

        result.setCreateBy(node.getProperty("jcr:createdBy").getString());
        result.setCreateDate(node.getProperty("jcr:created").getDate().getTime());

        if (node.isNodeType(MIXIN_TITLE)) {
            result.setTitle(getPropertyAsString(node, PROP_TITLE));
            result.setInfo(getPropertyAsString(node, PROP_DESCRIPTON));
        }

        if( Strings.isNullOrEmpty(result.getTitle())){
            result.setTitle(result.getName());
        }
        
        if (node.isNodeType(MIXIN_TAGGABLE)) {
            //result.setInfo(node.getProperty("raf:tags").getString());
            result.setCategory(getPropertyAsString(node, PROP_CATEGORY));
            result.setCategoryPath(getPropertyAsString(node, PROP_CATEGORY_PATH));
            result.setCategoryId(getPropertyAsLong(node, PROP_CATEGORY_ID));
            result.setTags(getPropertyAsStringList(node, PROP_TAG));
        }

        if (node.isNodeType(MIXIN_RECORD)) {
            result.setRecordType(getPropertyAsString(node, PROP_RECORD_TYPE));
            result.setDocumentType(getPropertyAsString(node, PROP_DOCUMENT_TYPE));
            result.setMainDocument(getPropertyAsString(node, PROP_MAIN_DOCUMENT));
            result.setLocation(getPropertyAsString(node, PROP_LOCATION));
            result.setProcessId(getPropertyAsString(node, PROP_PROCESS_ID));
            result.setProcessIntanceId(getPropertyAsLong(node, PROP_PROCESS_INST_ID));
            result.setStatus(getPropertyAsString(node, PROP_STATUS));
            result.setRecordNo(getPropertyAsString(node, PROP_RECORD_NO));
            result.setElectronicDocument(getPropertyAsBoolean(node, PROP_ELECTRONIC_DOCUMENT));
        }

        //child nodelar üzerinden dolaşıp metadata ve file bilgilerini topluyoruz.
        NodeIterator it = node.getNodes();
        while (it.hasNext()) {
            Node mn = it.nextNode();
            if (mn.isNodeType("nt:file")) {
                result.getDocuments().add(nodeToRafDocument(mn));
            } else if (mn.getName().endsWith(":metadata")) {
                MetadataConverter mc = MetadataConverterRegistery.getConverter(mn.getPrimaryNodeType().getName());
                result.getMetadatas().add(mc.nodeToModel(mn));
            }
        }

        /*
        //RafRecord için metadata'lar
        NodeIterator it = node.getNodes("*:metadata");
        while (it.hasNext()) {
            Node mn = it.nextNode();
            MetadataConverter mc = MetadataConverterRegistery.getConverter(mn.getPrimaryNodeType().getName());
            result.getMetadatas().add(mc.nodeToModel(mn));
        }
        
        //RafRecord'a ekli RefDocument listesi
        it = node.getNodes("nt:file");
        while (it.hasNext()) {
            Node mn = it.nextNode();
            result.getDocuments().add(nodeToRafDocument(mn));
        }
         */
        return result;
    }

    protected RafDocument nodeToRafDocument(Node node) throws RepositoryException, RafException {
        RafDocument result = new RafDocument();

        result.setId(node.getIdentifier());
        result.setPath(node.getPath());
        result.setName(node.getName());
        result.setParentId(node.getParent().getIdentifier());

        //bilenen diğer metadata ( createDate v.b. ) toplanmalı
        result.setCreateBy(node.getProperty("jcr:createdBy").getString());
        result.setCreateDate(node.getProperty("jcr:created").getDate().getTime());

        //FIXME: TIKA olmadığı için mimeType bulmada sorun olabilir.
        Node cn = node.getNode(NODE_CONTENT);
        String s = getPropertyAsString(cn, "jcr:mimeType");
        if (Strings.isNullOrEmpty(s)) {
            s = "raf/binary";
        }
        result.setMimeType(s);

        result.setUpdateBy(cn.getProperty("jcr:lastModifiedBy").getString());
        result.setUpdateDate(cn.getProperty("jcr:lastModified").getDate().getTime());

        if (node.isNodeType(MIXIN_TITLE)) {
            result.setTitle(getPropertyAsString(node, PROP_TITLE));
            result.setInfo(getPropertyAsString(node, PROP_DESCRIPTON));
        }

        if( Strings.isNullOrEmpty(result.getTitle())){
            result.setTitle(result.getName());
        }
        
        if (node.isNodeType(MIXIN_TAGGABLE)) {
            //result.setInfo(node.getProperty("raf:tags").getString());
            result.setCategory(getPropertyAsString(node, PROP_CATEGORY));
            result.setCategoryPath(getPropertyAsString(node, PROP_CATEGORY_PATH));
            result.setCategoryId(getPropertyAsLong(node, PROP_CATEGORY_ID));
            result.setTags(getPropertyAsStringList(node, PROP_TAG));
        }

        Node content = node.getNode(NODE_CONTENT);
        if (content.isNodeType(MIXIN_VERSIONABLE)) {
            result.setVersionable(Boolean.TRUE);
            VersionManager versionManager = node.getSession().getWorkspace().getVersionManager();
            result.setCheckedout(versionManager.isCheckedOut(content.getPath()));
            Version version = versionManager.getBaseVersion(content.getPath());
            result.setVersion(version.getName());
            jcrTools.printSubgraph(version);
        }
        
        result.setLength(content.getProperty( PROP_DATA ).getLength());
        

        NodeIterator it = node.getNodes("*:metadata");
        while (it.hasNext()) {
            Node mn = it.nextNode();
            MetadataConverter mc = MetadataConverterRegistery.getConverter(mn.getPrimaryNodeType().getName());
            result.getMetadatas().add(mc.nodeToModel(mn));
        }

        //raf:preview var ise onun bilgilerini alalım.
        if(node.hasNode("raf:preview")){
            result.setHasPreview(Boolean.TRUE);
            Node preview = node.getNode("raf:preview");
            result.setPreviewMimeType(getPropertyAsString(preview, "jcr:mimeType"));
        }
        
        return result;
    }

    private void populateFolders(Node node, List<RafFolder> result) throws RepositoryException {
        NodeIterator it = node.getNodes();
        while (it.hasNext()) {
            Node n = it.nextNode();
            //MIXIN_RECORD tipinde olanlar folder değildir!
            if (n.isNodeType(NODE_FOLDER) && !n.isNodeType(MIXIN_RECORD)) {
                result.add(nodeToRafFolder(n));
                populateFolders(n, result);
            }
        }

    }

    /**
     * Verilen property key sonucunun null kontrolü yaparak geriye değer
     * döndürür.
     *
     * @param node
     * @param prop
     * @return
     * @throws RepositoryException
     */
    private String getPropertyAsString(Node node, String prop) throws RepositoryException {

        try {
            Property property = node.getProperty(prop);

            if (property != null) {
                return property.getString();
            }
        } catch (PathNotFoundException ex) {
            //Aslında yapacak bişi yok. Attribute olmayabilir o zaman geriye null döneceğiz.
            LOG.debug("Property not found : {}", prop);
        }

        return null;
    }

    /**
     * Verilen property key sonucunun null kontrolü yaparak geriye değer
     * döndürür.
     *
     * @param node
     * @param prop
     * @return
     * @throws RepositoryException
     */
    private Long getPropertyAsLong(Node node, String prop) throws RepositoryException {

        try {
            Property property = node.getProperty(prop);

            if (property != null) {
                return property.getLong();
            }
        } catch (PathNotFoundException ex) {
            //Aslında yapacak bişi yok. Attribute olmayabilir o zaman geriye null döneceğiz.
            LOG.debug("Property not found : {}", prop);
        }

        return null;
    }

    /**
     * Verilen property key sonucunun null kontrolü yaparak geriye değer
     * döndürür.
     *
     * @param node
     * @param prop
     * @return
     * @throws RepositoryException
     */
    private Boolean getPropertyAsBoolean(Node node, String prop) throws RepositoryException {

        try {
            Property property = node.getProperty(prop);

            if (property != null) {
                return property.getBoolean();
            }
        } catch (PathNotFoundException ex) {
            //Aslında yapacak bişi yok. Attribute olmayabilir o zaman geriye null döneceğiz.
            LOG.debug("Property not found : {}", prop);
        }

        return null;
    }

    /**
     * Verilen property key sonucunun null kontrolü yaparak geriye değer
     * döndürür.
     *
     * @param node
     * @param prop
     * @return
     * @throws RepositoryException
     */
    private List<String> getPropertyAsStringList(Node node, String prop) throws RepositoryException {

        List<String> result = new ArrayList<>();

        try {
            Property property = node.getProperty(prop);

            if (property != null) {
                Value[] vls = property.getValues();
                for (Value v : vls) {
                    result.add(v.getString());
                }
            }
        } catch (PathNotFoundException ex) {
            //Aslında yapacak bişi yok. Attribute olmayabilir o zaman geriye null döneceğiz.
            LOG.debug("Property not found : {}", prop);
        }

        return result;
    }

}
