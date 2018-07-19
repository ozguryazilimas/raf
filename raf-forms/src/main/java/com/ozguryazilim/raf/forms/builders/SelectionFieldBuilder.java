/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms.builders;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.ozguryazilim.raf.forms.model.SelectionField;
import java.util.Map;

/**
 *
 * @author oyas
 */
public class SelectionFieldBuilder extends AbstractFieldBuilder{
    
    public SelectionField build( Map<String,String> attributes){
        
        SelectionField result = new SelectionField();
        baseBuild(result, attributes);
        result.setDefaultValue(attributes.get("defaultValue"));
        //FIXME: Burada ek olarak valueListesi parse edilecek.
        
        String vals = attributes.get("values");
        if( !Strings.isNullOrEmpty(vals)){
            result.setValues( Splitter.on(',').trimResults().omitEmptyStrings().splitToList(vals));
        }
        
        return result;
    }
}
