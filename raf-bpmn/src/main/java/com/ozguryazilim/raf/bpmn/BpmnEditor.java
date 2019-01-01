package com.ozguryazilim.raf.bpmn;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FIXME: Şimdilik sadece input hidden üzerinden text olarak veri alıyoruz. Normalde bu bir file Upload işlemi olmalı.
 * 
 * @author oyas
 */
@RequestScoped
@Named
public class BpmnEditor implements Serializable{
    
    private static final Logger LOG = LoggerFactory.getLogger(BpmnEditor.class);
    
    @Inject
    private RafService rafService;
    
    private String bpmnText;
    private String objectId;

    public String getBpmnText() {
        return bpmnText;
    }

    public void setBpmnText(String bpmnText) {
        this.bpmnText = bpmnText;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
   
    
    
    public void save() throws RafException, UnsupportedEncodingException{
        
        RafObject object = rafService.getRafObject(objectId);
        
        InputStream in = new ByteArrayInputStream(bpmnText.getBytes("UTF-8"));
        
        rafService.uploadDocument(object.getPath(), in);
    }
    
}
