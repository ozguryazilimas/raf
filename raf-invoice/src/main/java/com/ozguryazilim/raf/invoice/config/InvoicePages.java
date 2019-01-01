package com.ozguryazilim.raf.invoice.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;

/**
 *
 * @author oyas
 */
@Folder( name = "/invoice")
@ApplicationScoped
public interface InvoicePages extends Pages{
    
    @SecuredPage
    class InvoiceMetadataPanel implements InvoicePages {}
    
    @SecuredPage
    class InvoiceMetadataEditor implements InvoicePages {}
}
