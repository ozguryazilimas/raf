package com.ozguryazilim.raf.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;

/**
 *
 * @author oyas
 */
@Folder( name = "/previewpanels")
@ApplicationScoped
public interface PreviewPanelPages extends Pages{
    
    @SecuredPage
    class DefaultPreviewPanel implements PreviewPanelPages {}
    
    @SecuredPage
    class ImagePreviewPanel implements PreviewPanelPages {}
    
    @SecuredPage
    class TextPreviewPanel implements PreviewPanelPages {}
    
}
