/*
 * 
 * 
 * 
 */
package com.ozguryazilim.raf.imports;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.View;

/**
 *
 * @author xxx mailto:xx
 */
@Folder(name = "/rafcommands")
@ApplicationScoped
public interface RafCommandPages extends Pages {

    @SecuredPage
    @View
    public class FileImporterCommandEditor implements Admin {
    };
}
