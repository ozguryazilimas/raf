package com.ozguryazilim.raf.rest;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
    @Path("/file")
    public Response downloadFileByPath(@FormParam("raf") String raf, @FormParam("path") String path) {
        try {
            RafObject ro = rafService.getRafObjectByPath("/RAF/" + raf + path);
            LOG.info(String.format("%s is downloaded.", ro.getPath()));
            return getRafFileResponse(ro);
        } catch (RafException | IOException ex) {
            LOG.error("Error while downloading file", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error while downloading file").build();
        }
    }

    @GET
    @Path("/file/{id}")
    public Response downloadFile(@PathParam("id") String docID) {
        try {
            RafObject ro = rafService.getRafObject(docID);
            LOG.info(String.format("%s is downloaded.", ro.getPath()));
            return getRafFileResponse(ro);
        } catch (RafException | IOException ex) {
            LOG.error("Error while downloading file", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error while downloading file").build();
        }
    }

    private Response getRafFileResponse(RafObject ro) throws IOException, RafException {
        if (ro instanceof RafFolder) {
            //Check max file limit
            long maxFolderSize = Long.parseLong(ConfigResolver.getPropertyValue("raf.multiplefiledownloadlimit", String.valueOf(104857600)));
            try {
                rafService.getFolderSize(ro.getPath(), maxFolderSize);
            } catch (RafException e) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(e.getMessage())
                        .build();
            }

            StreamingOutput streamingOutput = output -> {
                try {
                    ZipOutputStream zipOut = new ZipOutputStream(output);
                    rafService.zipFile(ro, ro.getName(), zipOut);
                    zipOut.close();
                    output.close();
                } catch (RafException e) {
                    LOG.error("Error while zipping content",e);
                }
            };

            return Response.ok(streamingOutput, MediaType.APPLICATION_OCTET_STREAM_TYPE)
                    .header("Content-Disposition", "attachment; filename=\"" + ro.getName() + ".zip" + "\"")
                    .build();

        } else {
            InputStream is = rafService.getDocumentContent(ro.getId());
            return Response.ok(is, MediaType.APPLICATION_OCTET_STREAM_TYPE)
                    .header("Content-Disposition", "attachment; filename=\"" + ro.getName() + "\"")
                    .header("Content-Length", ro.getLength())
                    .build();

        }
    }
}

