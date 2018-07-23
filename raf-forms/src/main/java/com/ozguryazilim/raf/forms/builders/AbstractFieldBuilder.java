/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms.builders;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.forms.model.AbstractField;
import java.util.Map;

/**
 *
 * @author oyas
 */
public abstract class AbstractFieldBuilder {
    
    /**
     *
     * @param attributes
     * @return 
     */
    public abstract AbstractField build( Map<String,String> attributes);
    
    protected void baseBuild( AbstractField field, Map<String,String> attributes){
        //FIXME: default verileri ( parametre olarak xml'den gelmediyse ) doldurulacak. 
        //FIXME: Zorunlu olan alanlar doldurulmamış ise hata üretilecek.
        field.setId(attributes.get("id"));
        
        field.setDataKey(attributes.get("dataKey"));
        
        if( Strings.isNullOrEmpty(field.getId())){
            field.setId(field.getDataKey());
        }
        
        field.setLabel(attributes.get("label"));
        field.setPlaceholder(attributes.get("placeholder"));
        field.setReadonly("true".equals(attributes.get("readonly")));
        field.setRequired("true".equals(attributes.get("required(")));
    }
    
}
