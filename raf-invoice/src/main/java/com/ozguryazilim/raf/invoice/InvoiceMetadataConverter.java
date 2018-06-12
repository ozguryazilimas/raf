/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.invoice;

import com.ozguryazilim.raf.MetadataConverter;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.models.RafMetadata;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

/**
 * invoice:metadata node type'ı RafMetadata'ya dönüştürür.
 * 
 * @author Hakan Uygun
 */
public class InvoiceMetadataConverter implements MetadataConverter{

    @Override
    public RafMetadata nodeToModel(Node node) throws RafException {
        try {
            RafMetadata result = new RafMetadata();
            
            if( !node.isNodeType("invoice:metadata") ){
                //FIXME: Daha düzgün bir hata çevir.
                throw new RafException();
            }
            
            result.setNodeId(node.getIdentifier());
            result.setType( node.getPrimaryNodeType().getName());
            
            PropertyIterator it = node.getProperties();
            while( it.hasNext() ){
                Property p = it.nextProperty();
                if( !p.getName().startsWith("jcr:")){
                    //FIXME: Veri tipi kontrolü ile doğru bir şekilde aktarılmalı.
                    result.getAttributes().put( p.getName(), p.getValue().getString());
                }
                
            }
            
            return result;
        } catch (RepositoryException ex) {
            throw new RafException(ex);
        }
    }

    @Override
    public void modelToNode(RafMetadata data, Node node) throws RafException {
        
        try {
            node.setProperty("invoice:account", (String) data.getAttributes().get("invoice:account"));
            node.setProperty("invoice:serial", (String) data.getAttributes().get("invoice:serial"));
            node.setProperty("invoice:taxOffice", (String) data.getAttributes().get("invoice:taxOffice"));
            node.setProperty("invoice:taxNumber", (String) data.getAttributes().get("invoice:taxNumber"));
            //Calendar tipini nasıl setliyoruz ki?
            //node.setProperty("invoice:date", ((Date) data.getAttributes().get("invoice:date")));
            
        } catch (VersionException ex) {
            throw new RafException(ex);
        } catch (LockException ex) {
            throw new RafException(ex);
        } catch (ConstraintViolationException ex) {
            throw new RafException(ex);
        } catch (RepositoryException ex) {
            throw new RafException(ex);
        }
        
    }
    
}
