package com.ozguryazilim.raf.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.nav.AdminNavigationSection;
import com.ozguryazilim.telve.nav.Navigation;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.View;

@Folder(name = "/departments")
@ApplicationScoped
public interface RafDepartmentPages extends Pages {

    @SecuredPage("rafDepartment")
    @View
    @Navigation(icon = "fa fa-sitemap", section = AdminNavigationSection.class, order = 22)
    class RafDepartment implements RafDepartmentPages {
    };

    @SecuredPage()
    @View
    class RafDepartmentLookup implements RafDepartmentPages {
    };
}
