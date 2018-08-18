/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.preview;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import org.imgscalr.Scalr;
import org.modeshape.common.util.CheckArg;
import org.modeshape.jcr.api.sequencer.Sequencer;

/**
 * Raf'a yüklenen görsellerden preview amacıyla scale edilmiş sürümler üretir.
 * 
 * @author Hakan Uygun
 */
public class ImagePreviewSequencer extends Sequencer{

    @Override
    public boolean execute(Property inputProperty, Node outputNode, Context context) throws Exception {
        
        Binary binaryValue = inputProperty.getBinary();
        CheckArg.isNotNull(binaryValue, "binary");
        //Node sequencedNode = getPdfMetadataNode(outputNode);
    
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        BufferedImage src = ImageIO.read(binaryValue.getStream());
        BufferedImage scaledImg = Scalr.resize(src, Scalr.Method.BALANCED, 480, 320, Scalr.OP_ANTIALIAS);
        ImageIO.write(scaledImg, "png", os);
        
        src.flush();
        scaledImg.flush();
        
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        
        Node previewNode = getPreviewNode(outputNode);
        
        Binary preview = outputNode.getSession().getValueFactory().createBinary(is);
        previewNode.setProperty("jcr:mimeType", "image/png");
        previewNode.setProperty("jcr:data", preview);
        
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
