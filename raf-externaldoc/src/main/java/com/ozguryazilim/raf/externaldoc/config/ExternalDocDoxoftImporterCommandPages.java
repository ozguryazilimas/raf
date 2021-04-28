package com.ozguryazilim.raf.externaldoc.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.View;

/**
 *
 * @author oyas
 */
@Folder(name = "/externaldoccommands")
@ApplicationScoped
public interface ExternalDocDoxoftImporterCommandPages extends Pages {

    @SecuredPage
    @View
    public class DoxoftImporterCommandEditor implements Admin {
    };
}
