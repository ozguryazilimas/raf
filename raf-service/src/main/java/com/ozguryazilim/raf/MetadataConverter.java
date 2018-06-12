/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
