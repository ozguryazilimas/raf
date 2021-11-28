package com.ozguryazilim.raf.wopi;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafDocument;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WOPI Implementasyonu
 * 
 * TODO: AuditLog yazılmalı
 * TODO: token kontrolü yapılmalı
 * 
 * @author hakan
 */
@Path("/wopi")
@Produces({MediaType.APPLICATION_JSON})
public class WopiRest {

    private static final Logger LOG = LoggerFactory.getLogger(WopiRest.class);

    @Inject
    private RafService rafService;

    /**
     * Returns the file details.
     */
    @GET
    @Path("/files/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFileInfo(@PathParam("id") String id) {
        LOG.info("WOPI: Info request {}", id);
        try {
            //TODO: Obje tipi kontrol edilmeli RafRecord ise meselea master belgenin idsi ile ilgilenmeli
            RafDocument document = (RafDocument) rafService.getRafObject(id);

            return Response.ok(getFileInfo(document), MediaType.APPLICATION_JSON).build();
        } catch (JsonProcessingException ex) {
            LOG.error("Cannot Prepare Result", ex);
        } catch (RafException ex) {
            LOG.error("Cannot find object", ex);
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Opens a file and returns the file content as an octet-stream
     *
     * @param name - the file name to be opended
     * @return
     */
    @GET
    @Path("/files/{id}/contents")
    public Response getFileContents(@PathParam("id") String id) {
        LOG.info("WOPI: Content request {}", id);
        try {

            RafDocument document = (RafDocument) rafService.getRafObject(id);
            InputStream content = rafService.getDocumentContent(id);

            Response.ResponseBuilder builder = Response.ok(content, "application/octet-stream")
                    .header("Content-Disposition",
                            "attachment;filename=" + new String(document.getName().getBytes("utf-8"), "ISO-8859-1"))
                    .header("Content-Length", document.getLength());

            return builder.status(Response.Status.OK).entity(content).build();
        } catch (Exception ex) {
            LOG.error("Response Build Error", ex);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * saves a file content given form the wopi client
     *
     * @param name
     * @param content
     */
    @POST
    @Path("/files/{id}/contents")
    public Response postFileContents(@PathParam("id") String id, InputStream content) {
        LOG.info("WOPI: Save request {}", id);
        try {
            RafDocument document = (RafDocument) rafService.getRafObject(id);

            
            
            //TODO: aslında dosya için yeni sürüm / replace yapılacak
            
            return Response.ok(getFileInfo(document), MediaType.APPLICATION_JSON).build();
        } catch (JsonProcessingException ex) {
            LOG.error("Cannot Prepare Result", ex);
        } catch (RafException ex) {
            LOG.error("Cannot find object", ex);
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    private String getFileInfo(RafDocument document) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();

        result.put("BaseFileName", document.getName());
        result.put("Size", document.getLength());
        result.put("OwnerId", document.getCreateBy());
        //TODO: asıl edit eden kullanıcı lazım
        result.put("UserId", document.getCreateBy());
        result.put("UserFriendlyName", document.getCreateBy());
        result.put("Version", document.getVersion());
        result.put("LastModifiedTime", document.getUpdateDate().toString());
        result.put("Sha256", document.getHash());
        //FIXME: Kullanıcı yetkisine bakılmalı
        result.put("UserCanWrite", true);
        result.put("UserCanNotWriteRelative", true);
        result.put("HideSaveOption", true );

        return mapper.writeValueAsString(result);

    }
}
