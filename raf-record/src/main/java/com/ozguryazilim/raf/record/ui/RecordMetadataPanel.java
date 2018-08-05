/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.record.ui;

import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.record.RecordTypeManager;
import com.ozguryazilim.raf.record.config.RecordPages;
import com.ozguryazilim.raf.record.model.RafRecordDocumentType;
import com.ozguryazilim.raf.record.model.RafRecordType;
import com.ozguryazilim.raf.ui.base.AbstractMetadataPanel;
import com.ozguryazilim.raf.ui.base.MetadataPanel;
import javax.inject.Inject;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@MetadataPanel(type = "nt:file", view = RecordPages.RecordMetadataPanel.class, order = 0)
public class RecordMetadataPanel extends AbstractMetadataPanel{
    private static final Logger LOG = LoggerFactory.getLogger(RecordMetadataPanel.class);
    
    @Inject
    private RecordTypeManager recordTypeManager;
    
    @Inject
    private RuntimeDataService runtimeDataService;
    
    private RafRecord object;
    private String recordType;
    private String documentType;
    private ProcessInstanceDesc processInstanceDesc;

    public RafRecord getObject() {
        return object;
    }

    public void setObject(RafRecord object) {
        this.object = object;
        
        RafRecordType type = recordTypeManager.getRecordType(object.getRecordType());
        recordType = type.getTitle();
        for( RafRecordDocumentType dt : type.getDocumentTypes() ){
            if( dt.getName().equals(object.getDocumentType())){
                documentType = dt.getTitle();
                break;
            }
        }
        
        processInstanceDesc = runtimeDataService.getProcessInstanceById(getObject().getProcessIntanceId());
    }

    public String getRecordType() {
        return recordType;
    }

    public String getDocumentType() {
        return documentType;
    }

    public ProcessInstanceDesc getProcessInstanceDesc() {
        return processInstanceDesc;
    }
    
}
