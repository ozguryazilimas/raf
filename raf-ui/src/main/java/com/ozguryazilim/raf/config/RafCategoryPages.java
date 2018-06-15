/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.nav.AdminNavigationSection;
import com.ozguryazilim.telve.nav.Navigation;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.View;

/**
 *
 * @author oyas
 */
@Folder( name = "/categories")
@ApplicationScoped
public interface RafCategoryPages extends Pages{
    
    @SecuredPage("rafCategory") @View @Navigation(icon = "fa fa-sitemap", section = AdminNavigationSection.class, order = 22)
    class RafCategory implements RafCategoryPages {};
    
    @SecuredPage() @View 
    class RafCategoryLookup implements RafCategoryPages {};
}
