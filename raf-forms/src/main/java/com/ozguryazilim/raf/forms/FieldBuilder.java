/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms;

import com.ozguryazilim.raf.forms.model.AbstractField;

/**
 *
 * @author oyas
 */
public class FieldBuilder {
    
    AbstractField field;
    
    public static FieldBuilder type( String fieldType ) throws InstantiationException, IllegalAccessException{
        FieldBuilder result = new FieldBuilder();
        result.field = FieldTypeRegistery.getTypeClass(fieldType).newInstance();
        
        return result;
    }
    
    public FieldBuilder setAttribute( String attributeName, String value ){
        field.setDataKey(value);
        return this;
    }
    
}
