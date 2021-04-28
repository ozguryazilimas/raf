package com.ozguryazilim.raf.emaildoc.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;

/**
 *
 * @author oyas
 */
@Folder(name = "/emaildoc")
@ApplicationScoped
public interface EmailDocPages extends Pages {

    @SecuredPage
    class EmailDocMetadataPanel implements EmailDocPages {}

}
