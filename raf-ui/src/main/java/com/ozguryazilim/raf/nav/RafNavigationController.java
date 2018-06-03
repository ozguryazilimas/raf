/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.nav;

import com.ozguryazilim.raf.definition.RafDefinitionRepository;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.config.ConfigResolver;

/**
 * Sistem ayarları ve kullanıcı yetkileri çerçevesinde kullanıcının erişebileceği Raf'ları belirler.
 * 
 * @author Hakan Uygun
 */
@SessionScoped
@Named
public class RafNavigationController implements Serializable{
   
    @Inject
    private Identity identity;
    
    @Inject
    private RafDefinitionRepository definitionRepository;
    
    private Boolean hasPersonalRaf = false;
    private Boolean hasSharedRaf = false;
    
    private List<RafDefinition> rafs = new ArrayList<>();
    
    @PostConstruct
    public void init(){
        //FIXME: aslında burdaki kod service katmanına alınmalı. RafDefinition Service üzerinden bu bilgilere ulaşılmalı
        
        hasPersonalRaf = "true".equals( ConfigResolver.getPropertyValue("raf.personal.enabled", "true"));
        hasSharedRaf = "true".equals( ConfigResolver.getPropertyValue("raf.shared.enabled", "true"));
        
        //FIXME: Burada yetki kontrolü yapılacak.
        rafs = definitionRepository.findAll();
    }

    public Boolean hasPersonalRaf() {
        return hasPersonalRaf;
    }

    public Boolean hasSharedRaf() {
        return hasSharedRaf;
    }

    public List<RafDefinition> getRafs() {
        return rafs;
    }
    
    
}
