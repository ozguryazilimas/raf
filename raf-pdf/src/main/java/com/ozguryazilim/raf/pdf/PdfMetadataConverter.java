package com.ozguryazilim.raf.pdf;

import com.ozguryazilim.raf.MetadataConverter;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.models.RafMetadata;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

/**
 * PDF Sequencer ile üretilen pdf:metadata nodeType'ı RafMetadata'ya çevirir.
 * @author Hakan Uygun
 */
public class PdfMetadataConverter implements MetadataConverter{

    @Override
    public RafMetadata nodeToModel(Node node) throws RafException {
        try {
            RafMetadata result = new RafMetadata();
            //FIXME: burada pdf:metadata tipinde olup olmadığı kontrol edilcek
            if( !node.isNodeType("pdf:metadata") ){
                //FIXME: Daha düzgün bir hata çevir.
                throw new RafException("[RAF-0032] Raf Node is not Metatata node");
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
                
                //FIXME: pdf:page meta tiplerinden bir toparlama yapmak lazım.
            }
            
            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0030] Raf Metatata Convert Error", ex);
        }
    }

    @Override
    public void modelToNode(RafMetadata data, Node node) throws RafException {
        //Bu metada update edilmeyecek dolayısı ile doğrudan exception fırlatalım.
        //ReadOnly Metadata pdf:metadata şeklinde
        throw new RafException("[RAF-0031] Raf Metatata read only");
    }
    
}
