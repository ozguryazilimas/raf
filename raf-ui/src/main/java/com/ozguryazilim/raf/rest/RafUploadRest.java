package com.ozguryazilim.raf.rest;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionRepository;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafMemberType;
import com.ozguryazilim.raf.jcr.ModeShapeRepositoryFactory;
import com.ozguryazilim.raf.jcr.RafModeshapeRepository;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafNode;
import com.ozguryazilim.raf.models.RafObject;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import me.desair.tus.server.upload.UploadInfo;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author oyas
 */
@Path("/upload")
public class RafUploadRest implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RafUploadRest.class);

    @Inject
    private RafDefinitionService rafDefinitionService;

    @Inject
    private RafModeshapeRepository rafRepository;

    @Inject
    private TusFileUploadService fileUploadService;

    @Inject
    private RafService rafService;

    @Inject
    private RafDefinitionRepository repository;

    @Inject
    private RafMemberService memberService;

    @POST
    @Path("/createNewRaf")
    public Response createNewRaf(@FormParam("code") String code, @FormParam("name") String name, @FormParam("member") String member) {
        try {
            RafDefinition rd = new RafDefinition();
            rd.setCode(code);
            rd.setName(name);
            RafNode n = rafRepository.createRafNode(rd);
            rd.setNodeId(n.getId());
            repository.save(rd);
            memberService.addMember(rd, member, RafMemberType.USER, "MANAGER");
            //FIXME Oluşturuyor, Session save and logout da yapıyor ama uygulamada anında göstermiyor. logout login olunca gösteriyor.
            return Response.ok(rd.getNodeId()).build();
        } catch (RafException ex) {
            LOG.error("Cannot Create Folder", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/createFolder")
    public Response createFolder(@FormParam("raf") String raf, @FormParam("folderPath") String folderPath) {
        try {

            //FIXME: fieldlar doğru mu? Dolumu kontrol edilmeli

            RafDefinition rafDefinition = rafDefinitionService.getRafDefinitionByCode(raf);

            //FIXME: Burada yetki kontrolü gerek.

            RafFolder folder = rafService.createFolder(rafDefinition.getNode().getPath() + "/" + folderPath);
            LOG.debug("Folder Created : {}", folder.getPath());
            return Response.ok(folder.getId()).build();
        } catch (RafException ex) {
            LOG.error("Cannot Create Folder", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/complate")
    public Response uploadComplated(@FormParam("raf") String raf, @FormParam("folderId") String folderId, @FormParam("uri") String uri) {
        LOG.info("Upload Complete {} {} {}", raf, folderId, uri);

        try {
            //FIXME: yetki kontrolü yapılmalı

            RafObject o = rafService.getRafObject(folderId);

            UploadInfo uploadInfo = fileUploadService.getUploadInfo(uri);

            if (uploadInfo == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("TUS URI not found").build();
            }

            LOG.debug("Uploaded File : {}", uploadInfo.getFileName());
            RafDocument doc = rafService.uploadDocument(o.getPath() + "/" + uploadInfo.getFileName(), fileUploadService.getUploadedBytes(uri));
            fileUploadService.deleteUpload(uri);
            return Response.ok(doc.getId()).build();
        } catch (IOException | TusException | RafException ex) {
            //FIXME: i18n
            LOG.error("File Upload Error", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    /**
     * Geriye Object ID'si ve eğer mümkünse hash değerini döndürür.
     *
     * @param raf
     * @param folderPath
     * @return
     * @throws RafException
     */
    @GET
    @Path("/{raf}/{folderPath}")
    public Response getObjectData(@PathParam("raf") String raf, @PathParam("folderPath") String folderPath, @QueryParam("p") String docPath) throws RafException, UnsupportedEncodingException {

        //FIXME: yetki kontrolü
        //FIXME: hata kontrolü

        LOG.debug("Raf : {}, Requested object path: {}", raf, folderPath);

        docPath = URLDecoder.decode(docPath, "UTF-8");

        RafDefinition rafDefinition = rafDefinitionService.getRafDefinitionByCode(raf);
        RafObject o = rafService.getRafObjectByPath(rafDefinition.getNode().getPath() + "/" + docPath);

        if (o instanceof RafDocument) {
            return Response.ok(((RafDocument) o).getHash()).build();
        }

        return Response.ok(o.getId()).build();
    }

    @POST
    @Path("/uploadSingleFile/{raf}/{folderPath}/{fileName}")
    @Consumes("multipart/form-data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadSingleFile(
            @FormDataParam("file") InputStream uploadedFile,
            @PathParam("raf") String raf,
            @PathParam("folderPath") String folderPath,
            @PathParam("fileName") String fileName
    ) {
        RafDocument result = null;
        try {
            String path = rafService.getCollection(folderPath).getPath() + "/" + fileName;
            result = rafRepository.uploadDocument(path, uploadedFile);
        }  catch (RafException e) {
            e.printStackTrace();
        }

        return Response.ok(result.getId()).status(200).entity(result).build();
    }

}
