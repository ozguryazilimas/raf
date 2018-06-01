/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.repo;

import com.ozguryazilim.telve.auth.Identity;
import java.util.Map;
import javax.jcr.Credentials;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.modeshape.jcr.ExecutionContext;
import org.modeshape.jcr.security.AuthenticationProvider;
import org.modeshape.jcr.security.AuthorizationProvider;
import org.modeshape.jcr.security.SecurityContext;
import org.modeshape.jcr.value.Path;

/**
 *
 * @author oyas
 */
public class RafSecurityProvider implements AuthenticationProvider, AuthorizationProvider, SecurityContext{

    @Override
    public ExecutionContext authenticate(Credentials credentials, String repositoryName, String workspaceName, ExecutionContext repositoryContext, Map<String, Object> sessionAttributes) {
        return repositoryContext.with(this);
    }

    @Override
    public boolean hasPermission(ExecutionContext context, String repositoryName, String repositorySourceName, String workspaceName, Path absPath, String... actions) {
        //FIXME: Bunun detaylarına bir bakmak lazım.
        return true;
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public String getUserName() {
        return getIdentity().getLoginName();
    }

    @Override
    public boolean hasRole(String roleName) {
        //FIXME: Buraya gelen rol isimleri ModeShape'in readwrite, admin felan şeklinde. Bunların telve rollerine çevrilmesi lazım.
        //Kullanıcının grupları içerisinde istenilen role/grup adı var mı diye bakılıyor.
        //return getIdentity().getGroups().contains(roleName);
        return true;
    }

    @Override
    public void logout() {
        //Logout sırasında yapacak bişeyimiz yok gibi görünüyor.
    }
 
    
    protected Identity getIdentity(){
        return BeanProvider.getContextualReference(Identity.class, true);
    }
}
