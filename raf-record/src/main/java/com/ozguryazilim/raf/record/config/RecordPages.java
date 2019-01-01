package com.ozguryazilim.raf.record.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;

/**
 *
 * @author oyas
 */
@Folder( name = "/record")
@ApplicationScoped
public interface RecordPages extends Pages{
    
    @SecuredPage
    class RecordMetadataPanel implements RecordPages {}
    
    @SecuredPage
    class RecordDialog implements RecordPages {}
    
    @SecuredPage
    class RecordViewDialog implements RecordPages {}
}
