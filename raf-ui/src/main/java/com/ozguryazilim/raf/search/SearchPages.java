package com.ozguryazilim.raf.search;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.View;

/**
 *
 * @author oyas
 */
@Folder(name = "/search")
@ApplicationScoped
public interface SearchPages extends Pages {

    @SecuredPage()
    @View
    class SearchPage implements SearchPages {
    };
}
