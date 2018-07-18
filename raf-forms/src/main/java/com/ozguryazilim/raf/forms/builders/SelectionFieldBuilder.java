/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms.builders;

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
        
        return result;
    }
}
