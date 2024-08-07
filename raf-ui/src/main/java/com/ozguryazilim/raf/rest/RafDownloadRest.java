package com.ozguryazilim.raf.rest;

import com.google.gson.Gson;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.auth.Identity;
import javax.inject.Inject;
import javax.jcr.AccessDeniedException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.zip.ZipOutputStream;

/**
 * REST üzerinden dosya yüklemek için yazılmıştır.
 * ....:8080/dolap/download/file/{DOSYA_ID} olarak data post lanması gerekmektedir.
 * Geri dönüş olarak dosya adı ve dosya içeriği byte olarak geri döner.
 *
 * @author Taha GÜR
 */

@Path("/api/download")
public class RafDownloadRest implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RafDownloadRest.class);

    @Inject
    private RafService rafService;

    @Inject
    private RafRestPermissionService rafRestPermissionService;

    @Inject
    private Identity identity;

    @POST
    @Path("/file")
    public Response downloadFileByPath(@FormParam("raf") String raf, @FormParam("path") String path) {
        try {
            RafObject ro = rafService.getRafObjectByPath("/RAF/" + raf + path);
            if (!(isPermittedBySuperadminType() || rafRestPermissionService.hasReadPermission(ro.getPath()))) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            LOG.info(String.format("%s is downloaded.", ro.getPath()));
            return getRafFileResponse(ro);
        } catch (IOException ex) {
            LOG.error("Error while downloading file", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error while downloading file").build();
        } catch (RafException ex) {
            if (ex.getCause() instanceof AccessDeniedException) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("Content not found").build();
        }
    }

    @GET
    @Path("/file/{id}")
    public Response downloadFile(@PathParam("id") String docID) {
        try {
            RafObject ro = rafService.getRafObject(docID);

            if (!(isPermittedBySuperadminType() || rafRestPermissionService.hasReadPermission(ro.getPath()))) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            LOG.info(String.format("%s is downloaded.", ro.getPath()));
            return getRafFileResponse(ro);
        } catch (IOException ex) {
            LOG.error("Error while downloading file", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error while downloading file").build();
        } catch (RafException ex) {
            if (ex.getCause() instanceof AccessDeniedException) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("Content not found").build();
        }
    }

    @POST
    @Path("/getRafObject")
    @Consumes({"*/*"})
    @Produces({"application/json"})
    public Response getDocument(@FormParam("filePath") String filePath) {
        LOG.debug("Gelen Değer : {}", filePath);
        RafObject ro = null;

        try {
            ro = rafService.getRafObjectByPath(filePath);

            if (!(isPermittedBySuperadminType() || rafRestPermissionService.hasReadPermission(ro.getPath()))) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            LOG.debug("Okunan Raf Object : {}", ro.getId());

            Gson gson = new Gson();
            String json = gson.toJson(ro.getId());
            return Response.ok().type("application/json").entity(json).build();
        } catch (RafException ex) {
            if (ex.getCause() instanceof AccessDeniedException) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            LOG.error("Could not found raf object with path.", ex);
            return Response.status(HttpStatus.SC_NOT_FOUND).build();
        }
    }

    private Response getRafFileResponse(RafObject ro) throws IOException, RafException {
        if (ro instanceof RafFolder) {
            //Check max file limit
            long maxFolderSize = Long.parseLong(ConfigResolver.getPropertyValue("raf.multiplefiledownloadlimit", String.valueOf(104857600)));
            try {
                rafService.getFolderSize(ro.getPath(), maxFolderSize);
            } catch (RafException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
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

    private boolean isPermittedBySuperadminType() {
        boolean isPermittedBySuperadminType = ConfigResolver.resolve("raf.rest.documentOperation.permission.permitSuperadmin")
                .as(Boolean.class)
                .withCurrentProjectStage(false)
                .withDefault(Boolean.FALSE)
                .getValue();

        return isPermittedBySuperadminType && "SUPERADMIN".equals(identity.getUserInfo().getUserType());
    }
}