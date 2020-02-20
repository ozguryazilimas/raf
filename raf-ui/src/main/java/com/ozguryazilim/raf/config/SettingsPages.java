package com.ozguryazilim.raf.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.nav.AdminNavigationSection;
import com.ozguryazilim.telve.nav.Navigation;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;

/**
 *
 * @author oyas
 */
@Folder( name = "/settings")
@ApplicationScoped
public interface SettingsPages extends Pages{
    
    @SecuredPage("rafDefinitionAdmin")
    @Navigation(icon = "fa fa-archive", section = AdminNavigationSection.class)
    class RafDefinitionBrowse implements SettingsPages {};
    
    @SecuredPage("rafSettings")
    class RafSettings implements SettingsPages {};
    
    @SecuredPage("rafMembers")
    class RafMembers implements SettingsPages {};
           
}
