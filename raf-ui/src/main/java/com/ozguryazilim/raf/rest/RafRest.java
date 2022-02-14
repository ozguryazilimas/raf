package com.ozguryazilim.raf.rest;


import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafMember;
import com.ozguryazilim.raf.entities.RafMemberType;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.tag.TagSuggestionService;
import com.ozguryazilim.telve.rest.ext.Logged;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 *
 * member ekleme yönetme, silme v.s. API'leri de lazım
 * 
 * Ayrıca tek bir tanesini de geri dönenilmek lazım
 * 
 * @author oyas
 */
@Logged
@RequiresPermissions("admin")
@Path("/api/raf")
public class RafRest {
    
    private static final Logger LOG = LoggerFactory.getLogger(RafRest.class);
    
    @Inject
    private RafService rafService;
    
    @Inject
    private RafDefinitionService rafDefinitionService;
    
    @Inject
    private RafMemberService rafMemberService;

    @Inject
    private TagSuggestionService tagSuggestionService;

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

    @GET
    @Path("/{raf}/member")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RafMember> getMemberList(@PathParam("raf") String rafCode) throws RafException {
        List<RafMember> members = new ArrayList<>();
        RafDefinition raf = rafDefinitionService.getRafDefinitionByCode(rafCode);
        if (raf != null)
            members = rafMemberService.getMembers(raf);
        return members;
    }

    @POST()
    @Path("{raf}/member")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addMember(@PathParam("raf") String rafCode,
                              @FormParam("member") String member,
                              @FormParam("memberType") String memberType,
                              @FormParam("role") String role) throws RafException {

        RafDefinition raf = rafDefinitionService.getRafDefinitionByCode(rafCode);
        if (raf == null)
            return Response.status(Response.Status.NOT_FOUND).entity("Raf Code Not Found.").build();

        try {
            rafMemberService.addMember(raf, member, RafMemberType.valueOf(memberType), role);
        } catch (RafException e) {
            LOG.error("Member Add Error", e);
            return Response.status(Response.Status.CREATED).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/tag")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, List<String>> getTags() {
        return tagSuggestionService.getSuggestionsWithKeys();
    }

    @GET
    @Path("/{raf}/tag")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getTagsByRaf(@PathParam("raf") String rafCode) {
        return tagSuggestionService.getSuggestions(rafCode);
    }

    @POST()
    @Path("{raf}/tag")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTag(@PathParam("raf") String rafCode,
                           @FormParam("tag") String tag) {
        try {
            tagSuggestionService.saveSuggestion(rafCode, tag);
        } catch ( Exception e ){
            LOG.error("Tag (Suggestion) Add Error", e);
            return Response.status(Response.Status.CREATED).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }

    @GET
    @Path("object/{objectId}/tag")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getTagsByRafObject(@PathParam("objectId") String objectId) throws RafException {
        List<String> result = new ArrayList<>();

        RafObject rafObject = rafService.getRafObject(objectId);
        if (rafObject != null)
            result = rafObject.getTags();

        return result;
    }


    @POST()
    @Path("object/{objectId}/tag")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTagByRafObject(@PathParam("objectId") String objectId,
                                      @FormParam("tag") String tag) throws RafException {
        try {
            RafObject rafObject = rafService.getRafObject(objectId);
            if (rafObject == null)
                return Response.status(Response.Status.NOT_FOUND).entity("Raf Object Not Found.").build();

            rafObject.getTags().add(tag);
            rafService.saveProperties(rafObject);
        } catch ( Exception e ){
            LOG.error("Raf Object Tag Add Error", e);
            return Response.status(Response.Status.CREATED).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }

}
