package com.ozguryazilim.raf.jbpm.config;

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
@Folder( name = "/bpm")
@ApplicationScoped
public interface BpmPages extends Pages{
    
    @SecuredPage
    class TaskConsole implements BpmPages {}
    
    @SecuredPage
    class ProcessConsole implements BpmPages {}
    
    @SecuredPage("Deployment")
    @Navigation( icon = "fa fa-upload", label = "Deployment", section = AdminNavigationSection.class)
    class DeploymentConsole implements BpmPages {}
    
    @SecuredPage
    class ProcessDialog implements BpmPages {}
}
