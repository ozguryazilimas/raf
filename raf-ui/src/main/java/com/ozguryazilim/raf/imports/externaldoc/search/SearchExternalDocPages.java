package com.ozguryazilim.raf.imports.externaldoc.search;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.View;

/**
 *
 * @author oyas
 */
@Folder(name = "/externaldoc")
@ApplicationScoped
public interface SearchExternalDocPages extends Pages {

    @SecuredPage("searchExternalDocPage")
    @View
    class SearchExternalDocPage implements SearchExternalDocPages {
    };

}
