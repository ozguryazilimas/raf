package com.ozguryazilim.raf.download;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.share.RafShareService;
import com.ozguryazilim.telve.messages.FacesMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Public dosya paylaşımı için view class
 */
@RequestScoped
@Named
public class FileDownloadView {

    private static final Logger LOG = LoggerFactory.getLogger(FileDownloadView.class);

    @Inject
    private RafShareService shareService;

    @Inject
    private FacesContext facesContext;

    @Inject
    private RafService rafService;

    private String token;
    private String password;

    public void download() {
        try {
            RafObject doc = shareService.getDocument(token, password);

            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            response.setContentType(doc.getMimeType());
            response.setHeader("Content-disposition", String.format("attachment;filename=\"%s\"", doc.getName()));

            try (OutputStream out = response.getOutputStream()) {
                rafService.getDocumentContent(doc.getId(), out);
                out.flush();
            }

            facesContext.responseComplete();
        } catch (RafException | IOException ex) {
            LOG.error("file.cannot.be.downloaded", ex);
            FacesMessages.error("file.cannot.be.downloaded", ex.getMessage());
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}