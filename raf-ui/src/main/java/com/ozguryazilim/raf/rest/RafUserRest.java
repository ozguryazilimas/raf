package com.ozguryazilim.raf.rest;

import com.ozguryazilim.telve.idm.IdmEvent;
import com.ozguryazilim.telve.idm.entities.Role;
import com.ozguryazilim.telve.idm.entities.User;
import com.ozguryazilim.telve.idm.entities.UserRole;
import com.ozguryazilim.telve.idm.role.RoleRepository;
import com.ozguryazilim.telve.idm.user.UserRepository;
import com.ozguryazilim.telve.idm.user.UserRoleRepository;
import org.apache.camel.Body;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author oyas
 */
@RequiresPermissions("admin")
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
public class RafUserRest {

    private static final Logger LOG = LoggerFactory.getLogger(RafUserRest.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private UserRoleRepository userRoleRepository;

    @Inject
    private Event<IdmEvent> idmEvent;

    @GET
    public List<User> getUsers(){
        //Infinite recursion probleminden dolayı domain group alanını boşalttım.
        return userRepository.findAll().stream().peek(u -> u.setDomainGroup(null)).collect(Collectors.toList());
    }

    //@RequiresRoles("admin")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addUser( @Body UserPayload uvm ){
        
        User user = new User();
        
        user.setLoginName(uvm.getLoginName());
        user.setFirstName(uvm.getFirstName());
        user.setLastName(uvm.getLastName());
        user.setEmail(uvm.getEmail());
        user.setMobile(uvm.getMobile());
        user.setPasswordEncodedHash(uvm.getPasswordEncodedHash());
        user.setUserType(uvm.getUserType());
        
        userRepository.saveAndFlush(user);
        idmEvent.fire(new IdmEvent(IdmEvent.FROM_USER, IdmEvent.CREATE, uvm.getLoginName()));
        return Response.ok().build();
    }

    /**
     * İstenen kullanıcıya ait rol listesini döner
     *
     * @return
     */
    @GET
    @Path("/{username}/roles")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Role> getUserRoles(@PathParam("username") String username) {

        User user = userRepository.findAnyByLoginName(username);
        return userRoleRepository.findByUser(user)
                .stream()
                .map(ur -> {
                    Role role = ur.getRole();
                    role.setPermissions(null);
                    return role;
                })
                .collect(Collectors.toList());
    }

    /**
     * Mevcut kullanıcıya tanımlı rollerde var ise verilen rolü ekler.
     * @param username
     * @param role
     * @return
     */
    @POST()
    @Path("/{username}/roles")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addRoleToUser(@PathParam("username") String username,
                                @FormParam("role") String role) {
        try {
            UserRole userRole = new UserRole();
            userRole.setUser(userRepository.findAnyByLoginName(username));
            userRole.setRole(roleRepository.findAnyByName(role));
            //Eklemek istediğimiz role halihazırda bu kullanıcı üzerinde varsa tekrar eklemiyoruz.
            if(userRoleRepository.findByUser(userRole.getUser()).stream().anyMatch(ur -> ur.getRole().getCode().equals(role))){
                LOG.warn("Requested role is already added : {}", role);
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Requested role is already added").build();
            }
            userRoleRepository.save(userRole);
            idmEvent.fire(new IdmEvent(IdmEvent.FROM_USER, IdmEvent.UPDATE, username));
        } catch (Exception e) {
            LOG.error(String.format("Role Add Error - %s", username), e);
            return Response.status(Response.Status.CREATED).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }
    
}
