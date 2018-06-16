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
@Folder( name = "/sidepanels")
@ApplicationScoped
public interface SidePanelPages extends Pages{
    
    @SecuredPage
    class FolderSidePanel implements SidePanelPages {}
    
    @SecuredPage
    class CategorySidePanel implements SidePanelPages {}
    
    @SecuredPage
    class TagsSidePanel implements SidePanelPages {}
}
