package com.ozguryazilim.raf;

import com.ozguryazilim.raf.models.RafObject;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@RequestScoped
public class DownloadService {

    @Inject
    private RafService rafService;

    @Inject
    private FacesContext facesContext;

    public void downloadFile(RafObject doc) throws IOException, RafException {
        //FIXME: Yetki kontrolü ve event fırlatılacak

        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType(doc.getMimeType());

        response.setHeader("Content-disposition", String.format("attachment;filename=\"%s\"", doc.getName()));
        response.setContentLengthLong(doc.getLength());

        //FIXME: RafObject içine en azından RafDocument içine boyut ve hash bilgisi yazmak lazım.

        try (OutputStream out = response.getOutputStream()) {
            rafService.getDocumentContent(doc.getId(), out);
            out.flush();
        }

        facesContext.responseComplete();
    }

}
