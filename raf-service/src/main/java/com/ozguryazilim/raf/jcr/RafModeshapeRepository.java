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
public class RafModeshapeRepository  implements Serializable{

    private static final Logger LOG = LoggerFactory.getLogger(RafModeshapeRepository.class);

    private static final String PRIVATE_ROOT = "/PRIVATE/";
    private static final String SHARED_ROOT = "/SHARED";
    private static final String RAF_ROOT = "/RAF/";

    
    private static final String NODE_FOLDER = "nt:folder";
    private static final String NODE_FILE = "nt:file";
    
    private static final String MIXIN_TITLE = "mix:title";
    private static final String MIXIN_TAGGABLE = "raf:taggable";
    
    private static final String PROP_TITLE = "jcr:title";
    private static final String PROP_DESCRIPTON = "jcr:description";
    private static final String PROP_CATEGORY = "raf:category";
    private static final String PROP_TAG = "raf:tags";
    
    private UrlEncoder encoder;

    
    @PostConstruct
    public void init(){
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
            throw new RafException( ex );
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

            RafNode result = nodeToRafNode(node);

            //FIXME: şimdilik test işleri için var. Silinecek.
            node = jcrTools.findOrCreateNode(session, fullPath + "/dene/abc/def", NODE_FOLDER);

            session.save();
            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException( ex );
        }
    }

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
            throw new RafException( ex );
        }
    }

    public RafNode getPrivateRafNode(String username) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(PRIVATE_ROOT + username);

            JcrTools jcrTools = new JcrTools();
            Node node = jcrTools.findOrCreateNode(session, fullPath, NODE_FOLDER);

            RafNode result = nodeToRafNode(node);

            session.save();
            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException( ex );
        }

    }

    public RafNode getSharedRafNode() throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(SHARED_ROOT);

            JcrTools jcrTools = new JcrTools();
            Node node = jcrTools.findOrCreateNode(session, fullPath, NODE_FOLDER);

            RafNode result = nodeToRafNode(node);

            session.save();
            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException( ex );
        }
    }

    public List<RafFolder> getFolderList(RafNode rafNode) throws RafException {
        return getFolderList(rafNode.getName());
    }

    public List<RafFolder> getFolderList(String rafCode) throws RafException {

        List<RafFolder> result = new ArrayList<>();

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(RAF_ROOT + rafCode);

            JcrTools jcrTools = new JcrTools();
            Node node = jcrTools.findOrCreateNode(session, fullPath, NODE_FOLDER);

            //Root'u ekleyecek miyiz? Aslında bu bir RafNode ama aynı zamanda bir folder.
            //RootNode'un parentId'sini saklıyoruz. Ayrıca # ile UI tarafında ağaç da düzgün olacak.
            RafFolder f = nodeToRafFolder(node);
            f.setParentId("#");
            result.add(f);

            populateFolders(node, result);

            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException( ex );
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
            result.setTitle(node.getName());

            NodeIterator it = node.getNodes();
            while (it.hasNext()) {
                Node n = it.nextNode();
                
                //Node tipine göre doğru conversion.
                if( n.isNodeType(NODE_FOLDER)){
                    result.getItems().add(nodeToRafFolder(n));
                } else if ( n.isNodeType(NODE_FILE)){
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
            throw new RafException( ex );
        }

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
            
            n.setProperty(PROP_TITLE, fileName);
            
            
            /*
            n.addMixin("tlv:ref");
            n.addMixin("tlv:tag");
            n.setProperty("tlv:sourceCaption", getSourceCaption());
            n.setProperty("tlv:sourceDomain", getSourceDomain());
            n.setProperty("tlv:sourceId", getSourceId());
             */
            session.save();

            //n.getProperty("jcr:createdBy").setValue(getUserId());
            //session.save();
            result = null; //nodeToFile(n);
            session.logout();
            LOG.debug("Dosya JCR'e kondu : {}", fullName);
        } catch (RepositoryException ex) {
            LOG.error("Reporsitory Exception", ex);
            throw new RafException( ex );
        } catch (IOException ex) {
            LOG.error("IO Exception", ex);
            throw new RafException( ex );
        }
        return result;
    }
    
    
    public RafObject getRafObject(String id) throws RafException{
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            Node node = session.getNodeByIdentifier(id);
            
            RafObject result = null;
            
            if( node == null ){
                //FIXME: bu exception nedir söylemek lazım.
                throw new RafException();
            }

            if( node.isNodeType(NODE_FOLDER)){
                result = nodeToRafFolder(node);
            } else if( node.isNodeType(NODE_FILE)){
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

    public InputStream getDocumentContent( String id ) throws RafException{
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
            throw new RafException( ex );
        }
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
        
        if( node.isNodeType(MIXIN_TITLE)){
            result.setTitle(getPropertyAsString(node, PROP_TITLE));
            result.setInfo(getPropertyAsString( node, PROP_DESCRIPTON));
        }
        
        if( node.isNodeType(MIXIN_TAGGABLE)){
            //result.setInfo(node.getProperty("raf:tags").getString());
            //result.setInfo(node.getProperty("raf:category").getString());
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
        
        //FIXME: bilenen diğer metadata ( createDate v.b. ) toplanmalı
        
        //FIXME: TIKA olmadığı için mimeType bulmada sorun olabilir.
        Node cn = node.getNode("jcr:content");
        result.setMimeType(cn.getProperty("jcr:mimeType").getString());
        
        if( node.isNodeType(MIXIN_TITLE)){
            result.setTitle(getPropertyAsString(node, PROP_TITLE));
            result.setInfo(getPropertyAsString( node, PROP_DESCRIPTON));
        }
        
        if( node.isNodeType(MIXIN_TAGGABLE)){
            //result.setInfo(node.getProperty("raf:tags").getString());
            //result.setInfo(node.getProperty("raf:category").getString());
        }
        
        NodeIterator it = node.getNodes("*:metadata");
        while( it.hasNext() ){
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
            if( n.isNodeType(NODE_FOLDER)){
                result.add(nodeToRafFolder(n));
                populateFolders(n, result);
            }
        }

    }

    /**
     * Verilen property key sonucunun null kontrolü yaparak geriye değer döndürür.
     * @param node
     * @param prop
     * @return
     * @throws RepositoryException 
     */
    private String getPropertyAsString( Node node, String prop ) throws RepositoryException{
        
        try{
        Property property = node.getProperty(prop);
        
        if( property != null ){
            return property.getString();
        }
        } catch( PathNotFoundException ex ){
            //Aslında yapacak bişi yok. Attribute olmayabilir o zaman geriye null döneceğiz.
            LOG.debug("Property not found : {}", prop);
        }
        
        return null;
    }

}
