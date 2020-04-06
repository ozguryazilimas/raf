package com.ozguryazilim.raf.rest;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
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
 *
 * member ekleme yönetme, silme v.s. API'leri de lazım
 * 
 * Ayrıca tek bir tanesini de geri dönenilmek lazım
 * 
 * @author oyas
 */
@Path("/api/raf")
public class RafRest {
    
    private static final Logger LOG = LoggerFactory.getLogger(RafRest.class);
    
    @Inject
    private RafService rafService;
    
    @Inject
    private RafDefinitionService rafDefinitionService;
    
    
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<RafDefinition> getRafList(){
        
        return rafDefinitionService.getRafs();
        
    }
    
    
    @GET
    @Path("/{raf}")
    @Produces(MediaType.APPLICATION_JSON)
    public RafDefinition getRafList(@PathParam("raf") String rafCode) throws RafException{
        
        return rafDefinitionService.getRafDefinitionByCode(rafCode);
        
    }
    
    
    @POST()
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRaf(@FormParam("rafCode") String rafCode, 
            @FormParam("rafName") String rafName, 
            @FormParam("info") String info) {
        
        //FIXME: Yeti kontrolü servis üzeride yapılıyor mu bakmak lazım.
        
        try{
            RafDefinition rd = new RafDefinition();

            rd.setCode(rafCode);
            rd.setName(rafName);
            rd.setInfo(info);
            rafDefinitionService.createNewRaf(rd);
        } catch ( RafException e ){
            //FIXME: Burada hata mı değil mi karar vermek ve ona göre cevap vermek lazım
            LOG.error("Raf Create Error", e);
            return Response.status(Response.Status.CREATED).entity(e.getMessage()).build();
        }
        
        return Response.ok().build();
    }
    
}
