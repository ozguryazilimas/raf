package com.ozguryazilim.raf.config;

import com.ozguryazilim.telve.view.Pages;
import org.apache.deltaspike.jsf.api.config.view.Folder;

import javax.enterprise.context.ApplicationScoped;

@Folder(name = "/public")
@ApplicationScoped
public interface PublicPages extends Pages {

    class DownloadPage implements PublicPages {
    }
}
