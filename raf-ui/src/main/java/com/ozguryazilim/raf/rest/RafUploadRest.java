package com.ozguryazilim.raf.rest;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.RafUserRoleService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.utils.RafPathUtils;
import com.ozguryazilim.telve.auth.Identity;
import javax.jcr.AccessDeniedException;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import me.desair.tus.server.upload.UploadInfo;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author oyas
 */
@Path("/upload")
public class RafUploadRest implements Serializable{
    
    private static final Logger LOG = LoggerFactory.getLogger(RafUploadRest.class);

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
    private TusFileUploadService fileUploadService;
    
    
    @Inject
    private RafService rafService;
    
    @Inject
    private RafDefinitionService rafDefinitionService;

    @Inject
    private RafUserRoleService rafUserRoleService;

    @Inject
    private Identity identity;

    //FIXME: Yeri burası değil. Refactor
    private boolean hasCreatePermission(String path) {

        //FIXME: Refactor
        if (!RafPathUtils.isInGeneralRaf(path)) {
            //If private raf
            if (RafPathUtils.isInPrivateRaf(path)) {
                if (!path.split("/")[2].equals(identity.getUserName())) {
                    return false;
                }
            } else if (RafPathUtils.isInSharedRaf(path)) {
                boolean sharedRafEnabled = ConfigResolver.resolve("raf.shared.enabled")
                        .as(Boolean.class)
                        .withDefault(Boolean.TRUE)
                        .getValue();
                boolean sharedRafActionPermissionsEnabled = ConfigResolver.resolve("raf.shared.enable.action.permission")
                        .as(Boolean.class)
                        .withDefault(Boolean.TRUE)
                        .getValue();
                String createFolderPermission = ConfigResolver.getPropertyValue("createFolder.permission", "hasWrite");

                if (sharedRafActionPermissionsEnabled) {
                    boolean hasPermissionOnSharedRaf = identity.hasPermission("sharedRaf", "insert");
                    return "hasWrite".equals(createFolderPermission) &&
                            sharedRafEnabled &&
                            writeRoles.contains(rafUserRoleService.getRoleInPath(identity.getLoginName(), path)) &&
                            hasPermissionOnSharedRaf;
                } else {
                    return "hasWrite".equals(createFolderPermission) &&
                            sharedRafEnabled &&
                            writeRoles.contains(rafUserRoleService.getRoleInPath(identity.getLoginName(), path));
                }
            }

        } else {
            String role = rafUserRoleService.getRoleInPath(identity.getLoginName(), path);
            if (!writeRoles.contains(role)) {
                return false;
            }
        }

        return true;
    }

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

            if (!hasCreatePermission(nodePath)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
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
            if (ex.getCause() instanceof AccessDeniedException) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
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

            if (!hasCreatePermission(o.getPath())) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            
            UploadInfo uploadInfo = fileUploadService.getUploadInfo(uri);
            
            if( uploadInfo == null ){
                return Response.status(Response.Status.BAD_REQUEST).entity("TUS URI not found").build();
            }
            
            
            LOG.debug("Uploaded File : {}", uploadInfo.getFileName());
            RafDocument doc = rafService.uploadDocument( o.getPath() + "/" + uploadInfo.getFileName(), fileUploadService.getUploadedBytes(uri));
            fileUploadService.deleteUpload(uri);
            return Response.ok(doc.getId()).build();
        } catch (IOException | TusException ex) {
            //FIXME: i18n
            LOG.error("File Upload Error", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        } catch (RafException ex) {
            if (ex.getCause() instanceof AccessDeniedException) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
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

            //FIXME: Refactor
            if (!RafPathUtils.isInGeneralRaf(nodePath)) {
                //If private raf
                if (RafPathUtils.isInPrivateRaf(nodePath)) {
                    if (!nodePath.split("/")[2].equals(identity.getLoginName())) {
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                    }
                } else if (RafPathUtils.isInSharedRaf(nodePath)) {
                    boolean sharedRafEnabled = ConfigResolver.resolve("raf.shared.enabled")
                            .as(Boolean.class)
                            .withDefault(Boolean.TRUE)
                            .getValue();

                    if (!sharedRafEnabled || !identity.hasPermission("sharedRaf", "select")) {
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                    }
                }
            } else {
                String role = rafUserRoleService.getRoleInPath(identity.getLoginName(), nodePath);
                if (!writeRoles.contains(role)) {
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                }
            }

            RafObject o = rafService.getRafObjectByPath(nodePath + "/" + docPath );

            if( o instanceof RafDocument ){
                return Response.ok(((RafDocument)o).getHash()).build();
            }

            return Response.ok(o.getId()).build();
        } catch ( RafException e ){
            if (e.getCause() instanceof AccessDeniedException) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            LOG.error("Raf Exception", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    
}
