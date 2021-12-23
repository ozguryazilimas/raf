package com.ozguryazilim.raf.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import org.apache.deltaspike.jsf.api.config.view.Folder;

import javax.enterprise.context.ApplicationScoped;

@Folder(name = "/reader")
@ApplicationScoped
public interface ReaderPages extends Pages {

    @SecuredPage
    class PdfReaderPage implements ReaderPages {
    }

}