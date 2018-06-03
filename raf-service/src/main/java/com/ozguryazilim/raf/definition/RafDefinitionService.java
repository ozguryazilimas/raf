/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.definition;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.entities.RafDefinition;
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
    private RafDefinitionRepository repository;
    
    public void createNewRaf( RafDefinition rd ) throws RafException{
        
        //Raf daha önce tanımlı mı?
        if( repository.findAnyByCode(rd.getCode()) != null ){
            //Bu isimli bir raf tanımı zaten var.
            throw new RafException();
        }
        
        repository.save(rd);
    }
    
}
