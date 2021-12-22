package com.ozguryazilim.raf.rest;

import com.ozguryazilim.telve.idm.IdmEvent;
import com.ozguryazilim.telve.idm.entities.Role;
import com.ozguryazilim.telve.idm.role.RoleRepository;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@RequiresPermissions("admin")
@Path("/api/roles")
@Produces(MediaType.APPLICATION_JSON)
public class RafRoleRest {

    private static final Logger LOG = LoggerFactory.getLogger(RafRoleRest.class);

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private Event<IdmEvent> idmEvent;

    /**
     * Tüm kullanıcı rollerini döndürür.
     *
     * @return
     */
    @GET
    public List<Role> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        //Infinite recursion probleminden dolayı permissions alanını boşalttım.
        return roles.stream().peek(r -> r.setPermissions(null)).collect(Collectors.toList());
    }

    /**
     * Yeni rol ekler
     *
     * @param code
     * @param name
     * @param info
     * @return
     */
    @POST()
    public Response addRole(@FormParam("code") String code,
                                @FormParam("name") String name,
                                @FormParam("info") String info) {
        try {
            Role role = new Role();
            role.setActive(Boolean.TRUE);
            role.setCode(code);
            role.setName(name);
            role.setInfo(info);
            roleRepository.save(role);
            idmEvent.fire(new IdmEvent(IdmEvent.FROM_ROLE, IdmEvent.CREATE, code));
        } catch (Exception e) {
            LOG.error(String.format("Role Create Error - %s", code), e);
            return Response.status(Response.Status.CREATED).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }
}
