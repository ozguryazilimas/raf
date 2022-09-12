package com.ozguryazilim.raf.jcr;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.MetadataConverter;
import com.ozguryazilim.raf.MetadataConverterRegistery;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.SequencerConfig;
import com.ozguryazilim.raf.SequencerRegistery;
import com.ozguryazilim.raf.converter.PdfConverter;
import com.ozguryazilim.raf.encoder.RafEncoder;
import com.ozguryazilim.raf.encoder.RafEncoderFactory;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.enums.SortType;
import com.ozguryazilim.raf.models.DetailedSearchModel;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.models.RafMimeTypes;
import com.ozguryazilim.raf.models.RafNode;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.models.RafVersion;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jodconverter.core.office.OfficeException;
import org.modeshape.jcr.api.JcrTools;
import org.modeshape.jcr.sequencer.InvalidSequencerPathExpression;
import org.modeshape.jcr.sequencer.SequencerPathExpression;
import org.modeshape.jcr.value.BinaryValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.imageio.ImageIO;
import javax.jcr.AccessDeniedException;
import javax.jcr.Binary;
import javax.jcr.ItemNotFoundException;
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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    private static final String NODE_PREVIEW = "raf:preview";
    private static final String NODE_THUMBNAIL = "raf:thumbnail";


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
    private static final String PROP_PATH = "jcr:path";
    private static final String PROP_MIMETYPE = "jcr:mimeType";

    private static final String PROP_RECORD_TYPE = "raf:recordType";
    private static final String PROP_DOCUMENT_TYPE = "raf:documentType";
    private static final String PROP_MAIN_DOCUMENT = "raf:mainDocument";
    private static final String PROP_ELECTRONIC_DOCUMENT = "raf:electronicDocument";
    private static final String PROP_LOCATION = "raf:location";
    private static final String PROP_PROCESS_ID = "raf:processId";
    private static final String PROP_PROCESS_INST_ID = "raf:processIntanceId";
    private static final String PROP_STATUS = "raf:status";
    private static final String PROP_RECORD_NO = "raf:recordNo";

    private static final String MIXIN_RAFCHECKIN = "raf:checkin";
    private static final String PROP_RAF_CHECKIN_DATE = "raf:checkInDate";
    private static final String PROP_RAF_CHECKIN_USER = "raf:checkInUser";
    private static final String PROP_RAF_CHECKIN_STATE = "raf:checkInState";
    private static final String PROP_RAF_CHECKIN_PREVIOUS_VERSION = "raf:previousVersion";

    private static final String MIXIN_RAF_VERSIONING = "raf:versioning";
    private static final String PROP_RAF_VERSION_COMMENT = "raf:comment";

    private static final String RAF_TYPE_DEFAULT = "DEFAULT";
    private static final String RAF_TYPE_PRIVATE = "PRIVATE";
    private static final String RAF_TYPE_SHARED = "SHARED";
    private static final String RAF_TYPE_PROCESS = "PROCESS";

    private static final String PROP_CREATED_DATE = "jcr:created";
    private static final String PROP_CREATED_BY = "jcr:createdBy";

    private static final String PROP_UPDATED_DATE = "jcr:lastModified";
    private static final String PROP_UPDATED_BY = "jcr:lastModifiedBy";

    private RafEncoder fileNameEncoder;
    private RafEncoder rafNameEncoder;
    private RafEncoder dirNameEncoder;
    private Boolean debugMode = Boolean.FALSE;

    JcrTools jcrTools = new JcrTools();

    @PostConstruct
    public void init() {
        try {
            start();
            String debugModeProp = ConfigResolver.getProjectStageAwarePropertyValue("raf.repository.debug", "false");
            debugMode = "true".equals(debugModeProp);
            jcrTools.setDebug(this.debugMode);
        } catch (RafException ex) {
            LOG.error("ModeShape cannot started", ex);
        }
    }

    public void start() throws RafException {
        try {
            fileNameEncoder = RafEncoderFactory.getFileNameEncoder();
            rafNameEncoder = RafEncoderFactory.getRafNameEncoder();
            dirNameEncoder = RafEncoderFactory.getDirNameEncoder();

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

    public NodeIterator getAllNodesWithNodeType(String nodeType) throws RafException {
        NodeIterator result;
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            QueryManager queryManager = session.getWorkspace().getQueryManager();

            String expression = String.format("SELECT * FROM [%s] as nodes", nodeType);

            Query query = queryManager.createQuery(expression, Query.JCR_SQL2);
            QueryResult queryResult = query.execute();
            LOG.debug("Query executed for  {}", expression);
            result = queryResult.getNodes();

        }
        catch (RepositoryException ex) {
            throw new RafException("[RAF-0007] Raf Query Error", ex);
        }
        return result;
    }

    public RafNode createRafNode(RafDefinition definition) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedName(RAF_ROOT + definition.getCode());

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

            String fullPath = getEncodedName(RAF_ROOT + definition.getCode());

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

            String fullPath = getEncodedName(RAF_ROOT + code);

            Node node = jcrTools.findOrCreateNode(session, fullPath, NODE_FOLDER);

            RafNode result = nodeToRafNode(node);

            session.logout();

            return result;
        } catch (AccessDeniedException ex) {
            throw new RafException("[RAF-0042] Permission Denied: ", ex);
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

            String fullPath = getEncodedName(PRIVATE_ROOT + username);

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

            String fullPath = getEncodedName(SHARED_ROOT);

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

            String fullPath = getEncodedName(PROCESS_ROOT);

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
     * @param rafPath
     * @return
     * @throws RafException
     */
    public RafFolder getFolder(String rafPath) throws RafException {

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = rafPath;

            Node node = session.getNode(fullPath);

            //Root'u ekleyecek miyiz? Aslında bu bir RafNode ama aynı zamanda bir folder.
            //RootNode'un parentId'sini saklıyoruz. Ayrıca # ile UI tarafında ağaç da düzgün olacak.
            RafFolder f = nodeToRafFolder(node);
            f.setParentId("#");

            session.logout();

            return f;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0004] Raf Folders not found", ex);
        }

    }

    public List<RafFolder> getChildren(String rafPath) throws RafException {

        List<RafFolder> folderList = new ArrayList<RafFolder>();

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = rafPath;

            Node node = session.getNode(fullPath);

            for (NodeIterator iter = node.getNodes(); iter.hasNext();) {
                Node child = iter.nextNode();
                if (child.isNodeType(NODE_FOLDER) && !child.isNodeType(MIXIN_RECORD)) {
                    RafFolder f = nodeToRafFolder(child);
                    folderList.add(f);
                }
            }
            session.logout();
            folderList.sort(Comparator.comparing(RafObject::getName));
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0004] Raf Folders not found", ex);
        }

        return folderList;
    }

    /**
     * FIXME: findorCreateNode kullanamayız. Yetkisiz Raf oluşur.
     *
     * @param rafPath
     * @return
     * @throws RafException
     */
    public List<RafFolder> getFolderList(String rafPath) throws RafException {

        List<RafFolder> result = new ArrayList<>();

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = rafPath;

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

    /**
     * Sadece Verilen path altında bulunan folderların listesini döndürür.
     *
     * @param rafPath
     * @return
     * @throws RafException
     */
    public List<RafFolder> getChildFolderList(String rafPath) throws RafException {

        List<RafFolder> result = new ArrayList<>();

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = rafPath;

            Node node = session.getNode(fullPath);

            //Root'u ekleyecek miyiz? Aslında bu bir RafNode ama aynı zamanda bir folder.
            //RootNode'un parentId'sini saklıyoruz. Ayrıca # ile UI tarafında ağaç da düzgün olacak.
            //RafFolder f = nodeToRafFolder(node);
            //f.setParentId("#");
            //result.add(f);
            populateChildFolders(node, result);

            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0004] Raf Folders not found", ex);
        }

    }

    public RafCollection getCollection(String path) throws RafException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public NodeIterator getChildNodesByPagination(String absPath, int page, int pageSize, boolean justFolders, SortType sortBy, boolean descSort) {
        NodeIterator result = null;
        try {
            try {
                Session session = ModeShapeRepositoryFactory.getSession();

                QueryManager queryManager = session.getWorkspace().getQueryManager();

                //FIXME: Burada search textin için temizlenmeli. Kuralları bozacak bişiler olmamalı
                String expression = String.format("SELECT * FROM [%s] as nodes WHERE ISCHILDNODE(nodes,'%s')",
                        justFolders ? NODE_FOLDER : NODE_SEARCH, escapeQueryParam(absPath));

                if (justFolders) {
                    expression += " AND nodes.[jcr:mixinTypes] NOT IN ('raf:record')";
                }

                String sortQuery;

                switch (sortBy) {
                    case DATE_ASC: {
                        descSort = false;
                        sortQuery = String.format("nodes.[%s]", PROP_CREATED_DATE);
                        break;
                    }
                    case DATE_DESC: {
                        descSort = true;
                        sortQuery = String.format("nodes.[%s]", PROP_CREATED_DATE);
                        break;
                    }
                    case MODIFY_DATE_ASC: {
                        descSort = false;
                        sortQuery = String.format("nodes.[%s]", PROP_UPDATED_DATE);
                        break;
                    }
                    case MODIFY_DATE_DESC: {
                        descSort = true;
                        sortQuery = String.format("nodes.[%s]", PROP_UPDATED_DATE);
                        break;
                    }
                    case CATEGORY: {
                        sortQuery = String.format("nodes.[%s]", PROP_CATEGORY);
                        break;
                    }
                    case MIMETYPE: {
                        sortQuery = String.format("nodes.[%s]", PROP_MIMETYPE);
                        break;
                    }
                    case SIZE: {
                        sortQuery = "LENGTH(nodes.[jcr:content/jcr:data])";
                        break;
                    }
                    default: {
                        sortQuery = String.format("nodes.[%s]", PROP_TITLE);
                        break;
                    }
                }

                expression += String.format(" ORDER BY %s %s", sortQuery, descSort ? "DESC" : "ASC");

                Query query = queryManager.createQuery(expression, Query.JCR_SQL2);
                query.setLimit(pageSize);
                query.setOffset(page);
                QueryResult queryResult = query.execute();
                org.modeshape.jcr.api.query.QueryResult planResult = (org.modeshape.jcr.api.query.QueryResult) query.execute();
                String plan = planResult.getPlan();
                LOG.debug("Query executed for  {}", expression);
                LOG.debug("Query plan : {}", plan);
                result = queryResult.getNodes();

            } catch (RepositoryException ex) {
                throw new RafException("[RAF-0007] Raf Query Error", ex);
            }
        } catch (Exception e) {
            LOG.error("Exception", e);
        }

        return result;
    }

    public RafCollection getCollectionById(String id, boolean withPage, int page, int pageSize, boolean justFolders, SortType sortBy, Boolean descSort) throws RafException {
        RafCollection result = new RafCollection();

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            Node node = session.getNodeByIdentifier(id);

            if (node == null) {
                throw new RafException(String.format("[RAF-0005] Raf node not found, id: %s", id));
            }

            result.setId(node.getIdentifier());
            result.setMimeType(RafMimeTypes.RAF_FOLDER);
            result.setName(node.getName());
            result.setPath(node.getPath());
            //FIXME: Burada aslında raf'a denk geldiysel parentId null dönmeliyiz.
            result.setParentId(node.getParent().getIdentifier());

            result.setTitle(getPropertyAsString(node, PROP_TITLE));

            NodeIterator it = withPage ? getChildNodesByPagination(node.getPath(), page, pageSize, justFolders, sortBy, descSort) : node.getNodes();
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
                    RafDocument doc = nodeToRafDocument(n);
                    if(doc != null) result.getItems().add(doc);
                }
            }

            printSubgraph(node);

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
            String expression = String.format("SELECT * FROM [%s] WHERE [%s] = '%s' AND  ISDESCENDANTNODE('%s')",
                    MIXIN_TAGGABLE, PROP_CATEGORY_PATH, escapeQueryParam(categoryPath), escapeQueryParam(rootPath));

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
                    RafDocument doc = nodeToRafDocument(n);
                    if(doc != null) result.getItems().add(doc);
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
            String expression = String.format("SELECT * FROM [%s] WHERE [%s] = '%s' AND ISDESCENDANTNODE('%s')",
                    MIXIN_TAGGABLE, PROP_TAG, escapeQueryParam(tag), escapeQueryParam(rootPath));

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
                    RafDocument doc = nodeToRafDocument(n);
                    if(doc != null) result.getItems().add(doc);
                }
            }

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0007] Raf Query Error", ex);
        }

        return result;
    }

    public RafCollection getRafCollectionForAllNode() throws RafException {
        RafCollection result = new RafCollection();
        result.setId("SEARCH");
        result.setMimeType("raf/search");
        result.setPath("SEARCH");
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            QueryManager queryManager = session.getWorkspace().getQueryManager();

            String expression = "SELECT * FROM [" + NODE_SEARCH + "] as nodes";

            Query query = queryManager.createQuery(expression, Query.JCR_SQL2);
            QueryResult queryResult = query.execute();

            NodeIterator it = queryResult.getNodes();
            while (it.hasNext()) {
                Node n = it.nextNode();

                Node sn = it.nextNode();

                if (n.isNodeType("nt:resource")) {
                    sn = n.getParent();
                } else if (n.getName().endsWith(":metadata")) {
                    sn = n.getParent();
                }

                if (sn.isNodeType(NODE_FOLDER)) {
                    if (sn.isNodeType(MIXIN_RECORD)) {
                        result.getItems().add(nodeToRafRecord(sn));
                    } else {
                        result.getItems().add(nodeToRafFolder(sn));
                    }
                } else if (sn.isNodeType(NODE_FILE)) {
                    RafDocument doc = nodeToRafDocument(sn);
                    if(doc != null) result.getItems().add(doc);
                }
            }

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0007] Raf Query Error", ex);
        }

        return result;
    }

    public RafCollection getSearchCollection(String searchText, String rootPath, RafPathMemberService rafPathMemberService, String searcherUserName) throws RafException {
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
            String expression = String.format("SELECT * FROM [%s] as nodes WHERE CONTAINS(nodes.*, '%s') AND  ISDESCENDANTNODE('%s')",
                    NODE_SEARCH, escapeQueryParam(searchText), escapeQueryParam(rootPath));

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

                //node yetki kontrolü : eğer pathmember üyeliği yoksa raf yetkisi geçerlidir, eğer path üyeliği varsa okuma yetkisine bakılır.
                if (!rafPathMemberService.hasMemberInPath(searcherUserName, sn.getPath()) || rafPathMemberService.hasReadRole(searcherUserName, sn.getPath())) {
                    //Node tipine göre doğru conversion.
                    if (sn.isNodeType(NODE_FOLDER)) {
                        if (sn.isNodeType(MIXIN_RECORD)) {
                            result.getItems().add(nodeToRafRecord(sn));
                        } else {
                            result.getItems().add(nodeToRafFolder(sn));
                        }
                    } else if (sn.isNodeType(NODE_FILE)) {
                        RafDocument doc = nodeToRafDocument(sn);
                        if(doc != null) result.getItems().add(doc);
                    }
                }

            }

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0007] Raf Query Error", ex);
        }

        return result;
    }

    public String getJCRDate(Date dt) {
        //yyyy-MM-ddTHH:MM:ss.000Z
        SimpleDateFormat sdfForDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfForTime = new SimpleDateFormat("HH:mm:ss");

        return "CAST('" + sdfForDate.format(dt).concat("T").concat(sdfForTime.format(dt)) + ".000Z' AS DATE)";

    }

    //Note: JCR implementasyonlarında henüz count özelliği yok bu yüzden pagination larda şimdilik sonuç sayısı 1000 adet miş gibi cevap vereceğiz.
    @Deprecated
    private long getDetailedSearchCount(DetailedSearchModel searchModel, List<RafDefinition> rafs, RafPathMemberService rafPathMemberService, String searcherUserName) throws RafException {
        long result = 0;
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            QueryManager queryManager = session.getWorkspace().getQueryManager();

            //FIXME: Burada search textin için temizlenmeli. Kuralları bozacak bişiler olmamalı
            String expression = String.format("SELECT DISTINCT nodes.[jcr:name] as F_NAME FROM [%s] as nodes ", NODE_SEARCH);

            List<String> whereExpressions = new ArrayList();

            if (searchModel.getDateFrom() != null) {
                whereExpressions.add(String.format(" nodes.[%s] >= %s", PROP_CREATED_DATE, getJCRDate(searchModel.getDateFrom())));
            }

            if (searchModel.getDateTo() != null) {
                whereExpressions.add(String.format(" nodes.[%s] <= %s", PROP_CREATED_DATE, getJCRDate(searchModel.getDateTo())));
            }

            if (!Strings.isNullOrEmpty(searchModel.getSearchText())) {
                whereExpressions.add(String.format(" CONTAINS(nodes.*, '%s') ", escapeQueryParam(searchModel.getSearchText())));
            }

            if (searchModel.getSearchSubPath() == null) {
                searchModel.setSearchSubPath("");
            }

            if (!Strings.isNullOrEmpty(searchModel.getSearchRaf())) {
                whereExpressions.add(" ISDESCENDANTNODE(nodes,'/RAF/".concat(searchModel.getSearchRaf()).concat(searchModel.getSearchSubPath()) + "') ");
            } else {
                String rafWheres = " ( ";
                for (RafDefinition raf : rafs) {
                    rafWheres += " ISDESCENDANTNODE(nodes,'" + raf.getNode().getPath().concat(searchModel.getSearchSubPath()) + "') OR ";
                }
                rafWheres = rafWheres.substring(0, rafWheres.length() - 3);
                rafWheres += " ) ";
                whereExpressions.add(rafWheres);
            }

            if (!Strings.isNullOrEmpty(searchModel.getDocumentType())) {
                whereExpressions.add(String.format(" exdoc.[externalDoc:documentType] LIKE '%s' ", searchModel.getDocumentType()));
            }

            if (!Strings.isNullOrEmpty(searchModel.getDocumentStatus())) {
                whereExpressions.add(String.format(" exdoc.[externalDoc:documentStatus] LIKE '%s'", searchModel.getDocumentStatus()));
            }

            if (searchModel.getRegisterDateFrom() != null) {
                whereExpressions.add(String.format(" exdoc.[externalDoc:documentCreateDate] >= %s", getJCRDate(searchModel.getRegisterDateFrom())));
            }

            if (searchModel.getRegisterDateTo() != null) {
                whereExpressions.add(String.format(" exdoc.[externalDoc:documentCreateDate] <= %s", getJCRDate(searchModel.getRegisterDateTo())));
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
                        whereExpressions.add(String.format(" meta.[externalDocMetaTag:externalDocTypeAttribute] LIKE '%%%s%%' AND meta.[externalDocMetaTag:value] LIKE '%%%s%%' ",
                                key, escapeQueryParam(valueStr)));
                    }
                }
            }

            String lastWhereExpression = "";

            if (!whereExpressions.isEmpty()) {
                lastWhereExpression += " WHERE ";
                for (String whereExpression : whereExpressions) {
                    lastWhereExpression += whereExpression.concat(" AND ");
                }
                lastWhereExpression = lastWhereExpression.substring(0, lastWhereExpression.length() - 4).trim();
            }

            if (lastWhereExpression.contains("exdoc")) {
                expression += " JOIN [externalDoc:metadata] as exdoc on ISCHILDNODE(exdoc,nodes) ";
            }

            if (lastWhereExpression.contains("meta")) {
                expression += " JOIN [externalDocMetaTag:metadata] as meta on ISCHILDNODE(meta,nodes) ";
            }

            expression = expression.concat(lastWhereExpression);
            Query query = queryManager.createQuery(expression, Query.JCR_SQL2);
            QueryResult queryResult = query.execute();
            org.modeshape.jcr.api.query.QueryResult resultt = (org.modeshape.jcr.api.query.QueryResult) query.execute();
            String plan = resultt.getPlan();
            LOG.debug("Query executed for  {}", expression);
            LOG.debug("Query plan : {}", plan);
            result = queryResult.getRows().getSize();

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0007] Raf Query Error", ex);
        }

        return result;
    }

    public RafCollection getDetailedSearchCollection(DetailedSearchModel searchModel,
            List<RafDefinition> rafs,
            RafPathMemberService rafPathMemberService, String searcherUserName, int limit, int offset, List extendedQuery, List extendedSortQuery) throws RafException {
        RafCollection result = new RafCollection();
        result.setId("SEARCH");
        result.setMimeType("raf/search");
        result.setTitle("Detay Arama");
        result.setPath("SEARCH");
        result.setName("Detay Arama");
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            QueryManager queryManager = session.getWorkspace().getQueryManager();

            //FIXME: Burada search textin için temizlenmeli. Kuralları bozacak bişiler olmamalı
            String expression = "SELECT DISTINCT nodes.[" + PROP_PATH + "] FROM [" + NODE_SEARCH + "] as nodes ";

            List<String> whereExpressions = new ArrayList();

            whereExpressions.addAll(extendedQuery);

            String lastWhereExpression = "";

            if (!whereExpressions.isEmpty()) {
                lastWhereExpression += " WHERE ";
                for (String whereExpression : whereExpressions) {
                    lastWhereExpression += whereExpression.concat(" AND ");
                }
                lastWhereExpression = lastWhereExpression.substring(0, lastWhereExpression.length() - 4).trim();
            }

            if (extendedSortQuery != null && !extendedSortQuery.isEmpty()) {
                lastWhereExpression = lastWhereExpression.concat(" ORDER BY ");
                for (int i = 0; i < extendedSortQuery.size(); i++) {
                    lastWhereExpression = lastWhereExpression.concat(extendedSortQuery.get(i).toString());
                    if (i < extendedSortQuery.size() - 1) {
                        lastWhereExpression = lastWhereExpression.concat(" AND ");
                    }
                }
            }

            expression = expression.concat(lastWhereExpression).concat(String.format(" LIMIT %d OFFSET %d ", limit, offset));
            LOG.debug("JCR-SQL2 Query will be executed  {}", expression);
            Query query = queryManager.createQuery(expression, Query.JCR_SQL2);
            QueryResult queryResult = query.execute();
            org.modeshape.jcr.api.query.QueryResult resultt = (org.modeshape.jcr.api.query.QueryResult) query.execute();
            String plan = resultt.getPlan();

            LOG.debug("Query plan : {}", plan);

            NodeIterator it = queryResult.getNodes();
            while (it.hasNext()) {
                LOG.debug("Search result next.");
                Node n = it.nextNode();
                Node sn = session.getNode(n.getPath());

                if (n.isNodeType("nt:resource")) {
                    sn = n.getParent();
                } else if (n.getName().endsWith(":metadata")) {
                    sn = n.getParent();
                }

                //node yetki kontrolü : eğer pathmember üyeliği yoksa raf yetkisi geçerlidir, eğer path üyeliği varsa okuma yetkisine bakılır.
                if (!sn.isNodeType(MIXIN_RAF) && !rafPathMemberService.hasMemberInPath(searcherUserName, sn.getPath()) || rafPathMemberService.hasReadRole(searcherUserName, sn.getPath())) {
                    //Node tipine göre doğru conversion.
                    if (sn.isNodeType(NODE_FOLDER)) {
                        if (sn.isNodeType(MIXIN_RECORD)) {
                            result.getItems().add(nodeToRafRecord(sn));
                        }
                    } else if (sn.isNodeType(NODE_FILE)) {
                        RafDocument doc = nodeToRafDocument(sn);
                        if(doc != null) result.getItems().add(doc);
                    }

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

            String fullPath = getEncodedDirName(folder.getPath());

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

            String fullPath = getEncodedDirName(folderPath);

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

    public RafObject checkout(String path) throws RafException {
        RafObject result = null;

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            Node node = session.getNode(path);
            if (node.isNodeType(NODE_FOLDER) && !node.isNodeType(MIXIN_RECORD)) {
                //Folder'lar versionlanmaz! Dolayısı ile checkout edilmez
                throw new RafException("[RAF-00010] Folder node cannot checkout");
            }

            if (!node.hasNode(NODE_CONTENT)) {
                throw new RafException(String.format("[RAF-0043] JCR Content is not found. \"jcr:content\" not found. Path: %s, NodeID: %s",
                        node.getPath(),
                        node.getIdentifier()));
            }

            VersionManager vm = session.getWorkspace().getVersionManager();
            Node content = node.getNode(NODE_CONTENT);
            if (!content.isNodeType(MIXIN_VERSIONABLE)) {
                //Eğer daha öncesinde version eklenmiş ise önce onu ekliyoruz!
                content.addMixin(MIXIN_VERSIONABLE);
            }
            vm.checkout(content.getPath());
            //FIXME: devamında ne olacak?
            session.save();
            printSubgraph(node);
            result = nodeToRafDocument(node);
            return result;

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-00011] Raf Node cannot checkout", ex);
        }
    }

    public RafObject checkin(String path) throws RafException {
        RafObject result = null;

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            Node node = session.getNode(path);
            if (node.isNodeType(NODE_FOLDER) && !node.isNodeType(MIXIN_RECORD)) {
                //Folder'lar versionlanmaz! Dolayısı ile checkout edilmez
                throw new RafException("[RAF-00010] Folder node cannot checkout");
            }

            if (!node.hasNode(NODE_CONTENT)) {
                throw new RafException(String.format("[RAF-0043] JCR Content is not found. \"jcr:content\" not found. Path: %s, NodeID: %s",
                        node.getPath(),
                        node.getIdentifier()));
            }

            VersionManager vm = session.getWorkspace().getVersionManager();
            Node content = node.getNode(NODE_CONTENT);
            if (!content.isNodeType(MIXIN_VERSIONABLE)) {
                //Eğer daha öncesinde version eklenmiş ise önce onu ekliyoruz!
                content.addMixin(MIXIN_VERSIONABLE);
            }
            vm.checkin(content.getPath());
            //FIXME: devamında ne olacak?
            session.save();
            printSubgraph(node);
            result = nodeToRafDocument(node);
            return result;

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-00014] Raf Node cannot checkin", ex);
        }
    }

    //checkin with new version...
    public RafObject checkin(String path, InputStream in, String versionComment) throws RafException {
        RafObject result = null;

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            if (!session.nodeExists(path)) {
                //Olmayan bir dosya için checkin yapılamaz!
                throw new RafException("[RAF-00012] Raf Node not exist");
            }

            Node node = session.getNode(path);
            //TODO: Record'lar için sürüm desteği biraz sorunlu, bunu bir düşünelim.
            if (node.isNodeType(NODE_FOLDER) && !node.isNodeType(MIXIN_RECORD)) {
                //Folder'lar versionlanmaz! Dolayısı ile checkin yapılamaz!
                throw new RafException("[RAF-00013] Raf Folder cannot checked in");
            }

            if (!node.hasNode(NODE_CONTENT)) {
                throw new RafException(String.format("[RAF-0043] JCR Content is not found. \"jcr:content\" not found. Path: %s, NodeID: %s",
                        node.getPath(),
                        node.getIdentifier()));
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

            printSubgraph(node);

            //şimdi yeni belgeyi ekleyelim
            versionManager.checkout(content.getPath());
            content.addMixin(MIXIN_RAF_VERSIONING);
            content.setProperty(PROP_RAF_VERSION_COMMENT, versionComment);
            node = jcrTools.uploadFile(session, path, in);
            session.save();
            versionManager.checkin(content.getPath());

            printSubgraph(node);

            node = findPreviewAndWait(session, node);
            result = nodeToRafDocument(node);
            return result;

        } catch (RepositoryException | IOException ex) {
            throw new RafException("[RAF-00014] Raf Node cannot checkin.", ex);
        } catch (InterruptedException ex) {
            LOG.error("Interrupted Exception", ex);
            Thread.currentThread().interrupt();
            throw new RafException("[RAF-00014] Raf Node cannot checkin.", ex);
        }
    }

    public List<RafVersion> getVersionHistory(RafObject object) throws RafException {
        try {

            List<RafVersion> result = new ArrayList<>();

            Session session = ModeShapeRepositoryFactory.getSession();

            Node node = session.getNodeByIdentifier(object.getId());

            if (node == null) {
                throw new RafException("[RAF-0005] Raf node not found");
            }

            if (!node.isNodeType(NODE_FILE)) {
                throw new RafException("[RAF-0029] Not a document type");
            }

            if (!node.hasNode(NODE_CONTENT)) {
                throw new RafException(String.format("[RAF-0043] JCR Content is not found. \"jcr:content\" not found. Path: %s, NodeID: %s",
                        node.getPath(),
                        node.getIdentifier()));
            }

            Node content = node.getNode(NODE_CONTENT);
            if (content.isNodeType(MIXIN_VERSIONABLE)) {
                VersionManager versionManager = session.getWorkspace().getVersionManager();
                VersionHistory vh = versionManager.getVersionHistory(content.getPath());
                VersionIterator vit = vh.getAllVersions();
                while (vit.hasNext()) {
                    Version v = vit.nextVersion();
                    RafVersion rv = new RafVersion();
                    rv.setId(v.getIdentifier());
                    rv.setName(v.getName());
                    rv.setCreatedBy(getPropertyAsString(v.getFrozenNode(), "jcr:lastModifiedBy"));
                    rv.setCreated(v.getProperty(PROP_CREATED_DATE).getDate().getTime());
                    rv.setPath(v.getPath());
                    if (v.getFrozenNode().hasProperty(PROP_RAF_VERSION_COMMENT)) {
                        rv.setComment(v.getFrozenNode().getProperty(PROP_RAF_VERSION_COMMENT).getString());
                    }
                    result.add(rv);
                }
            }

            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0020] Raf Object not found", ex);
        }
    }

    public InputStream getVersionContent(String id, String versionName) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            VersionManager versionManager = session.getWorkspace().getVersionManager();

            Node node = session.getNodeByIdentifier(id);

            if (!node.hasNode(NODE_CONTENT)) {
                throw new RafException(String.format("[RAF-0043] JCR Content is not found. \"jcr:content\" not found. Path: %s, NodeID: %s",
                        node.getPath(),
                        node.getIdentifier()));
            }

            Node content = node.getNode(NODE_CONTENT);

            if (!content.isNodeType(MIXIN_VERSIONABLE)) {
                throw new RafException("[RAF-0124] Raf Node History content cannot found");
            }

            //Sadece CONTENT kısmının tarihçesini saklıyoruz. Dolayısı ile onun path'ine ihtiyacımız var.
            VersionHistory vh = versionManager.getVersionHistory(content.getPath());

            Version v = vh.getVersion(versionName);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(v.getFrozenNode().getProperty(PROP_DATA).getBinary().getStream(), bos);

            session.logout();

            ByteArrayInputStream result = new ByteArrayInputStream(bos.toByteArray());

            return result;

        } catch (IOException | RepositoryException ex) {
            LOG.error("RafException", ex);
            throw new RafException("[RAF-0024] Raf Node content cannot found", ex);
        }
    }

    public RafDocument uploadDocument(String fileName, InputStream in) throws RafException {
        return uploadDocument(fileName, in, true);
    }

    public RafDocument uploadDocument(String fileName, InputStream in, boolean generatePreview) throws RafException {
        if (Strings.isNullOrEmpty(fileName)) {
            throw new RafException("[RAF-00016] Filename cannot be null");
        }

        RafDocument result = null;

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullName = getEncodedPath(fileName);
            LOG.debug("Encoded FileName : {}", fullName);

            Node n = jcrTools.uploadFile(session, fullName, in);

            n.addMixin(MIXIN_TITLE);
            n.addMixin(MIXIN_TAGGABLE);
            n.addMixin(MIXIN_METADATA);

            String[] fa = fileName.split("/");
            n.setProperty(PROP_TITLE, fa[fa.length - 1]);

            session.save();

            if (!n.hasNode(NODE_CONTENT)) {
                throw new RafException(String.format("[RAF-0043] JCR Content is not found. \"jcr:content\" not found. Path: %s, NodeID: %s",
                        n.getPath(),
                        n.getIdentifier()));
            }

            //FIXME: Bazı durumlarda upload sırasında mimeType bulunamıyor. Bu durumda null gelmesi yerine "raf/binary atadık. Buna daha iyi bir çözüm lazım.
            Node nc = n.getNode(NODE_CONTENT);
            String mimeType = getPropertyAsString(nc, PROP_MIMETYPE);
            if (Strings.isNullOrEmpty(mimeType)) {
                nc.setProperty(PROP_MIMETYPE, "raf/binary");
            }

            //Normalde mimeType application/xml geliyor BPMN için bunu değiştiriyoruz.
            //TODO: MimeType dedection için aslında daha düzgün bir şey gerek. 
            if (fileName.endsWith(".bpmn") || fileName.endsWith(".bpmn2")) {
                nc.setProperty(PROP_MIMETYPE, "application/bpmn-xml");
            }

            session.save();

            if (generatePreview) {
                // Sequencer'lar çalışıyor, bu arada preview node oluşana kadar bekleyelim.(Eğer preview sequencer tanımlı ise)
                n = findPreviewAndWait(session, n);
            }

            result = nodeToRafDocument(n);

            session.logout();
            LOG.debug("Dosya JCR'e kondu : {}", fullName);
        } catch (RepositoryException ex) {
            LOG.error("Repository Exception", ex);
            throw new RafException("[RAF-00017] File cannot uploaded", ex);
        } catch (IOException ex) {
            LOG.error("IO Exception", ex);
            throw new RafException("[RAF-00018] File cannot uploaded", ex);
        } catch (InterruptedException ex) {
            LOG.error("Interrupted Exception", ex);
            Thread.currentThread().interrupt();
            throw new RafException("[RAF-00019] File cannot uploaded", ex);
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

            printSubgraph(node);

            if (node.isNodeType(NODE_FOLDER)) {
                if (node.isNodeType(MIXIN_RECORD)) {
                    result = nodeToRafRecord(node);
                } else {
                    result = nodeToRafFolder(node);
                }
            } else if (node.isNodeType(NODE_FILE) && (node.hasNode(NODE_CONTENT))) {
                result = nodeToRafDocument(node);
            } else {
                throw new RafException("[RAF-0005] Raf node not found");
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

            //TODO: Burada encode etmek doğru bir areket mi?
            path = getEncodedPath(path);
            Node node = session.getNode(path);

            RafObject result = null;

            if (node == null) {
                //FIXME: bu exception nedir söylemek lazım.
                throw new RafException("[RAF-0005] Raf node not found");
            }

            printSubgraph(node);

            if (node.isNodeType(NODE_FOLDER)) {
                if (node.isNodeType(MIXIN_RECORD)) {
                    result = nodeToRafRecord(node);
                } else {
                    result = nodeToRafFolder(node);
                }
            } else if (node.isNodeType(NODE_FILE) && node.hasNode(NODE_CONTENT)) {
                result = nodeToRafDocument(node);
            } else {
                throw new RafException("[RAF-0005] Raf node not found");
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
            if (node.hasNode(NODE_CONTENT)) {
                Node cn = node.getNode(NODE_CONTENT);
                if (cn.isNodeType(MIXIN_VERSIONABLE) && !cn.isCheckedOut()) {
                    VersionManager vm = session.getWorkspace().getVersionManager();
                    vm.checkout(cn.getPath());
                }
                cn.setProperty(PROP_UPDATED_BY, session.getUserID());
                GregorianCalendar gCal = new GregorianCalendar();
                gCal.setTime(new Date());
                cn.setProperty(PROP_UPDATED_DATE, gCal);
            }

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
            session.save();
            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0023] Raf Metadata cannot saved", ex);
        }
    }

    public void saveMetadatas(String id, List<RafMetadata> metadatas) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            for (RafMetadata metadata : metadatas) {
                try {
                    saveMetadata(id, metadata, session);
                } catch (Exception ex) {
                    LOG.error("Exception", ex);
                }
            }
            session.save();
            session.logout();
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

            if (!node.hasNode(NODE_CONTENT)) {
                throw new RafException(String.format("[RAF-0043] JCR Content is not found. \"jcr:content\" not found. Path: %s, NodeID: %s",
                        node.getPath(),
                        node.getIdentifier()));
            }

            Node content = node.getNode(NODE_CONTENT);

            //FIXME: Burada böyle bi rtakla gerçekten lazım mı? Bütün veriyi memory'e okumak dert olcaktır...
            /*
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(content.getProperty(PROP_DATA).getBinary().getStream(), bos);

            session.logout();

            ByteArrayInputStream result = new ByteArrayInputStream(bos.toByteArray());

            return result;
             */
            return content.getProperty(PROP_DATA).getBinary().getStream();

        } catch (RepositoryException ex) {
            LOG.error("RAfException", ex);
            throw new RafException("[RAF-0024] Raf Node content cannot found", ex);
        }
    }

    public void getDocumentContent(String id, OutputStream out) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            Node node = session.getNodeByIdentifier(id);

            LOG.debug("Document Content Requested: {}", node.getPath());

            if (!node.hasNode(NODE_CONTENT)) {
                throw new RafException(String.format("[RAF-0043] JCR Content is not found. \"jcr:content\" not found. Path: %s, NodeID: %s",
                        node.getPath(),
                        node.getIdentifier()));
            }

            Node content = node.getNode(NODE_CONTENT);

            IOUtils.copy(content.getProperty(PROP_DATA).getBinary().getStream(), out);

            session.logout();

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

            if (!node.hasNode(NODE_PREVIEW)) {
                throw new RafException("[RAF-0035] Raf Node preview cannot found");
            }

            Node content = node.getNode(NODE_PREVIEW);

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

    public InputStream getThumbnailContent(String id) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            Node node = session.getNodeByIdentifier(id);

            LOG.debug("PDF Document Thumbnail Content Requested: {}", node.getPath());
            // thumbnail jcr'de yok preview'dan bakıp oluşturalım.
            if (!node.hasNode(NODE_THUMBNAIL) && node.hasNode(NODE_PREVIEW)) {
                Node content = node.getNode(NODE_PREVIEW);
                if (content.getProperty("jcr:mimeType").getString().equals("image/png")) {
                    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                        InputStream is = content.getProperty("jcr:data").getBinary().getStream();
                        BufferedImage img = ImageUtils.createThumbnailImageFromInputStream(is);
                        LOG.debug("First page of the pdf has been successfully converted to an image: {}", node.getPath());
                        ImageIO.write(img, "png", bos);
                        is.close();
                        session.logout();
                        return new ByteArrayInputStream(bos.toByteArray());
                    }
                } else if (content.getProperty("jcr:mimeType").getString().equals("application/pdf")) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    PDDocument document = PDDocument.load(content.getProperty("jcr:data").getBinary().getStream());
                    PDFRenderer pdfRenderer = new PDFRenderer(document);
                    BufferedImage img = ImageUtils.createThumbnailImageFromPDF(pdfRenderer);
                    LOG.debug("First page of the pdf has been successfully converted to an image: {}", node.getPath());
                    ImageIO.write(img, "png", bos);
                    session.logout();
                    document.close();
                    return new ByteArrayInputStream(bos.toByteArray());
                }
            } else if (node.hasNode(NODE_THUMBNAIL)) {
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    Node content = node.getNode(NODE_THUMBNAIL);
                    IOUtils.copy(content.getProperty("jcr:data").getBinary().getStream(), bos);
                    session.logout();
                    return new ByteArrayInputStream(bos.toByteArray());
                }
            }
        } catch (RepositoryException | IOException ex) {
            LOG.error("RafException", ex);
            throw new RafException("[RAF-0024] Raf Node content cannot found", ex);
        }
        return null;
    }

    public InputStream getFullPdfDocumentContent(String id) throws RafException {

        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            Node node = session.getNodeByIdentifier(id);

            if (!node.hasNode(NODE_CONTENT)) {
                throw new RafException(String.format("[RAF-0043] JCR Content is not found. \"jcr:content\" not found. Path: %s, NodeID: %s",
                        node.getPath(),
                        node.getIdentifier()));
            }

            LOG.debug("Requested Full PDF Review. Path: {}, NodeID: {}", node.getPath(), node.getIdentifier());

            Node content = node.getNode(NODE_CONTENT);
            String mimeType = content.getProperty(PROP_MIMETYPE).getString();
            Binary binary = content.getProperty(PROP_DATA).getBinary();
            if (PdfConverter.instance().isAcceptedMimeType(mimeType)) {
                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    PdfConverter.instance().convertReader(binary.getStream(), mimeType, os);
                    return new ByteArrayInputStream(os.toByteArray());
                } catch (OfficeException ex) {
                    throw new RafException(String.format("[RAF-0045] Office Converter Cannot Worked Properly. Path: %s, NodeID: %s",
                            node.getPath(),
                            node.getIdentifier()));
                }
            }
            return binary.getStream();
        } catch (RepositoryException | IOException ex) {
            LOG.error("RafException", ex);
            throw new RafException("[RAF-0024] Raf Node content cannot found", ex);
        }
    }

    public void regeneratePreviews(String id) throws RafException {
        regeneratePreviews(id, false);
    }

    /**
     * Idsi verilen folder'ın altındaki bütün belgeler için preview oluşturur.
     *
     * Arayüzden doğrudan çağırmamak gerekir. RegenaratePreviewCommand'ı ile
     * çağrılmalı
     *
     * @param id
     * @throws RafException
     */
    public void regeneratePreviews(String id, boolean regenerateOnlyMissingPreviews) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            Node node = session.getNodeByIdentifier(id);
            regeneratePreviews(node, regenerateOnlyMissingPreviews);
            session.logout();
        } catch (RepositoryException ex) {
            LOG.error("RAfException", ex);
            throw new RafException("[RAF-0024] Raf Node content cannot found", ex);
        }
    }

    /**
     * Verilen node nt:folder ise altındaki dosyalar için preview çıkarırır.
     *
     * @param node
     * @throws RafException
     */
    protected void regeneratePreviews(Node node, boolean regenerateOnlyMissingPreviews) throws RafException {

        try {
            if (node.isNodeType(NODE_FOLDER)) {
                for (NodeIterator iter = node.getNodes(); iter.hasNext();) {
                    Node child = iter.nextNode();
                    if (child.isNodeType(NODE_FOLDER)) {
                        regeneratePreviews(child, regenerateOnlyMissingPreviews);
                    } else if (child.isNodeType(NODE_FILE)) {
                        if (regenerateOnlyMissingPreviews) {
                            boolean hasContent = child.isNodeType(NODE_FILE) && child.hasNode(NODE_CONTENT);
                            boolean hasPreview = !child.isNew() && child.hasNode(NODE_PREVIEW);
                            if (hasContent && hasPreview) {
                                continue;
                            }
                        }
                        //çünkü başka bir seesion açılsın istiyoruz.
                        regeneratePreview(child.getIdentifier());
                    }
                }
            }

        } catch (RepositoryException ex) {
            LOG.error("RAfException", ex);
            throw new RafException("[RAF-0024] Raf Node content cannot found", ex);
        }

    }

    boolean isPreviewGenerateable(RafObject rafObject) {
        String expPath = rafObject.getPath() + "/" + NODE_CONTENT + "/@" + PROP_DATA;

        return SequencerRegistery.getSequencers().stream()
                .map((SequencerConfig sequencerConfig) -> {
                    try {
                        return SequencerPathExpression.compile(sequencerConfig.getExpression()).matcher(expPath);
                    } catch (InvalidSequencerPathExpression e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .anyMatch(SequencerPathExpression.Matcher::matches);
    }

    /**
     * ID'si verilen nodun preview dosyasini yeniden uretir.
     *
     * @param id
     */
    public void regeneratePreview(String id) throws RafException {

        try {

            Session session = ModeShapeRepositoryFactory.getSession();
            Node node = session.getNodeByIdentifier(id);
            if (node.isNodeType(NODE_FILE) && node.hasNode(NODE_CONTENT)) {
                Node nodeContent = node.getNode(NODE_CONTENT);
                String expPath = node.getPath() + "/" + NODE_CONTENT + "/@" + PROP_DATA;

                for (SequencerConfig sequencerConfig : SequencerRegistery.getSequencers()) {
                    if (SequencerPathExpression.compile(sequencerConfig.getExpression()).matcher(expPath).matches()) {
                        switch (sequencerConfig.getName()) {
                            case "ImagePreviewSequencer": {
                                LOG.info("Image preview generating for {}",node.getPath());
                                FilePreviewHelper.generateImagePreview(nodeContent.getProperty(PROP_DATA), node);
                                break;
                            }
                            case "PdfPreviewSequencer":
                            case "OfficePreviewSequencer": {
                                LOG.info("PDF preview generating for {}",node.getPath());
                                FilePreviewHelper.generatePDFPreview(nodeContent.getProperty(PROP_DATA), node);
                                break;
                            }
                        }
                        session.save();
                    }
                }
            }
            session.logout();

        } catch (RepositoryException ex) {
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

            try {
                //Burada tarihçe silmek gerek. Aksi halde bütün dosyalar sistem de kalacak
                //TODO: bu aslında bir sistem ayarı olabilir. Bazı müşteriler gerçekten hiç bir şey silmek istemezler!
                deleteVersionHistory(node);
            } catch (RepositoryException ex) {
                LOG.warn("[RAF-0045] Failed to remove version history. Path: {}, NodeID: {}",
                        node.getPath(),
                        node.getIdentifier());
            }

            node.remove();

            session.save();
            session.logout();

        } catch (ItemNotFoundException ex) {
            throw new RafException("[RAF-0046] Item not found. It may be already deleted.", ex);
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0025] Raf Node cannot delete", ex);
        }

    }

    public void deleteVersion(String path, String versionName) throws RepositoryException {
        Session session = ModeShapeRepositoryFactory.getSession();
        Node node = session.getNode(path).getNode(NODE_CONTENT);
        if (node.isNodeType(MIXIN_VERSIONABLE)) {
            org.modeshape.jcr.api.version.VersionManager vm = (org.modeshape.jcr.api.version.VersionManager) node.getSession().getWorkspace().getVersionManager();
            //İlginç bir şekilde modeshape version listesini temizlemiyor. Bizim temizlememizi bekliyor.
            VersionHistory versionHistory = vm.getVersionHistory(node.getPath());
            versionHistory.removeVersion(versionName);
            session.save();
        }
        session.logout();
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
            //İlginç bir şekilde modeshape version listesini temizlemiyor. Bizim temizlememizi bekliyor.
            VersionHistory versionHistory = vm.getVersionHistory(node.getPath());
            VersionIterator vi = versionHistory.getAllVersions();
            while (vi.hasNext()) {
                Version v = vi.nextVersion();
                versionHistory.removeVersion(v.getName());
            }
            vm.remove(node.getPath());
        } else {
            NodeIterator it = node.getNodes();
            while (it.hasNext()) {
                Node n = it.nextNode();

                deleteVersionHistory(n);
            }
        }
    }

    public void copyObject(RafObject from, RafObject to) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            if (session.hasPendingChanges()) {
                LOG.warn("Saklanmamış değişlikler var");
                session.save();
            }

            //FIXME: burada hedef ismin olup olmadığı kontrol edilecek. Varsa isimde (1) gibi ekler yapılacak.
            //FIXME: url encoding
            String[] pathArr = targetPath(session, from, to.getPath());
            copy(session.getWorkspace(), from.getPath(), pathArr[0]);

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
            String[] pathArr = targetPath(session, from, to.getPath());
            copy(session.getWorkspace(), from.getPath(), pathArr[0]);

            session.save();
            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0026] Raf Node cannot copied", ex);
        }
    }

    public void copyObject(List<RafObject> from, RafObject to) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            //FIXME: burada hedef ismin olup olmadığı kontrol edilecek. Varsa isimde (1) gibi ekler yapılacak.
            //FIXME: url encoding
            for (RafObject o : from) {
                String[] pathArr = targetPath(session, o, to.getPath());
                copy(session.getWorkspace(), o.getPath(), pathArr[0], pathArr[1]);
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
                String[] pathArr = targetPath(session, o, to.getPath());
                copy(session.getWorkspace(), o.getPath(), pathArr[0]);
            }

            session.save();
            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0026] Raf Node cannot copied", ex);
        }
    }

    public void moveObject(RafObject from, RafRecord to) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            //FIXME: burada hedef ismin olup olmadığı kontrol edilecek. Varsa isimde (1) gibi ekler yapılacak.
            //FIXME: url encoding
            String[] pathArr = targetPath(session, from, to.getPath());
            move(session.getWorkspace(), from.getPath(), pathArr[0]);

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
            String[] pathArr = targetPath(session, from, to.getPath());
            move(session.getWorkspace(), from.getPath(), pathArr[0]);

            session.save();
            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0026] Raf Node cannot copied", ex);
        }
    }

    public void moveObject(List<RafObject> from, RafRecord to) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            //FIXME: burada hedef ismin olup olmadığı kontrol edilecek. Varsa isimde (1) gibi ekler yapılacak.
            //FIXME: url encoding
            for (RafObject o : from) {
                String[] pathArr = targetPath(session, o, to.getPath());
                move(session.getWorkspace(), o.getPath(), pathArr[0]);
            }

            session.save();
            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0026] Raf Node cannot copied", ex);
        }
    }

    public void moveObject(List<RafObject> from, RafObject to) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            //FIXME: burada hedef ismin olup olmadığı kontrol edilecek. Varsa isimde (1) gibi ekler yapılacak.
            //FIXME: url encoding
            for (RafObject o : from) {
                String[] pathArr = targetPath(session, o, to.getPath());
                move(session.getWorkspace(), o.getPath(), pathArr[0]);
            }

            session.save();
            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0026] Raf Node cannot copied", ex);
        }
    }

    protected String[] targetPath(Session session, RafObject o, String targetBase) throws RepositoryException, RafException {
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
            return new String[]{result, o.getName()};
        }

        //Demekki hedef var. Dolayısı ile ismini değiştirmek lazım.    
        String pathName = o.getName() + "(" + folderNode.getNodes(o.getName() + "*").getSize() + ")";
        String fileName = FilenameUtils.removeExtension(o.getName());
        String fileExtension = FilenameUtils.getExtension(o.getName());
        result = targetBase + "/" + fileName + "(" + folderNode.getNodes(o.getName() + "*").getSize() + ")" + "." + fileExtension;

        return new String[]{result, pathName};
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

    protected void copy(Workspace workspace, String fromPath, String toPath, String name) throws RepositoryException {
        workspace.copy(fromPath, toPath);

        //Copy sonrasında sürümlü dosyalar checkedout kalıyor. Checkedin yapalım.
        Node n = workspace.getSession().getNode(toPath);
        n.setProperty(PROP_TITLE, name);
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
        if (n.isNodeType(NODE_FILE) && n.hasNode(NODE_CONTENT)) {
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
     * @param name
     * @return
     */
    protected String getEncodedPath(String name) {
        return fileNameEncoder.encode(name);
    }

    /**
     * Orjinal haline geri çevriliyor
     *
     * @param path
     * @return
     */
    protected String getDecodedPath(String path) {
        return fileNameEncoder.decode(path);
    }

    /**
     * Türkçe ya da raf isminde kabul edilmeyecek karakterler temizleniyor
     *
     * @param name
     * @return
     */
    protected String getEncodedName(String name) {
        return rafNameEncoder.encode(name);
    }

    /**
     * Türkçe ya da dizin isminde kabul edilmeyecek karakterler temizleniyor
     *
     * @param name
     * @return
     */
    protected String getEncodedDirName(String name) {
        return dirNameEncoder.encode(name);
    }

    protected RafNode nodeToRafNode(Node node) throws RepositoryException {
        RafNode result = new RafNode();
        LOG.debug("NODE ID to be converted RafNode Object: {}, {}", node.getPath(), node.getIdentifier());
        result.setId(node.getIdentifier());
        result.setPath(node.getPath());
        result.setName(node.getName());

        return result;
    }

    protected RafFolder nodeToRafFolder(Node node) throws RepositoryException {
        RafFolder result = new RafFolder();
        LOG.debug("NODE ID to be converted RafFolder Object: {}, {}", node.getPath(), node.getIdentifier());
        result.setId(node.getIdentifier());
        result.setPath(node.getPath());
        result.setName(node.getName());
        result.setParentId(node.getParent().getIdentifier());

        result.setCreateBy(node.getProperty(PROP_CREATED_BY).getString());

        if (node.hasProperty(PROP_CREATED_DATE)) {
            result.setCreateDate(node.getProperty(PROP_CREATED_DATE).getDate().getTime());
        } else {
            result.setCreateDate(new GregorianCalendar().getTime());
        }

        if (node.isNodeType(MIXIN_TITLE)) {
            result.setTitle(getPropertyAsString(node, PROP_TITLE));
            result.setInfo(getPropertyAsString(node, PROP_DESCRIPTON));
        }

        if (Strings.isNullOrEmpty(result.getTitle())) {
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
        LOG.debug("NODE ID to be converted RafRecord Object: {}, {}", node.getPath(), node.getIdentifier());
        result.setId(node.getIdentifier());
        result.setPath(node.getPath());
        result.setName(node.getName());
        result.setParentId(node.getParent().getIdentifier());

        result.setCreateBy(node.getProperty(PROP_CREATED_BY).getString());
        result.setCreateDate(node.getProperty(PROP_CREATED_DATE).getDate().getTime());

        if (node.isNodeType(MIXIN_TITLE)) {
            result.setTitle(getPropertyAsString(node, PROP_TITLE));
            result.setInfo(getPropertyAsString(node, PROP_DESCRIPTON));
        }

        if (Strings.isNullOrEmpty(result.getTitle())) {
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
                RafDocument doc = nodeToRafDocument(mn);
                if(doc != null) result.getDocuments().add(doc);
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
        LOG.debug("NODE ID to be converted RafDocument Object: {}, {}", node.getPath(), node.getIdentifier());
        result.setId(node.getIdentifier());
        result.setPath(node.getPath());
        result.setName(node.getName());
        result.setParentId(node.getParent().getIdentifier());

        //bilenen diğer metadata ( createDate v.b. ) toplanmalı
        result.setCreateBy(node.getProperty(PROP_CREATED_BY).getString());
        result.setCreateDate(node.getProperty(PROP_CREATED_DATE).getDate().getTime());

        if (!node.hasNode(NODE_CONTENT)) {
            LOG.warn("[RAF-0043] JCR Content is not found. \"jcr:content\" not found. Path: {}, NodeID: {}",
                    node.getPath(),
                    node.getIdentifier());
            return null;
        }
        //FIXME: TIKA olmadığı için mimeType bulmada sorun olabilir.
        Node cn = node.getNode(NODE_CONTENT);
        String s = getPropertyAsString(cn, PROP_MIMETYPE);
        if (Strings.isNullOrEmpty(s)) {
            s = "raf/binary";
        }
        //Node üzerindeki SHA-1 değerini alalım.
        result.setHash(((BinaryValue) cn.getProperty(PROP_DATA).getBinary()).getHexHash());

        result.setMimeType(s);

        result.setUpdateBy(cn.getProperty(PROP_UPDATED_BY).getString());
        result.setUpdateDate(cn.getProperty(PROP_UPDATED_DATE).getDate().getTime());

        if (node.isNodeType(MIXIN_TITLE)) {
            result.setTitle(getPropertyAsString(node, PROP_TITLE));
            result.setInfo(getPropertyAsString(node, PROP_DESCRIPTON));
        }

        if (Strings.isNullOrEmpty(result.getTitle())) {
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
            printSubgraph(version);
        }

        result.setLength(content.getProperty(PROP_DATA).getLength());

        NodeIterator it = node.getNodes("*:metadata");
        while (it.hasNext()) {
            Node mn = it.nextNode();
            MetadataConverter mc = MetadataConverterRegistery.getConverter(mn.getPrimaryNodeType().getName());
            result.getMetadatas().add(mc.nodeToModel(mn));
        }

        //raf:preview var ise onun bilgilerini alalım.
        if (node.hasNode(NODE_PREVIEW)) {
            result.setHasPreview(Boolean.TRUE);
            Node preview = node.getNode(NODE_PREVIEW);
            result.setPreviewMimeType(getPropertyAsString(preview, PROP_MIMETYPE));
        }

        return result;
    }

    private void populateFolders(Node node, List<RafFolder> result) throws RafException {

        try {
            Session session = node.getSession();

            QueryManager queryManager = session.getWorkspace().getQueryManager();

            String expression = String.format("SELECT * FROM [%s] WHERE ISDESCENDANTNODE('%s')", NODE_FOLDER, escapeQueryParam(node.getPath()));

            Query query = queryManager.createQuery(expression, Query.JCR_SQL2);
            QueryResult queryResult = query.execute();

            NodeIterator it = queryResult.getNodes();
            while (it.hasNext()) {
                Node n = it.nextNode();

                //Node tipine göre doğru conversion.
                if (n.isNodeType(NODE_FOLDER) && !n.isNodeType(MIXIN_RECORD)) {
                    result.add(nodeToRafFolder(n));
                }
            }

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0007] Raf Query Error", ex);
        }

    }

    private void populateChildFolders(Node node, List<RafFolder> result) throws RafException {

        try {

            NodeIterator it = node.getNodes();
            while (it.hasNext()) {
                Node n = it.nextNode();

                //Node tipine göre doğru conversion.
                if (n.isNodeType(NODE_FOLDER) && !n.isNodeType(MIXIN_RECORD)) {
                    result.add(nodeToRafFolder(n));
                }
            }

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0007] Raf Query Error", ex);
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
            LOG.trace("Property not found : {}", prop);
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
            LOG.trace("Property not found : {}", prop);
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
            LOG.trace("Property not found : {}", prop);
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
            LOG.trace("Property not found : {}", prop);
        }

        return result;
    }

    private void printSubgraph(Node node) throws RepositoryException {
        if (debugMode) {
            jcrTools.printSubgraph(node);
        }
    }

    public Boolean getRafCheckStatus(String path) {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            Node node = session.getNode(path);
            boolean result = false;
            if (node == null) {
                result = false;
            } else {
                if (!node.isNodeType(MIXIN_RAFCHECKIN)) {
                    result = false;
                } else {
                    result = node.getProperty(PROP_RAF_CHECKIN_STATE).getBoolean();
                }
            }
            session.logout();
            return result;
        } catch (Exception ex) {
            LOG.error("Exception", ex);
            return false;
        }
    }

    public String getRafCheckOutPreviousVersion(String path) {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            Node node = session.getNode(path);
            String result = "";
            if (node == null) {
                result = "";
            } else {
                if (!node.isNodeType(MIXIN_RAFCHECKIN)) {
                    result = "";
                } else {
                    result = node.getProperty(PROP_RAF_CHECKIN_PREVIOUS_VERSION).getString();
                }
            }
            session.logout();
            return result;
        } catch (Exception ex) {
            LOG.error("Exception", ex);
            return "";
        }
    }

    public String getRafCheckerUser(String path) {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            Node node = session.getNode(path);
            String result = "";
            if (node == null) {
                result = "";
            } else {
                if (!node.isNodeType(MIXIN_RAFCHECKIN)) {
                    result = "";
                } else {
                    result = node.getProperty(PROP_RAF_CHECKIN_USER).getString();
                }
            }
            session.logout();
            return result;
        } catch (Exception ex) {
            LOG.error("Exception", ex);
            return "";
        }
    }

    public RafObject setRafCheckOutValue(String path, Boolean checkStatus, String userName, Date checkTime) throws RafException {
        try {
            RafObject result;
            Session session = ModeShapeRepositoryFactory.getSession();

            Node node = session.getNode(path);

            if (node == null) {
                throw new RafException("[RAF-0005] Raf node not found");
            }

            if (!node.hasNode(NODE_CONTENT)) {
                throw new RafException(String.format("[RAF-0043] JCR Content is not found. \"jcr:content\" not found. Path: %s, NodeID: %s",
                        node.getPath(),
                        node.getIdentifier()));
            }

            if (!node.isNodeType(MIXIN_RAFCHECKIN)) {
                node.addMixin(MIXIN_RAFCHECKIN);
            }

            node.setProperty(PROP_RAF_CHECKIN_STATE, checkStatus);
            node.setProperty(PROP_RAF_CHECKIN_USER, userName);
            Calendar c = Calendar.getInstance();
            c.setTime(checkTime);
            node.setProperty(PROP_RAF_CHECKIN_DATE, c);
            result = nodeToRafDocument(node);
            List<RafVersion> versions = getVersionHistory((RafDocument) result);
            //en son sürüm = edit için eklenen sürümdür, dolayısı ile bize edit sürümnden bir önceki sürüm lazım.
            node.setProperty(PROP_RAF_CHECKIN_PREVIOUS_VERSION, (versions != null && !versions.isEmpty()) ? versions.get(versions.size() - 2).getName() : "");
            session.save();
            result = nodeToRafDocument(node);
            session.logout();
            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0021] Raf properties cannot saved", ex);
        }
    }

    public RafObject turnBackToVersion(String path, String versionName) throws RafException {
        try {
            RafObject result;
            Session session = ModeShapeRepositoryFactory.getSession();
            Node node = session.getNode(path);
            if (node == null) {
                throw new RafException("[RAF-0005] Raf node not found");
            }

            if (!node.hasNode(NODE_CONTENT)) {
                throw new RafException(String.format("[RAF-0043] JCR Content is not found. \"jcr:content\" not found. Path: %s, NodeID: %s",
                        node.getPath(),
                        node.getIdentifier()));
            }

            VersionManager vm = session.getWorkspace().getVersionManager();
            Node content = node.getNode(NODE_CONTENT);
            VersionHistory vh = vm.getVersionHistory(content.getPath());
            Version targetVersion = vh.getVersion(versionName);
            VersionIterator vi = vh.getAllVersions();
            vm.restore(targetVersion, false);
            while (vi.hasNext()) {
                Version version = vi.nextVersion();
                version.getDepth();
                targetVersion.getDepth();
                if (!"jcr:rootVersion".equals(version.getName()) && version.getCreated().after(targetVersion.getCreated())) {
                    vh.removeVersion(version.getName());
                }
            }
            vm.checkout(content.getPath());
            vm.checkin(content.getPath());
            result = nodeToRafDocument(node);
            session.logout();
            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0021] Raf cannot turn to version", ex);
        }
    }

    private String escapeQueryParam(String param) {
        return param.replace("'", "\\'");
    }

    public void reindex() {
        try {
            try {
                Session session = ModeShapeRepositoryFactory.getSession();
                ((org.modeshape.jcr.api.Workspace) session.getWorkspace()).reindex("/RAF/");
                session.logout();
            } catch (RepositoryException ex) {
                throw new RafException("[RAF-0007] Raf Query Error", ex);
            }
        } catch (Exception e) {
            LOG.error("Exception", e);
        }
    }

    public RafCollection getLastCreatedOrModifiedFilesCollection(Date fromDate, List<RafDefinition> rafs, boolean created) throws RafException {
        RafCollection result = new RafCollection();
        result.setId("SEARCH");
        result.setMimeType("raf/search");
        result.setTitle("Detay Arama");
        result.setPath("SEARCH");
        result.setName("Detay Arama");

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            QueryManager queryManager = session.getWorkspace().getQueryManager();

            //FIXME: Burada search textin için temizlenmeli. Kuralları bozacak bişiler olmamalı
            String expression = "SELECT DISTINCT nodes.[" + PROP_PATH + "] FROM [" + NODE_SEARCH + "] as nodes ";

            List<String> whereExpressions = new ArrayList();

            if (fromDate != null) {
                whereExpressions.add(" nodes.[" + (created ? PROP_CREATED_DATE : PROP_UPDATED_DATE) + "] >= " + getJCRDate(fromDate));
            }

            String rafWheres = " ( ";
            for (RafDefinition raf : rafs) {
                rafWheres += " ISDESCENDANTNODE(nodes,'/RAF/" + raf.getCode() + "') OR ";
            }
            rafWheres = rafWheres.substring(0, rafWheres.length() - 3);
            rafWheres += " ) ";
            whereExpressions.add(rafWheres);

            String lastWhereExpression = "";

            if (!whereExpressions.isEmpty()) {
                lastWhereExpression += " WHERE ";
                for (String whereExpression : whereExpressions) {
                    lastWhereExpression += whereExpression.concat(" AND ");
                }
                lastWhereExpression = lastWhereExpression.substring(0, lastWhereExpression.length() - 4).trim();
            }

            expression = expression.concat(lastWhereExpression);
            Query query = queryManager.createQuery(expression, Query.JCR_SQL2);
            QueryResult queryResult = query.execute();
            org.modeshape.jcr.api.query.QueryResult resultt = (org.modeshape.jcr.api.query.QueryResult) query.execute();
            String plan = resultt.getPlan();

            LOG.debug("Query executed for  {}", expression);
            LOG.debug("Query plan : {}", plan);

            NodeIterator it = queryResult.getNodes();
            while (it.hasNext()) {
                LOG.debug("Search result next.");
                Node n = it.nextNode();
                Node sn = session.getNode(n.getPath());

                if (n.isNodeType("nt:resource")) {
                    sn = n.getParent();
                } else if (n.getName().endsWith(":metadata")) {
                    sn = n.getParent();
                }

                //node yetki kontrolü : eğer pathmember üyeliği yoksa raf yetkisi geçerlidir, eğer path üyeliği varsa okuma yetkisine bakılır.
                if (!sn.isNodeType(MIXIN_RAF)) {
                    //Node tipine göre doğru conversion.
                    if (sn.isNodeType(NODE_FOLDER)) {
                        if (sn.isNodeType(MIXIN_RECORD)) {
                            result.getItems().add(nodeToRafRecord(sn));
                        }
                    } else if (sn.isNodeType(NODE_FILE)) {
                        RafDocument doc = nodeToRafDocument(sn);
                        if(doc != null) result.getItems().add(doc);

                    }
                }

            }

        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0007] Raf Query Error", ex);
        }

        return result;
    }

    public void unregisterIndexes(String... indexNames) {
        try {
            try {
                Session session = ModeShapeRepositoryFactory.getSession();
                ((org.modeshape.jcr.api.Workspace) session.getWorkspace()).getIndexManager().unregisterIndexes(indexNames);
                session.logout();
            } catch (RepositoryException ex) {
                throw new RafException("[RAF-0007] Raf Query Error", ex);
            }
        } catch (Exception e) {
            LOG.error("Exception", e);
        }
    }

    public long getFolderSize(String absPath, Long maxSumSize) throws RafException {
        long result = 0;
        try {
            List<Node> folders = getAllChildFolders(absPath);
            List<Node> files = getChildFiles(absPath);

            for (Node folder : folders) {
                files.addAll(getChildFiles(folder.getPath()));
            }
            for (Node file : files) {
                if (file.hasProperty("jcr:content/jcr:data")) {
                    result += file.getProperty("jcr:content/jcr:data").getLength();
                    if (maxSumSize > 0 && maxSumSize < result) {
                        throw new RafException(String.format("Max file limit is over, Max File Limit : %d, File Size : %d", maxSumSize, result));
                    }
                }
            }
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0007] Raf Query Error", ex);
        }

        return result;
    }

    public String getDocumentExtractedText(String id) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            Node node = session.getNodeByIdentifier(id);
            if (node == null) {
                return "";
            }
            LOG.debug("Document Content Requested: {}", node.getPath());

            if (!node.hasNode(NODE_CONTENT)) {
                throw new RafException(String.format("[RAF-0043] JCR Content is not found. \"jcr:content\" not found. Path: %s, NodeID: %s",
                        node.getPath(),
                        node.getIdentifier()));
            }

            Node content = node.getNode(NODE_CONTENT);
            if (content == null) {
                return "";
            }
            Property prop = content.getProperty(PROP_DATA);
            if (prop == null) {
                return "";
            }
            BinaryValue binaryValue = (BinaryValue) prop.getBinary();
            if (binaryValue == null) {
                return "";
            }
            return ModeShapeRepositoryFactory.getBinaryStore().getText(binaryValue);

        } catch (RepositoryException ex) {
            LOG.error("RAfException", ex);
            throw new RafException("[RAF-0024] Raf Node content cannot found", ex);
        } catch (Exception ex) {
            LOG.error("RAfException", ex);
            throw new RafException("[RAF-0024] Raf Node content cannot found", ex);
        }
    }

    public void extractZipFile(RafObject zipFile, boolean generatePreview) throws RafException { try {
            RafObject destDir = getRafObject(zipFile.getParentId());
            InputStream fileIS = getDocumentContent(zipFile.getId());
            ZipInputStream zis = new ZipInputStream(fileIS);
            ZipEntry zipEntry = zis.getNextEntry();

            int generatedPreviewCounter = 0;
            boolean generateDecompressedFilePreview;
            Long zipExtractGeneratePreviewFileLimit = (long) -1;

            if (generatePreview) {
                 zipExtractGeneratePreviewFileLimit = ConfigResolver.resolve("zip.extract.preview.generation.file.limit")
                        .as(Long.class)
                        .withDefault((long) -1)
                        .getValue();
            }

            while (zipEntry != null) {
                try {
                    if (zipEntry.getSize() != 0) {
                        generateDecompressedFilePreview = (zipExtractGeneratePreviewFileLimit == -1) || (zipExtractGeneratePreviewFileLimit > 1 && generatedPreviewCounter < zipExtractGeneratePreviewFileLimit);
                        RafObject destFile = newFile(destDir, zipEntry, zis, generateDecompressedFilePreview);

                        if (generateDecompressedFilePreview && isPreviewGenerateable(destFile)) {
                            generatedPreviewCounter++;
                        }
                    } else {
                        createFolder(destDir.getPath().concat("/").concat(zipEntry.getName()));
                    }
                } catch (Exception ex) {
                    LOG.error("RafException", ex);
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            fileIS.close();

        } catch (Exception ex) {
            LOG.error("RafException", ex);
            throw new RafException("[RAF-0028] Failed to extract zip object", ex);
        }
    }

    public void extractZipFile(RafObject zipFile) throws RafException {
        extractZipFile(zipFile, true);
    }

    /**
     * Verilen path altındaki tüm klasör ağacını tarar ve döner.
     *
     * @param absPath
     * @return
     */
    public List<Node> getAllChildFolders(String absPath) throws RafException {
        List<Node> folders = new ArrayList<>();
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            QueryManager queryManager = session.getWorkspace().getQueryManager();
            String expression = "SELECT nodes.* FROM [" + NODE_FOLDER + "] as nodes WHERE ISCHILDNODE(nodes,'" + absPath.replaceAll("'", "") + "')";
            Query query = queryManager.createQuery(expression, Query.JCR_SQL2);
            QueryResult queryResult = query.execute();
            NodeIterator nodes = queryResult.getNodes();
            while (nodes.hasNext()) {
                Node n = nodes.nextNode();
                folders.add(n);
                if (n.hasNodes()) {
                    folders.addAll(getAllChildFolders(n.getPath()));
                }
            }
            return folders;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0041] Raf properties cannot be calculated", ex);
        }
    }

    /**
     * Verilen path altındaki tüm dosyaları çeker. Dikkat: Bu method klasör
     * ağacını vs. taramaz, yalnızca verilen path altındaki dosya'ları döndürür.
     *
     * @param absPath
     * @return
     */
    public List<Node> getChildFiles(String absPath) throws RafException {
        List<Node> files = new ArrayList<>();
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            QueryManager queryManager = session.getWorkspace().getQueryManager();
            String expression = "SELECT nodes.* FROM [" + NODE_FILE + "] as nodes WHERE ISCHILDNODE(nodes,'" + absPath.replaceAll("'", "") + "')";
            Query query = queryManager.createQuery(expression, Query.JCR_SQL2);
            QueryResult queryResult = query.execute();
            NodeIterator nodes = queryResult.getNodes();
            while (nodes.hasNext()) {
                Node n = nodes.nextNode();
                files.add(n);
            }
            return files;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0041] Raf properties cannot be calculated", ex);
        }
    }

    public Map<String, Long> getRafDefinitionProperties(String absPath) throws RafException {
        try {
            // 1. root path altındaki tüm klasör ağacını topluyoruz
            List<Node> folders = getAllChildFolders(absPath);
            // 2. root path için tüm dosyaları alalım
            List<Node> files = getChildFiles(absPath);
            // 3. root path altındaki tüm raf folderlar için de dosyaları alalım.
            for (Node f : folders) {
                files.addAll(getChildFiles(f.getPath()));
            }
            long totalFolderCount = folders.size();
            long totalFileCount = files.size();
            long totalFileSize = 0;
            for (Node f : files) {
                totalFileSize += f.getProperty("jcr:content/jcr:data").getLength();
            }
            Map<String, Long> propertiesMap = new HashMap<>();
            propertiesMap.put("totalFolderCount", totalFolderCount);
            propertiesMap.put("totalFileCount", totalFileCount);
            propertiesMap.put("totalFileSize", totalFileSize);
            return propertiesMap;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0041] Raf properties cannot be calculated", ex);
        }
    }

    private RafObject newFile(RafObject destinationDir, ZipEntry zipEntry, ZipInputStream zis) throws IOException, RafException {
        return newFile(destinationDir, zipEntry, zis, true);
    }

    private RafObject newFile(RafObject destinationDir, ZipEntry zipEntry, ZipInputStream zis, boolean generatePreview) throws IOException, RafException {
        String newFilePath = destinationDir.getPath().concat("/").concat(zipEntry.getName());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        org.apache.poi.util.IOUtils.copy(zis, bos);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        RafObject destFile = uploadDocument(newFilePath, bis, generatePreview);
        bos.close();
        bis.close();
        String destDirPath = destinationDir.getPath();
        String destFilePath = destFile.getPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public long getChildCount(String absPath) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            QueryManager queryManager = session.getWorkspace().getQueryManager();
            String expression = String.format("SELECT nodes.[jcr:name] FROM [%s] as nodes WHERE ISCHILDNODE(nodes,'%s')", NODE_SEARCH, absPath.replaceAll("'", ""));
            Query query = queryManager.createQuery(expression, Query.JCR_SQL2);
            return query.execute().getNodes().getSize();
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0041] Raf properties cannot be calculated", ex);
        }
    }

    /**
     * Preview kaydı yapılan uzantılar için asenkron çalışan sequencer'ların sonucunu beklemek için yazıldı.
     * Eğer 10 sn kadar preview node bulamazsa akışı kilitlemeden devam ediyor.
     *
     * @param session
     * @param n
     * @return
     * @throws RepositoryException
     * @throws InterruptedException
     */
    private Node findPreviewAndWait(Session session, Node n) throws RepositoryException, InterruptedException {
        long start = System.currentTimeMillis();
        long maxWaitInMillis = TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS);
        String expPath = n.getPath() + "/" + NODE_CONTENT + "/@" + PROP_DATA;
        do {
            try {
                // Session'dan güncel node'u tekrar alıyoruz.
                Node node = session.getNode(n.getPath());
                for (SequencerConfig sequencerConfig : SequencerRegistery.getSequencers()) {
                    // Yüklenen dosya sequencer expression ile uymuyorsa direk atlayabiliriz.
                    if (SequencerPathExpression.compile(sequencerConfig.getExpression()).matcher(expPath).matches()
                            && !n.hasNode(NODE_PREVIEW)) {
                        throw new PathNotFoundException("Preview node has not been created yet.");
                    }
                }
                return node;
            } catch (PathNotFoundException e) {
                // The node wasn't there yet, so try again ...
            } catch (Exception e) {
                LOG.warn("Unknown Error: ", e);
                return n;
            }
            Thread.sleep(10L);
        } while ((System.currentTimeMillis() - start) <= maxWaitInMillis);
        LOG.warn("Failed to find preview {} even after waiting {} {}", n.getPath(), 10, TimeUnit.SECONDS);return n;
    }

}
