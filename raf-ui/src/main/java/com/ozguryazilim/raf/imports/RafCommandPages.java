package com.ozguryazilim.raf.imports;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.View;

@Folder(name = "/rafcommands")
@ApplicationScoped
public interface RafCommandPages extends Pages {

    @SecuredPage
    @View
    public class FileImporterCommandEditor implements Admin {
    };

    @SecuredPage
    @View
    public class FolderDivideByYearsCommandEditor implements Admin {
    };

    @SecuredPage
    @View
    public class ElasticSearchExporterCommandEditor implements Admin {
    };

    @SecuredPage
    @View
    public class IndexRemoverCommandEditor implements Admin {
    };

    @SecuredPage
    @View
    public class EMailFetchCommandEditor implements Admin {
    };

    @SecuredPage
    @View
    public class RegeneratePreviewCommandEditor implements Admin {
    };

    @SecuredPage
    @View
    public class CleanupExpiredUploadsCommandEditor implements Admin {
    };

    @SecuredPage
    @View
    public class CleanupOutdatedSharingsCommandEditor implements Admin {
    };

    @SecuredPage
    @View
    public class CheckMissingContentsCommandEditor implements Admin {
    };

    @SecuredPage
    @View
    public class ReindexManagerCommandEditor implements Admin {
    };
}
