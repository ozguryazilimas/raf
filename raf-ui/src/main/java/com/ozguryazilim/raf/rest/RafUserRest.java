package com.ozguryazilim.raf.rest;

import com.ozguryazilim.telve.idm.entities.User;
import com.ozguryazilim.telve.idm.user.UserRepository;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.camel.Body;

/**
 *
 * @author oyas
 */
@Path("/api/users")

@Produces(MediaType.APPLICATION_JSON)
public class RafUserRest {
    
    
    @Inject
    private UserRepository userRepository;
    
    
    @GET
    public List<User> getUsers(){
        return userRepository.findAll();
    }
    
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addUser( @Body UserPayload uvm ){
        
        User user = new User();
        
        user.setLoginName(uvm.getLoginName());
        user.setFirstName(uvm.getFirstName());
        user.setLastName(uvm.getLastName());
        user.setEmail(uvm.getEmail());
        user.setMobile(uvm.getMobile());
        
        user = userRepository.saveAndFlush(user);
        
        return Response.ok().build();
    }
    
}
