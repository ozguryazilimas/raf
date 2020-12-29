package com.ozguryazilim.raf.nav;

import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.events.RafDataChangedEvent;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
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
    private RafDefinitionService rafDefinitionService;
    
    private Boolean hasPersonalRaf = false;
    private Boolean hasSharedRaf = false;
    
    private List<RafDefinition> rafs = new ArrayList<>();
    
    @PostConstruct
    public void init(){
        //FIXME: aslında burdaki kod service katmanına alınmalı. RafDefinition Service üzerinden bu bilgilere ulaşılmalı
        
        hasPersonalRaf = "true".equals( ConfigResolver.getPropertyValue("raf.personal.enabled", "true"));
        hasSharedRaf = "true".equals( ConfigResolver.getPropertyValue("raf.shared.enabled", "true")) && identity.hasPermission("sharedRaf", "select");
        
        //Yetki kontrolü rafDefinitonService üzerinde yapılıyor
        rafs = rafDefinitionService.getRafsForUser(identity.getLoginName());
    }

    public Boolean hasPersonalRaf() {
        return hasPersonalRaf;
    }

    public Boolean hasSharedRaf() {
        return hasSharedRaf;
    }

    public List<RafDefinition> getRafs() {
        return rafs.stream().limit(10).collect(Collectors.toList());
    }
    
    public void rafDataChangedListener( @Observes RafDataChangedEvent event){
        rafDefinitionService.refresh();
        rafs = rafDefinitionService.getRafsForUser(identity.getLoginName());
    }
}
