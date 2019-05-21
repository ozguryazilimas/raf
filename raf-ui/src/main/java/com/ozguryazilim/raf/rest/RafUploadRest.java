package com.ozguryazilim.raf.rest;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import java.io.IOException;
import java.io.Serializable;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import me.desair.tus.server.upload.UploadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@Path("/upload")
public class RafUploadRest implements Serializable{
    
    private static final Logger LOG = LoggerFactory.getLogger(RafUploadRest.class);
    
    @Inject
    private TusFileUploadService fileUploadService;
    
    
    @Inject
    private RafService rafService;
    
    @Inject
    private RafDefinitionService rafDefinitionService;
    
    @POST
    @Path("/createFolder")
    public Response createFolder( @FormParam("raf") String raf, @FormParam("folderPath") String folderPath ){
        try {
            
            //FIXME: fieldlar doğru mu? Dolumu kontrol edilmeli
            
            RafDefinition rafDefinition = rafDefinitionService.getRafDefinitionByCode(raf);
            
            //FIXME: Burada yetki kontrolü gerek.
            
            RafFolder folder = rafService.createFolder( rafDefinition.getNode().getPath() + "/" + folderPath);
            LOG.debug("Folder Created : {}", folder.getPath());
            return Response.ok(folder.getId()).build();
        } catch (RafException ex) {
            LOG.error("Cannot Create Folder", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @POST
    @Path("/complate")
    public Response uploadComplated( @FormParam("raf") String raf, @FormParam("folderId") String folderId, @FormParam("uri") String uri ){
        LOG.info("Upload Complete {} {} {}", raf, folderId, uri);
        
        try {
            //FIXME: yetki kontrolü yapılmalı
            
            RafObject o = rafService.getRafObject(folderId);
            
            UploadInfo uploadInfo = fileUploadService.getUploadInfo(uri);
            
            if( uploadInfo == null ){
                return Response.status(Response.Status.BAD_REQUEST).entity("TUS URI not found").build();
            }
            
            LOG.debug("Uploaded File : {}", uploadInfo.getFileName());
            RafDocument doc = rafService.uploadDocument( o.getPath() + "/" + uploadInfo.getFileName(), fileUploadService.getUploadedBytes(uri));
            fileUploadService.deleteUpload(uri);
            return Response.ok(doc.getId()).build();
        } catch (IOException | TusException | RafException ex) {
            //FIXME: i18n
            LOG.error("File Upload Error", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }
    
    @GET
    @Path("/{raf}/{folderPath}")
    public Response getFolderId( @PathParam("raf") String raf, @PathParam("folderPath") String folderPath ) throws RafException{
        
        //FIXME: yetki kontrolü
        //FIXME: hata kontrolü
        
        RafDefinition rafDefinition = rafDefinitionService.getRafDefinitionByCode(raf);
        RafObject o = rafService.getRafObjectByPath(rafDefinition.getNode().getPath() + "/" + folderPath );
        
        return Response.ok(o.getId()).build();
    }
    
}
