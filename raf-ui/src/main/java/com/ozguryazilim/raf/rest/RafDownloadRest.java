package com.ozguryazilim.raf.rest;

import com.google.gson.Gson;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.DownloadResponse;
import com.ozguryazilim.raf.models.RafObject;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * REST üzerinden dosya yüklemek için yazılmıştır.
 * ....:8080/dolap/download/file/{DOSYA_ID} olarak data post lanması gerekmektedir.
 * Geri dönüş olarak dosya adı ve dosya içeriği byte olarak geri döner.
 *
 * @author Taha GÜR
 */

@RequiresPermissions("admin")
@Path("/api/download")
public class RafDownloadRest implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RafDownloadRest.class);

    @Inject
    private RafService rafService;

    @POST
    @Path("/content")
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadFileByPath(@FormParam("raf") String raf, @FormParam("path") String path) {
        try {
            RafObject ro = rafService.getRafObjectByPath("/RAF/" + raf + path);
            InputStream is = rafService.getDocumentContent(ro.getId());

            LOG.info(String.format("%s is downloaded.", ro.getPath()));
            return Response.status(Response.Status.OK)
                    .entity(new DownloadResponse(ro.getName(), IOUtils.toByteArray(is)))
                    .build();

        } catch (RafException | IOException ex) {
            String errMsg = "Error while downloading file";
            LOG.error(errMsg, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @POST
    @Path("/file")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadFile(@FormParam("docid") String docID) {
        DownloadResponse responseDownload = new DownloadResponse();
        try {
            RafObject ro = rafService.getRafObject(docID);
            InputStream is = rafService.getDocumentContent(docID);
            byte[] bytes = IOUtils.toByteArray(is);
            responseDownload.setFileName(ro.getName());
            responseDownload.setBytes(bytes);
        } catch (Exception ioEx) {
            ioEx.printStackTrace();
        }
        Gson gson = new Gson();
        String json = gson.toJson(responseDownload);
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(json).build();
    }

}

