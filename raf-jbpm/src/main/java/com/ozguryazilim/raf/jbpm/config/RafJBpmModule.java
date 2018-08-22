/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm.config;

import com.ozguryazilim.raf.jbpm.identity.TelveUserGroupCallback;
import com.ozguryazilim.telve.api.module.TelveModule;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Raf-JBPM modül tanımı initi.
 * 
 * @author Hakan Uygun
 */
@TelveModule
public class RafJBpmModule {
    
    private static final Logger LOG = LoggerFactory.getLogger(RafJBpmModule.class);
    
    @PostConstruct @Transactional
    public void init(){
        //FIXME: aslında bu değerleri telve.properties'den alsak güzel olacak!
        
        //TelveUserGroupCallback üzerinden Telve IDM kullanıcı tanımlarının kullanılmasını sağlıyoruz.
        System.setProperty("org.jbpm.ht.callback", "custom");
        System.setProperty("org.jbpm.ht.custom.callback", TelveUserGroupCallback.class.getName());
        
        //Kullanıcı atamalarının RafTaskAssignmentStrategy sınıfı üzerinden yapılmasını sağlıyoruz.
        System.setProperty("org.jbpm.task.assignment.enabled", "true" );
        System.setProperty("org.jbpm.task.assignment.strategy", "RafTaskAssignment" );
    }
    
}
