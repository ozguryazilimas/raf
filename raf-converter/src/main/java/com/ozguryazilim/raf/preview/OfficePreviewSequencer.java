/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.preview;

import com.ozguryazilim.raf.jod.PDFPreviewConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.jcr.Binary;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import org.modeshape.common.util.CheckArg;
import org.modeshape.jcr.api.nodetype.NodeTypeManager;
import org.modeshape.jcr.api.sequencer.Sequencer;

/**
 *
 * @author oyas
 */
public class OfficePreviewSequencer extends Sequencer{

    @Override
    public void initialize(NamespaceRegistry registry, NodeTypeManager nodeTypeManager) throws RepositoryException, IOException {
        super.registerNodeTypes("/preview.cnd", nodeTypeManager, true);
    }

    
    
    @Override
    public boolean execute(Property inputProperty, Node outputNode, Context context) throws Exception {
        
        Binary binaryValue = inputProperty.getBinary();
        CheckArg.isNotNull(binaryValue, "binary");
        //Node sequencedNode = getPdfMetadataNode(outputNode);
        
        PDFPreviewConverter converter = new PDFPreviewConverter();
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        String mimeType = inputProperty.getParent().getProperty("jcr:mimeType").getString();
        
        converter.convert(binaryValue.getStream(), mimeType, os);
        
        Node previewNode = getPreviewNode(outputNode);
        
        
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        
        Binary previewPfd = outputNode.getSession().getValueFactory().createBinary(is);
        previewNode.setProperty("jcr:mimeType", "application/pdf");
        previewNode.setProperty("jcr:data", previewPfd);
        
        //Yapılan değişiklikler kayıt edilsin.
        return true;
        
    }
    
    private Node getPreviewNode( Node outputNode ) throws RepositoryException{
        if( outputNode.isNew() ){
            outputNode.setPrimaryType("raf:preview");
            return outputNode;
        }
        
        return outputNode.addNode("raf:preview", "raf:preview");
    }
    
}
