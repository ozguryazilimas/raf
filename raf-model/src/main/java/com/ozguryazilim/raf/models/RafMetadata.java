/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author oyas
 */
public class RafMetadata implements Serializable{
    
    /**
     * Metadata JCR NodeType
     * Örnek image:metadata, einvoice:metadata
     */
    String type;
    String nodeId;
    
    /**
     * JCR'den aktarılırken node üzerinden attributelar çevrilir.
     */
    Map<String, Object> attributes = new HashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    
}
