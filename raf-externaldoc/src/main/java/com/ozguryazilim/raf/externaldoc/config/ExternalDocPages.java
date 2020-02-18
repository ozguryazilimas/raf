package com.ozguryazilim.raf.externaldoc.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;

/**
 *
 * @author oyas
 */
@Folder(name = "/externaldoc")
@ApplicationScoped
public interface ExternalDocPages extends Pages {

    @SecuredPage
    class ExternalDocMetadataPanel implements ExternalDocPages {}

    @SecuredPage
    class ExternalDocAnnotationMetadataPanel implements ExternalDocPages {}
    
    @SecuredPage
    class ExternalDocMetaTagMetadataPanel implements ExternalDocPages {}
    
    @SecuredPage
    class ExternalDocWFMetadataPanel implements ExternalDocPages {}
    
    @SecuredPage
    class ExternalDocWFStepMetadataPanel implements ExternalDocPages {}
}
