package com.ozguryazilim.raf.rest;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafMemberType;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.idm.entities.Group;
import com.ozguryazilim.telve.idm.group.GroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Path("/auth")
public class RafAuthService implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RafUploadRest.class);

    @Inject
    private RafService rafService;

    @Inject
    private RafDefinitionService rafDefinitionService;


    @Inject
    private GroupRepository groupRepository;

    @Inject
    private RafMemberService memberService;



    @POST
    @Path("/addRafUser")
    public Response addRafUser(@FormParam("raf") String raf, @FormParam("userName") String userName, @FormParam("role") String role) {
        try {
            //FIXME: fieldlar doğru mu? Dolumu kontrol edilmeli

            RafObject o = rafService.getRafObject(raf);
            RafDefinition rafDefinition = rafDefinitionService.getRafDefinitionByCode(o.getName());

            //FIXME: Burada yetki kontrolü gerek.
            List<String> members = new ArrayList<>();
            members.add(userName);

            memberService.addMembers(rafDefinition, members, RafMemberType.USER, role);
            return Response.ok(200).build();
        } catch (RafException ex) {
            LOG.error("Cannot Added Raf User", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/addRafGroup")
    public Response addRafGroup(@FormParam("raf") String raf, @FormParam("groupCode") String groupCode, @FormParam("role") String role) {
        try {
            //FIXME: fieldlar doğru mu? Dolumu kontrol edilmelias

            RafObject o = rafService.getRafObject(raf);
            RafDefinition rafDefinition = rafDefinitionService.getRafDefinitionByCode(o.getName());

            // GRUP  İSİMLERİNE GÖRE KONTROL EDİLMELİ.

            List<Group> ls = groupRepository.findByCode(groupCode);
            if (ls.isEmpty()) {
                return Response.ok("Grup Bulunamadı").build();
            }

            //FIXME: Burada yetki kontrolü gerek.

            memberService.addMember(rafDefinition, ls.get(0).getCode(), RafMemberType.GROUP, role);
            return Response.ok(200).build();
        } catch (RafException ex) {
            LOG.error("Cannot Added Raf User", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
