/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms.builders;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.forms.model.AbstractField;
import com.ozguryazilim.raf.forms.model.Field;
import java.util.Map;

/**
 *
 * @author oyas
 */
public abstract class AbstractFieldBuilder<T extends Field> {
    
    /**
     *
     * @param attributes
     * @return 
     */
    public abstract AbstractField build( Map<String,String> attributes);
    public abstract AbstractField build( T field);
    
    protected void baseBuild( AbstractField field, Map<String,String> attributes){
        //FIXME: default verileri ( parametre olarak xml'den gelmediyse ) doldurulacak. 
        //FIXME: Zorunlu olan alanlar doldurulmamış ise hata üretilecek.
        field.setId(attributes.get("id"));
        
        field.setDataKey(attributes.get("dataKey"));
        
        /* Burada datakey'i id atıyorduk ama artık form register edilirken bir id verilecek
        if( Strings.isNullOrEmpty(field.getId())){
            field.setId(field.getDataKey());
        }*/
        
        field.setLabel(attributes.get("label"));
        field.setPlaceholder(attributes.get("placeholder"));
        field.setReadonly("true".equals(attributes.get("readonly")));
        field.setRequired("true".equals(attributes.get("required")));
        field.setColumnClass(attributes.getOrDefault("columnClass","col-md-6 col-xs-12"));
        
    }
    
    protected void baseBuild( T sourceField, AbstractField targetField ){
        //FIXME: default verileri ( parametre olarak xml'den gelmediyse ) doldurulacak. 
        //FIXME: Zorunlu olan alanlar doldurulmamış ise hata üretilecek.
        targetField.setId(sourceField.getId());
        
        targetField.setDataKey(sourceField.getDataKey());
        
        if( Strings.isNullOrEmpty(targetField.getId())){
            targetField.setId(targetField.getDataKey());
        }
        
        targetField.setLabel(sourceField.getLabel());
        targetField.setPlaceholder(sourceField.getPlaceholder());
        targetField.setReadonly(sourceField.getReadonly());
        targetField.setRequired(sourceField.getRequired());
        targetField.setColumnClass(sourceField.getColumnClass());
    }
    
}
