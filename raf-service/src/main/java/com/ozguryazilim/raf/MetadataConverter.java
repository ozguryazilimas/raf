package com.ozguryazilim.raf;

import com.ozguryazilim.raf.models.RafMetadata;
import javax.jcr.Node;

/**
 *
 * @author oyas
 */
public interface MetadataConverter {
    
    RafMetadata nodeToModel( Node node ) throws RafException;
    
    void modelToNode ( RafMetadata data, Node node ) throws RafException;
}
