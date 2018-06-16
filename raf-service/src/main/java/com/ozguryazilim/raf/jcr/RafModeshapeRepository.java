/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jcr;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.MetadataConverter;
import com.ozguryazilim.raf.MetadataConverterRegistery;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.models.RafMimeTypes;
import com.ozguryazilim.raf.models.RafNode;
import com.ozguryazilim.raf.models.RafObject;
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
import org.apache.commons.io.IOUtils;
import org.modeshape.common.text.UrlEncoder;
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

    private static final String NODE_FOLDER = "nt:folder";
    private static final String NODE_FILE = "nt:file";

    private static final String MIXIN_TITLE = "mix:title";
    private static final String MIXIN_TAGGABLE = "raf:taggable";
    private static final String MIXIN_RAF = "raf:raf";

    private static final String PROP_TITLE = "jcr:title";
    private static final String PROP_DESCRIPTON = "jcr:description";
    private static final String PROP_CATEGORY = "raf:category";
    private static final String PROP_CATEGORY_PATH = "raf:categoryPath";
    private static final String PROP_CATEGORY_ID = "raf:categoryId";
    private static final String PROP_TAG = "raf:tags";
    private static final String PROP_RAF_TYPE = "raf:type";

    private static final String RAF_TYPE_DEFAULT = "DEFAULT";
    private static final String RAF_TYPE_PRIVATE = "PRIVATE";
    private static final String RAF_TYPE_SHARED = "SHARED";

    private UrlEncoder encoder;
    JcrTools jcrTools = new JcrTools();

    @PostConstruct
    public void init() {
        try {
            start();
        } catch (RafException ex) {
            LOG.error("ModeShape cannot started", ex);
        }
    }

    public void start() throws RafException {
        try {
            encoder = new UrlEncoder();
            encoder.setSlashEncoded(false);

            //Engine'de başlatılsın
            Session session = ModeShapeRepositoryFactory.getSession();
            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException(ex);
        }
    }

    public void stop() throws RafException {
        ModeShapeRepositoryFactory.shutdown();
    }

    public RafNode createRafNode(RafDefinition definition) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(RAF_ROOT + definition.getCode());

            JcrTools jcrTools = new JcrTools();
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
            throw new RafException(ex);
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
            throw new RafException(ex);
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

            JcrTools jcrTools = new JcrTools();
            Node node = jcrTools.findOrCreateNode(session, fullPath, NODE_FOLDER);

            RafNode result = nodeToRafNode(node);

            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException(ex);
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

            JcrTools jcrTools = new JcrTools();
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
            throw new RafException(ex);
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

            JcrTools jcrTools = new JcrTools();
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
            throw new RafException(ex);
        }
    }

    public List<RafFolder> getFolderList(RafNode rafNode) throws RafException {
        return getFolderList(rafNode.getPath());
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
            throw new RafException(ex);
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
                throw new RafException();
            }

            result.setId(node.getIdentifier());
            result.setMimeType(RafMimeTypes.RAF_FOLDER);
            result.setName(node.getName());
            result.setPath(node.getPath());
            //FIXME: burada title attribute'u alınmalı
            result.setTitle(getPropertyAsString(node, PROP_TITLE));

            NodeIterator it = node.getNodes();
            while (it.hasNext()) {
                Node n = it.nextNode();

                //Node tipine göre doğru conversion.
                if (n.isNodeType(NODE_FOLDER)) {
                    result.getItems().add(nodeToRafFolder(n));
                } else if (n.isNodeType(NODE_FILE)) {
                    result.getItems().add(nodeToRafDocument(n));
                }
            }

            //FIXME: Debug için duruyor kalkacak.
            JcrTools jcrTools = new JcrTools();
            jcrTools.setDebug(true);
            jcrTools.printSubgraph(node);

            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException(ex);
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
            String expression = "SELECT * FROM [" + MIXIN_TAGGABLE + "] WHERE ["+ PROP_CATEGORY_PATH + "] = '" + categoryPath + "' AND  ISDESCENDANTNODE('" + rootPath + "')";
            
            Query query = queryManager.createQuery(expression, Query.JCR_SQL2);
            QueryResult queryResult = query.execute();

            NodeIterator it = queryResult.getNodes();
            while (it.hasNext()) {
                Node n = it.nextNode();

                //Node tipine göre doğru conversion.
                if (n.isNodeType(NODE_FOLDER)) {
                    result.getItems().add(nodeToRafFolder(n));
                } else if (n.isNodeType(NODE_FILE)) {
                    result.getItems().add(nodeToRafDocument(n));
                }
            }

        } catch (RepositoryException ex) {
            throw new RafException(ex);
        }

        return result;
    }
    
    
    public RafCollection getTagCollection( String tag, String rootPath ) throws RafException {
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
            String expression = "SELECT * FROM [" + MIXIN_TAGGABLE + "] WHERE ["+ PROP_TAG + "] = '" + tag + "' AND  ISDESCENDANTNODE('" + rootPath + "')";
            
            Query query = queryManager.createQuery(expression, Query.JCR_SQL2);
            QueryResult queryResult = query.execute();

            NodeIterator it = queryResult.getNodes();
            while (it.hasNext()) {
                Node n = it.nextNode();

                //Node tipine göre doğru conversion.
                if (n.isNodeType(NODE_FOLDER)) {
                    result.getItems().add(nodeToRafFolder(n));
                } else if (n.isNodeType(NODE_FILE)) {
                    result.getItems().add(nodeToRafDocument(n));
                }
            }

        } catch (RepositoryException ex) {
            throw new RafException(ex);
        }

        return result;
    }
    

    public void createFolder(RafFolder folder) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(folder.getPath());

            JcrTools jcrTools = new JcrTools();
            Node node = jcrTools.findOrCreateNode(session, fullPath, NODE_FOLDER);

            node.addMixin(MIXIN_TITLE);
            node.addMixin(MIXIN_TAGGABLE);

            node.setProperty(PROP_TITLE, folder.getTitle());
            node.setProperty(PROP_DESCRIPTON, folder.getInfo());

            session.save();
            session.logout();

        } catch (RepositoryException ex) {
            throw new RafException();
        }
    }

    public RafDocument uploadDocument(String fileName, InputStream in) throws RafException {
        if (Strings.isNullOrEmpty(fileName)) {
            //FIXME: UI'a hata vermeli ama nasıl?
            throw new RafException();
        }

        RafDocument result = null;

        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            JcrTools jcrTools = new JcrTools();

            String fullName = getEncodedPath(fileName);
            LOG.debug("Encoded FileName : {}", fileName);

            Node n = jcrTools.uploadFile(session, fullName, in);

            n.addMixin(MIXIN_TITLE);
            n.addMixin(MIXIN_TAGGABLE);
            n.addMixin("raf:metadata");

            String[] fa = fileName.split("/");
            n.setProperty(PROP_TITLE, fa[fa.length - 1]);

            session.save();

            //FIXME: Bazı durumlarda upload sırasında mimeType bulunamıyor. Bu durumda null gelmesi yerine "raf/binary atadık. Buna daha iyi bir çözüm lazım.
            Node nc = n.getNode("jcr:content");
            String mimeType = getPropertyAsString(nc, "jcr:mimeType");
            if (Strings.isNullOrEmpty(mimeType)) {
                nc.setProperty("jcr:mimeType", "raf/binary");
            }

            session.save();

            //n.getProperty("jcr:createdBy").setValue(getUserId());
            //session.save();
            result = null; //nodeToFile(n);
            session.logout();
            LOG.debug("Dosya JCR'e kondu : {}", fullName);
        } catch (RepositoryException ex) {
            LOG.error("Reporsitory Exception", ex);
            throw new RafException(ex);
        } catch (IOException ex) {
            LOG.error("IO Exception", ex);
            throw new RafException(ex);
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
                throw new RafException();
            }

            if (node.isNodeType(NODE_FOLDER)) {
                result = nodeToRafFolder(node);
            } else if (node.isNodeType(NODE_FILE)) {
                result = nodeToRafDocument(node);
            } else {
                //FIXME:Bilinen bir node bulunamadı.
                throw new RafException();
            }

            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException();
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
                //FIXME: bu exception nedir söylemek lazım.
                throw new RafException();
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
            node.setProperty(PROP_TAG, object.getTags().toArray(new String[] {}));
            //node.setProperty(PROP_TAG, "");

            session.save();
            session.logout();

        } catch (RepositoryException ex) {
            throw new RafException();
        }
    }

    public void saveMetadata(String id, RafMetadata metadata) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            Node metaNode = null;

            //Demek ki ilk kez yazılacak.
            if (Strings.isNullOrEmpty(metadata.getNodeId())) {
                Node node = session.getNodeByIdentifier(id);

                if (node == null) {
                    //FIXME: bu exception nedir söylemek lazım.
                    throw new RafException();
                }

                metaNode = node.addNode(metadata.getType(), metadata.getType());
            } else {
                //MetadataNode var update için kendisini bulalım
                metaNode = session.getNodeByIdentifier(metadata.getNodeId());

                if (metaNode == null) {
                    //FIXME: bu exception nedir söylemek lazım.
                    throw new RafException();
                }
            }

            MetadataConverter converter = MetadataConverterRegistery.getConverter(metadata.getType());

            converter.modelToNode(metadata, metaNode);

            session.save();

            session.logout();

        } catch (RepositoryException ex) {
            throw new RafException();
        }
    }

    public InputStream getDocumentContent(String id) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            Node node = session.getNodeByIdentifier(id);

            LOG.debug("Document Content Requested: {}", node.getPath());

            Node content = node.getNode("jcr:content");

            //FIXME: Burada böyle bi rtakla gerçekten lazım mı? Bütün veriyi memory'e okumak dert olcaktır...
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(content.getProperty("jcr:data").getBinary().getStream(), bos);

            session.logout();

            ByteArrayInputStream result = new ByteArrayInputStream(bos.toByteArray());

            return result;

        } catch (RepositoryException | IOException ex) {
            LOG.error("RAfException", ex);
            throw new RafException(ex);
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

            JcrTools jcrTools = new JcrTools();
            jcrTools.removeAllChildren(node);
            node.remove();

            session.save();
            session.logout();

        } catch (RepositoryException ex) {
            throw new RafException(ex);
        }

    }

    public void copyObject(RafObject from, RafFolder to) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            //FIXME: burada hedef ismin olup olmadığı kontrol edilecek. Varsa isimde (1) gibi ekler yapılacak.
            //FIXME: url encoding
            copy(session.getWorkspace(), from.getPath(), targetPath(session, from, to.getPath()));

            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException(ex);
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

            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException(ex);
        }
    }

    public void moveObject(RafObject from, RafFolder to) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            //FIXME: burada hedef ismin olup olmadığı kontrol edilecek. Varsa isimde (1) gibi ekler yapılacak.
            //FIXME: url encoding
            move(session.getWorkspace(), from.getPath(), targetPath(session, from, to.getPath()));

            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException(ex);
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

            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException(ex);
        }
    }

    protected String targetPath(Session session, RafObject o, String targetBase) throws RepositoryException, RafException {
        //jcrTools.findOrCreateNode(session, PROP_TAG)
        //Önce folder var mı bakalım yoksa yoksa zaten exception ile çıkacağız.
        Node folderNode = session.getNode(targetBase);

        //Node tipi folder değil ise hata verelim. Bunun üzerine kopyalama yapamayız
        if (!folderNode.isNodeType(NODE_FOLDER)) {
            //FIXME: doğru bir hata üretelim.
            throw new RafException();
        }

        String result = targetBase + "/" + o.getName();

        try {
            Node targetNode = session.getNode(result);
        } catch (PathNotFoundException e) {
            //Beklenen durum. Node yok ve doğru isimle geri döneceğiz. Aksi halde devam edip isim değiştireceğiz.
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

        JcrTools jcrTools = new JcrTools();
        jcrTools.printSubgraph(node);

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

        if (node.isNodeType(MIXIN_TAGGABLE)) {
            //result.setInfo(node.getProperty("raf:tags").getString());
            result.setCategory(getPropertyAsString(node, PROP_CATEGORY));
            result.setCategoryPath(getPropertyAsString(node, PROP_CATEGORY_PATH));
            result.setCategoryId(getPropertyAsLong(node, PROP_CATEGORY_ID));
            result.setTags(getPropertyAsStringList(node, PROP_TAG));
        }

        return result;
    }

    protected RafDocument nodeToRafDocument(Node node) throws RepositoryException, RafException {
        RafDocument result = new RafDocument();

        JcrTools jcrTools = new JcrTools();
        jcrTools.printSubgraph(node);

        result.setId(node.getIdentifier());
        result.setPath(node.getPath());
        result.setName(node.getName());
        result.setParentId(node.getParent().getIdentifier());

        //bilenen diğer metadata ( createDate v.b. ) toplanmalı
        result.setCreateBy(node.getProperty("jcr:createdBy").getString());
        result.setCreateDate(node.getProperty("jcr:created").getDate().getTime());

        //FIXME: TIKA olmadığı için mimeType bulmada sorun olabilir.
        Node cn = node.getNode("jcr:content");
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

        if (node.isNodeType(MIXIN_TAGGABLE)) {
            //result.setInfo(node.getProperty("raf:tags").getString());
            result.setCategory(getPropertyAsString(node, PROP_CATEGORY));
            result.setCategoryPath(getPropertyAsString(node, PROP_CATEGORY_PATH));
            result.setCategoryId(getPropertyAsLong(node, PROP_CATEGORY_ID));
            result.setTags(getPropertyAsStringList(node, PROP_TAG));
        }

        NodeIterator it = node.getNodes("*:metadata");
        while (it.hasNext()) {
            Node mn = it.nextNode();
            MetadataConverter mc = MetadataConverterRegistery.getConverter(mn.getPrimaryNodeType().getName());
            result.getMetadatas().add(mc.nodeToModel(mn));
        }

        return result;
    }

    private void populateFolders(Node node, List<RafFolder> result) throws RepositoryException {
        NodeIterator it = node.getNodes();
        while (it.hasNext()) {
            Node n = it.nextNode();
            if (n.isNodeType(NODE_FOLDER)) {
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
    private List<String> getPropertyAsStringList(Node node, String prop) throws RepositoryException {

        List<String> result = new ArrayList<>();
        
        try {
            Property property = node.getProperty(prop);

            if (property != null) {
                Value[] vls = property.getValues();
                for( Value v : vls ){
                    result.add( v.getString());
                }
            }
        } catch (PathNotFoundException ex) {
            //Aslında yapacak bişi yok. Attribute olmayabilir o zaman geriye null döneceğiz.
            LOG.debug("Property not found : {}", prop);
        }

        return result;
    }

}
