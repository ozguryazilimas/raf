/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;

/**
 *
 * @author oyas
 */
@Folder( name = "/contentpanels")
@ApplicationScoped
public interface ContentPanelPages extends Pages{
    
    @SecuredPage
    class SimpleRowViewPanel implements ContentPanelPages {}
    
    @SecuredPage
    class DetailRowViewPanel implements ContentPanelPages {}
    
    @SecuredPage
    class DocumentViewPanel implements ContentPanelPages {}
    
    @SecuredPage
    class FolderViewPanel implements ContentPanelPages {}
}
