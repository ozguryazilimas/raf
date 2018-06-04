/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jcr;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafMimeTypes;
import com.ozguryazilim.raf.models.RafNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.modeshape.common.text.UrlEncoder;
import org.modeshape.jcr.api.JcrTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
public class RafModeshapeRepository implements RafRepository {

    private static final Logger LOG = LoggerFactory.getLogger(RafModeshapeRepository.class);

    private static final String PRIVATE_ROOT = "/PRIVATE/";
    private static final String SHARED_ROOT = "/SHARED";
    private static final String RAF_ROOT = "/RAF/";

    private UrlEncoder encoder;

    @Override
    public void start() throws RafException {
        try {
            encoder = new UrlEncoder();
            encoder.setSlashEncoded(false);

            //Engine'de başlatılsın
            Session session = ModeShapeRepositoryFactory.getSession();
            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException();
        }
    }

    @Override
    public void stop() throws RafException {
        ModeShapeRepositoryFactory.shutdown();
    }

    @Override
    public RafNode createRafNode(RafDefinition definition) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(RAF_ROOT + definition.getCode());

            JcrTools jcrTools = new JcrTools();
            Node node = jcrTools.findOrCreateNode(session, fullPath, "nt:folder");

            RafNode result = nodeToRafNode(node);

            //FIXME: şimdilik test işleri için var. Silinecek.
            node = jcrTools.findOrCreateNode(session, fullPath + "/dene/abc/def", "nt:folder");

            session.save();
            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException();
        }
    }

    @Override
    public RafNode getRafNode(String code) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(RAF_ROOT + code);

            JcrTools jcrTools = new JcrTools();
            Node node = jcrTools.findOrCreateNode(session, fullPath, "nt:folder");

            RafNode result = nodeToRafNode(node);

            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException();
        }
    }

    @Override
    public RafNode getPrivateRafNode(String username) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(PRIVATE_ROOT + username);

            JcrTools jcrTools = new JcrTools();
            Node node = jcrTools.findOrCreateNode(session, fullPath, "nt:folder");

            RafNode result = nodeToRafNode(node);

            session.save();
            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException();
        }

    }

    @Override
    public RafNode getSharedRafNode() throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(SHARED_ROOT);

            JcrTools jcrTools = new JcrTools();
            Node node = jcrTools.findOrCreateNode(session, fullPath, "nt:folder");

            RafNode result = nodeToRafNode(node);

            session.save();
            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException();
        }
    }

    @Override
    public List<RafFolder> getFolderList(RafNode rafNode) throws RafException {
        return getFolderList(rafNode.getName());
    }

    @Override
    public List<RafFolder> getFolderList(String rafCode) throws RafException {

        List<RafFolder> result = new ArrayList<>();

        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(RAF_ROOT + rafCode);

            JcrTools jcrTools = new JcrTools();
            Node node = jcrTools.findOrCreateNode(session, fullPath, "nt:folder");

            //Root'u ekleyecek miyiz? Aslında bu bir RafNode ama aynı zamanda bir folder.
            //RootNode'un parentId'sini saklıyoruz. Ayrıca # ile UI tarafında ağaç da düzgün olacak.
            RafFolder f = nodeToRafFolder(node);
            f.setParentId("#");
            result.add(f);

            populateFolders(node, result);

            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException();
        }

    }

    @Override
    public RafCollection getCollection(String path) throws RafException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
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
                //FIXME: node tipine göre farklı raf nesnelerine dönüştürülmeli.
                //node.getPrimaryNodeType().getName()
                result.getItems().add(nodeToRafFolder(n));
            }

            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException();
        }

    }

    @Override
    public void createFolder(RafFolder folder) throws RafException {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath(folder.getPath());

            JcrTools jcrTools = new JcrTools();
            Node node = jcrTools.findOrCreateNode(session, fullPath, "nt:folder");

            session.save();
            session.logout();

        } catch (RepositoryException ex) {
            throw new RafException();
        }
    }

    @Override
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
            throw new RafException();
        } catch (IOException ex) {
            LOG.error("IO Exception", ex);
            throw new RafException();
        }
        return result;
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

        return result;
    }

    private void populateFolders(Node node, List<RafFolder> result) throws RepositoryException {
        NodeIterator it = node.getNodes();
        while (it.hasNext()) {
            Node n = it.nextNode();
            result.add(nodeToRafFolder(n));
            populateFolders(n, result);
        }

    }

}
