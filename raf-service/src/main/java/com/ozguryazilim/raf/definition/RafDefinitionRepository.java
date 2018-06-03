/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.definition;

import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.telve.data.RepositoryBase;
import javax.enterprise.context.Dependent;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

/**
 *
 * @author oyas
 */
@Repository
@Dependent
public abstract class RafDefinitionRepository extends RepositoryBase<RafDefinition, RafDefinition> implements CriteriaSupport<RafDefinition> {
    
    
    public abstract RafDefinition findAnyByCode( String code );
}
