package com.ozguryazilim.raf.rest;

import com.google.gson.Gson;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.RafUserRoleService;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.utils.RafPathUtils;
import com.ozguryazilim.telve.auth.Identity;
import javax.jcr.AccessDeniedException;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.http.HttpStatus;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
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
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipInputStream;
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

    /**
     * FIXME: readRoles ve writeRoles setlerinin buralarda olmaları doğru değil
     *        Rol kontrolleri üzerindeki mevcut karmaşıklığın çözülmesi gerekli.
     *        RafMemberService ve RafPathMemberService incelenebilir
     */
    private static final Set<String> readRoles = new HashSet<String>() {{
        add("CONSUMER");
        add("CONTRIBUTER");
        add("EDITOR");
        add("SUPPORTER");
        add("MANAGER");
    }};
    private static final Set<String> writeRoles = new HashSet<String>() {{
        add("CONTRIBUTER");
        add("EDITOR");
        add("SUPPORTER");
        add("MANAGER");
    }};

    @Inject
    private RafService rafService;

    @Inject
    private RafUserRoleService rafUserRoleService;

    @Inject
    private Identity identity;

    @POST
    @Path("/file")
    public Response downloadFileByPath(@FormParam("raf") String raf, @FormParam("path") String path) {
        try {
            RafObject ro = rafService.getRafObjectByPath("/RAF/" + raf + path);
            String role = rafUserRoleService.getRoleInPath(identity.getLoginName(), "/RAF/" + raf + path);
            if (!readRoles.contains(role)) {
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

            //FIXME: Refactor
            if (!RafPathUtils.isInGeneralRaf(ro.getPath())) {
                //If private raf
                if (RafPathUtils.isInPrivateRaf(ro.getPath())) {
                    if (!ro.getPath().split("/")[2].equals(identity.getLoginName())) {
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                    }
                } else if (RafPathUtils.isInSharedRaf(ro.getPath())) {
                    boolean sharedRafEnabled = ConfigResolver.resolve("raf.shared.enabled")
                            .as(Boolean.class)
                            .withDefault(Boolean.TRUE)
                            .getValue();

                    if (!sharedRafEnabled || !identity.hasPermission("sharedRaf", "select")) {
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                    }
                }
            } else {
                String role = rafUserRoleService.getRoleInPath(identity.getLoginName(), ro.getPath());
                if (!readRoles.contains(role)) {
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                }
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

            //FIXME: Refactor
            if (!RafPathUtils.isInGeneralRaf(ro.getPath())) {
                //If private raf
                if (RafPathUtils.isInPrivateRaf(ro.getPath())) {
                    if (!ro.getPath().split("/")[2].equals(identity.getLoginName())) {
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                    }
                } else if (RafPathUtils.isInSharedRaf(ro.getPath())) {
                    boolean sharedRafEnabled = ConfigResolver.resolve("raf.shared.enabled")
                            .as(Boolean.class)
                            .withDefault(Boolean.TRUE)
                            .getValue();

                    if (!sharedRafEnabled || !identity.hasPermission("sharedRaf", "select")) {
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                    }
                }
            } else {
                String role = rafUserRoleService.getRoleInPath(identity.getLoginName(), ro.getPath());
                if (!readRoles.contains(role)) {
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                }
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
}

