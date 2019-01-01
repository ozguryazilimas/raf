package com.ozguryazilim.raf.bpmn.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;

/**
 *
 * @author oyas
 */
@Folder( name = "/bpmn")
@ApplicationScoped
public interface BpmnPages extends Pages{
    
    @SecuredPage
    class BpmnPreviewPanel implements BpmnPages {}
}
