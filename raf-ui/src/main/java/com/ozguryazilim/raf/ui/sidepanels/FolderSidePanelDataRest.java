package com.ozguryazilim.raf.ui.sidepanels;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import java.io.Serializable;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author oyas
 */
@Path("folderData")
public class FolderSidePanelDataRest implements Serializable{
    
    @Inject
    private RafService rafService;
    
    @Inject
    private RafContext context;
    
    @Inject
    private RafDefinitionService rafDefinitionService;
    
    /**
     *
     * @param id
     * @return
     */
    @GET
    public Response folderList( @QueryParam("id") String id, @QueryParam("raf") String raf ) throws RafException{

        List<RafFolder> folders = null;
        
        if( "#".equals(id)){
            RafDefinition rafDefinition = rafDefinitionService.getRafDefinitionByCode(raf);
            folders = rafService.getRootFolders(rafDefinition.getNode().getPath());
        } else {
            RafObject o = rafService.getRafObject(id);
            folders = rafService.getChildFolderList(o.getPath());
        }
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("[");
        for( RafFolder f : folders ){
            sb.append("{");
            
            sb.append("\"id\":\"").append(f.getId()).append("\",");
            sb.append("\"parent\":\"").append(f.getParentId()).append("\",");
            sb.append("\"text\":\"").append(f.getTitle()).append("\"");
            
            if( !"#".equals(f.getParentId())){
                sb.append(",\"children\": true" );
            }
            
            sb.append("},");
        }
        if( sb.length() > 1 ){
            sb.deleteCharAt(sb.length()-1);
        }
        sb.append("]");
        
        return Response.ok(sb.toString(), MediaType.APPLICATION_JSON).build();
    }
    
    
}
