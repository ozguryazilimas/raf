package com.ozguryazilim.raf.rest;

import com.google.gson.Gson;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
/**
 * REST üzerinden dosya yüklemek için yazılmıştır.
 * ....:8080/dolap/download/file/{DOSYA_ID} olarak data post lanması gerekmektedir.
 * Geri dönüş olarak dosya adı ve dosya içeriği byte olarak geri döner.
 *
 * @author Taha GÜR
 */
@Path("/download")
public class RafDownloadRest implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RafDownloadRest.class);

    @Inject
    private RafService rafService;


    private class DownloadResponse {
        private String fileName = "";
        private byte[] bytes;

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }
    }

    @POST
    @Path("/file")
    @Consumes("*/*")
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
