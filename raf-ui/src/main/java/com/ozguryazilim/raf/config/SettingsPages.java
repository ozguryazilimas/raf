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
@Folder( name = "/settings")
@ApplicationScoped
public interface SettingsPages extends Pages{
    
    @SecuredPage("rafDefinitionAdmin")
    @Navigation(icon = "fa fa-archive", section = AdminNavigationSection.class)
    class RafDefinitionBrowse implements SettingsPages {};

    @SecuredPage()
    class RafSettings implements SettingsPages {};
    
    @SecuredPage()
    class RafMembers implements SettingsPages {};
    
    @SecuredPage("userRole") @View 
    class UserRafListSubView implements Admin {};

    @SecuredPage @View
    class NotificationOptionPane implements Admin {};

    @SecuredPage @View
    class RafUserRolesOptionPane implements Admin {};

    @SecuredPage @View
    class DefaultSearchOptionPane implements Admin {};

    @SecuredPage @View
    class CollectionOptionPane implements Admin {};

    @SecuredPage @View
    class ReadOnlyModeOptionPane implements Admin {};

    @SecuredPage @View
    class ConfigurationPropertiesOptionPane implements Admin {};

}
