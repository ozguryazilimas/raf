/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.ozguryazilim.raf.models.RafMetadata;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

/**
 *
 * @author oyas
 */
public class DefaultMetadataConverter implements MetadataConverter{

    @Override
    public RafMetadata nodeToModel(Node node) throws RafException {
        
        try {
            RafMetadata result = new RafMetadata();
            //node.isNodeType("*:metadata")
            
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
