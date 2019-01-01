package com.ozguryazilim.raf.pdf.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;

/**
 *
 * @author oyas
 */
@Folder( name = "/pdf")
@ApplicationScoped
public interface PdfPages extends Pages{
    
    @SecuredPage
    class PdfPreviewPanel implements PdfPages {}
    
    @SecuredPage
    class PdfMetadataPanel implements PdfPages {}
}
