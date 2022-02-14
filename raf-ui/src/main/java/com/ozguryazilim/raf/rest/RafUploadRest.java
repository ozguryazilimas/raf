package com.ozguryazilim.raf.rest;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.rest.ext.Logged;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import me.desair.tus.server.upload.UploadInfo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 *
 * @author oyas
 */
@Logged
@RequiresPermissions("admin")
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
            
            String nodePath = "";
            if( raf.equals("SHARED")){
                nodePath = "/SHARED";
            } else if ( raf.startsWith("PRIVATE")){
                nodePath = "/" + raf;
            } else {
                RafDefinition rafDefinition = rafDefinitionService.getRafDefinitionByCode(raf);
                nodePath = rafDefinition.getNode().getPath();
            }
            
            
            //FIXME: Burada yetki kontrolü gerek.
            //FIXME: Shared ve Private'lar için bu API'de değişiklik gerekecek. rafDefinition olmayacak ama varlığının kontrol edilmesi lazım
            
            if( folderPath.startsWith("/")){
                folderPath = folderPath.replaceFirst("/", "");
            }
                
            
            RafFolder folder = rafService.createFolder( nodePath + "/" + folderPath);
            LOG.debug("Folder Created : {}", folder.getPath());
            return Response.ok(folder.getId()).build();
        } catch (RafException ex) {
            LOG.error("Cannot Create Folder", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    @Deprecated
    @POST
    @Path("/complate")
    public Response uploadComplated(@FormParam("raf") String raf, @FormParam("folderId") String folderId, @FormParam("uri") String uri) {
        return uploadCompleted(raf, folderId, uri);
    }

    @POST
    @Path("/complete")
    public Response uploadCompleted( @FormParam("raf") String raf, @FormParam("folderId") String folderId, @FormParam("uri") String uri ){
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
    
    /**
     * Geriye Object ID'si ve eğer mümkünse hash değerini döndürür.
     * @param raf
     * @param folderPath
     * @return
     * @throws RafException 
     */
    @GET
    @Path("/{raf}/{folderPath}")
    public Response getObjectData( @PathParam("raf") String raf, @PathParam("folderPath") String folderPath, @QueryParam("p") String docPath ) throws UnsupportedEncodingException{
        
        //FIXME: yetki kontrolü
        //FIXME: hata kontrolü
        
        LOG.debug("Raf : {}, Requested object path: {}", raf, docPath);
        
        docPath = URLDecoder.decode(docPath, "UTF-8");
        

        if( docPath.startsWith("/")){
            docPath = docPath.replaceFirst("/", "");
        }
        
        try{
            
            String nodePath = "";
            if( raf.equals("PRIVATE")){
                nodePath = "/PRIVATE";
            } else if ( raf.equals("SHARED")){
                nodePath = "/SHARED";
            } else {
                RafDefinition rafDefinition = rafDefinitionService.getRafDefinitionByCode(raf);
                nodePath = rafDefinition.getNode().getPath();
            }
            
            
            
            RafObject o = rafService.getRafObjectByPath(nodePath + "/" + docPath );

            if( o instanceof RafDocument ){
                return Response.ok(((RafDocument)o).getHash()).build();
            }

            return Response.ok(o.getId()).build();
        } catch ( RafException e ){
            LOG.error("Raf Exception", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    
}
