package com.ozguryazilim.raf;

import com.ozguryazilim.raf.models.RafObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@RequestScoped
public class DownloadService {
    private Logger LOG = LoggerFactory.getLogger(DownloadService.class);

    @Inject
    private RafService rafService;

    @Inject
    private FacesContext facesContext;

    public void writeFileDataToResponse(RafObject doc) throws RafException {
        //FIXME: Yetki kontrolü ve event fırlatılacak

        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        //FIXME: RafObject içine en azından RafDocument içine boyut ve hash bilgisi yazmak lazım.
        if (!rafService.isContentPresent(doc)) {
            LOG.error("Error while downloading file: {}", doc.getPath());
            throw new RafException("Error while downloading file. Content is not present.");
        }

        try (OutputStream out = response.getOutputStream()) {
            rafService.getDocumentContent(doc.getId(), out);
            out.flush();

            response.setContentType(doc.getMimeType());

            response.setHeader("Content-disposition", String.format("attachment;filename=\"%s\"", doc.getName()));
            response.setContentLengthLong(doc.getLength());
        } catch (Exception ex) {
            LOG.error("Error while downloading file: {}", doc.getPath(), ex);
            throw new RafException("Error while downloading file", ex);
        }

    }

}
