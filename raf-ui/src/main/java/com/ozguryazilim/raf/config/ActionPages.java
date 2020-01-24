package com.ozguryazilim.raf.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;

/**
 *
 * @author oyas
 */
@Folder(name = "/actions")
@ApplicationScoped
public interface ActionPages extends Pages {

    @SecuredPage
    class CreateFolderDialog implements Pages {
    }

    @SecuredPage
    class RafObjectMembers implements Pages {
    };

}
