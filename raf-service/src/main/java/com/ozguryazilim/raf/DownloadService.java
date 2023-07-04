package com.ozguryazilim.raf;

import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
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

    //FIXME: Yetki kontrolü ve event fırlatılacak
    public void writeFileDataToResponse(RafObject obj) throws RafException {

        RafObject doc = obj;
        if (doc instanceof RafRecord) {
            RafRecord record = (RafRecord) doc;
            doc = record.getDocuments().isEmpty() ? doc : record.getDocuments().get(0);
        }

        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

        //Check if binary content is present
        if (!rafService.isContentPresent(doc)) {
            LOG.error("Error while downloading file: {}", doc.getPath());
            throw new RafException("Error while downloading file. Content is not present.");
        }

        //Write response
        try (OutputStream out = response.getOutputStream()) {
            //FIXME: RafObject içine en azından RafDocument içine boyut ve hash bilgisi yazmak lazım.
            response.setContentType(doc.getMimeType());
            response.setHeader("Content-disposition", String.format("attachment;filename=\"%s\"", doc.getName()));
            response.setContentLengthLong(doc.getLength());

            rafService.getDocumentContent(doc.getId(), out);
            out.flush();
        } catch (Exception ex) {
            LOG.error("Error while downloading file: {}", doc.getPath(), ex);
            throw new RafException("Error while downloading file", ex);
        }

    }

}
