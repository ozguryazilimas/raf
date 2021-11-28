package com.ozguryazilim.raf.wopi.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.View;

/**
 *
 * @author hakan
 */
@Folder(name = "/wopi")
@ApplicationScoped
public interface WopiPages extends Pages {
    
    @View(navigation = View.NavigationMode.REDIRECT, viewParams = View.ViewParameterMode.INCLUDE)
    @SecuredPage
    class OnlineEditor implements Pages {
    }
}
