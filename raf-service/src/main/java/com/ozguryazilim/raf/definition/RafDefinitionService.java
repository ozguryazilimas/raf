/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.definition;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.jcr.RafModeshapeRepository;
import com.ozguryazilim.raf.models.RafNode;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class RafDefinitionService implements Serializable{

    @Inject
    private Identity identity;
    
    @Inject
    private RafDefinitionRepository repository;
    
    @Inject
    private RafModeshapeRepository rafRepository;
    
    public void createNewRaf( RafDefinition rd ) throws RafException{
        
        //Raf daha önce tanımlı mı?
        if( repository.findAnyByCode(rd.getCode()) != null ){
            //Bu isimli bir raf tanımı zaten var.
            throw new RafException();
        }
        
        RafNode n = rafRepository.createRafNode(rd);
        
        rd.setNodeId(n.getId());
        
        repository.save(rd);
    }
    
    public RafDefinition getRafDefinitionByCode( String code ) throws RafException{
        
        if( "PRIVATE".equals(code)){
            RafDefinition privateRaf = new RafDefinition();
            
            privateRaf.setId(-1L);
            privateRaf.setCode(code);
            privateRaf.setName("raf.label.Private");
            
            RafNode n = rafRepository.getPrivateRafNode(identity.getLoginName());
            privateRaf.setNodeId(n.getId());
            privateRaf.setNode(n);
            
            return privateRaf;
        } else if( "SHARED".equals(code)){
            RafDefinition privateRaf = new RafDefinition();
            
            privateRaf.setId(-2L);
            privateRaf.setCode(code);
            privateRaf.setName("raf.label.Shared");
            
            RafNode n = rafRepository.getSharedRafNode();
            privateRaf.setNodeId(n.getId());
            privateRaf.setNode(n);
            
            return privateRaf;
        } else {
            //FIXME: burada yetki kontrolü de yapılması gerekiyor.
            
            RafDefinition result = repository.findAnyByCode(code);
            
            if( result!=null ){
                RafNode n = rafRepository.getRafNode(code);
                //NodeId aynı olmalı. Kontrol etsek mi?
                result.setNode(n);
            }
            
            return result;
        }
        
    }
    
}
