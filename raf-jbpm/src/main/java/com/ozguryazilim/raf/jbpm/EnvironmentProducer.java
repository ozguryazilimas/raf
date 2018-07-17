/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm;

import com.ozguryazilim.raf.jbpm.identity.TelveIdentityProvider;
import com.ozguryazilim.raf.jbpm.identity.TelveUserGroupCallback;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.cdi.Kjar;
import org.jbpm.services.task.audit.JPATaskLifeCycleEventListener;
import org.jbpm.services.task.lifecycle.listeners.TaskLifeCycleEventListener;
import org.kie.api.task.UserInfo;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class EnvironmentProducer {

    @PersistenceUnit(unitName = "org.jbpm.domain")
    private EntityManagerFactory emf;

    /*
    @Inject
    @Selectable
    private UserGroupInfoProducer userGroupInfoProducer;
    */
    
    @Inject
    private TelveUserGroupCallback userGroupCallback;
    
    @Inject
    private TelveIdentityProvider telveIdentityProvider;

    @Inject
    @Kjar
    private DeploymentService deploymentService;

    @Produces
    public EntityManagerFactory getEntityManagerFactory() {
        return this.emf;
    }

    /*
    @Produces
    public org.kie.api.task.UserGroupCallback produceSelectedUserGroupCalback() {
        return userGroupCallback;
    }*/

    @Produces
    public UserInfo produceUserInfo() {
        return null; //userGroupInfoProducer.produceUserInfo();
    }

    @Produces
    @Named("Logs")
    public TaskLifeCycleEventListener produceTaskAuditListener() {
        return new JPATaskLifeCycleEventListener(true);
    }

    @Produces
    public DeploymentService getDeploymentService() {
        return this.deploymentService;
    }

    /*
    @Produces
    public IdentityProvider produceIdentityProvider() {
        return telveIdentityProvider;
    }
    */
}
