/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jcr;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafNode;
import javax.jcr.Node;
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
public class RafModeshapeRepository implements RafRepository{

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
    public RafNode createRafNode(RafDefinition definition) throws RafException{
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath( RAF_ROOT + definition.getCode() );

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
    public RafNode getRafNode(String code) throws RafException{
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath( RAF_ROOT + code );

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
    public RafNode getPrivateRafNode(String username) throws RafException{
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath( PRIVATE_ROOT + username );

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
    public RafNode getSharedRafNode() throws RafException{
        try {
            Session session = ModeShapeRepositoryFactory.getSession();

            String fullPath = getEncodedPath( SHARED_ROOT );

            JcrTools jcrTools = new JcrTools();
            Node node = jcrTools.findOrCreateNode(session, fullPath, "nt:folder");

            RafNode result = nodeToRafNode(node);

            session.logout();

            return result;
        } catch (RepositoryException ex) {
            throw new RafException();
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
 
    protected RafNode nodeToRafNode( Node node ) throws RepositoryException{
        RafNode result = new RafNode();
        
        result.setId(node.getIdentifier());
        result.setPath(node.getPath());
        result.setName(node.getName());
        
        return result;
    }
    
}
