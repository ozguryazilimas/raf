package com.ozguryazilim.raf.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;

/**
 *
 * @author oyas
 */
@Folder( name = "/metadatapanels")
@ApplicationScoped
public interface MetadataPanelPages extends Pages{
 
    @SecuredPage
    class DefaultMetadataPanel implements MetadataPanelPages {}
    
    @SecuredPage
    class DynaFormMetadataPanel implements MetadataPanelPages {}
    
    @SecuredPage
    class BasicMetadataPanel implements MetadataPanelPages {}
    
    @SecuredPage
    class BasicMetadataEditorDialog implements MetadataPanelPages {}
}
