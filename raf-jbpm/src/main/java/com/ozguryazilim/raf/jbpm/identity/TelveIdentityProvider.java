package com.ozguryazilim.raf.jbpm.identity;

import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.kie.internal.identity.IdentityProvider;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class TelveIdentityProvider implements IdentityProvider, Serializable{

    @Inject
    private Identity identity;
    
    @Override
    public String getName() {
        if( identity == null ){
            return "SYSTEM";
        }
        return identity.getLoginName();
    }

    @Override
    public List<String> getRoles() {
        if( identity == null ){
            return Collections.emptyList();
        }
        return identity.getRoles();
    }

    @Override
    public boolean hasRole(String role) {
        if( identity == null ){
            return false;
        }
        //TODO: Burası belki biraz daha becerikli olabilir.
        return getRoles().contains(role);
    }
    
}
