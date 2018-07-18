/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms.builders;

import com.ozguryazilim.raf.forms.model.SuggestionField;
import java.util.Map;

/**
 *
 * @author oyas
 */
public class SuggestionFieldBuilder extends AbstractFieldBuilder{

    public SuggestionField build( Map<String,String> attributes){
        
        SuggestionField result = new SuggestionField();
        baseBuild(result, attributes);
        result.setDefaultValue(attributes.get("defaultValue"));
        //FIXME: Burada ek olarak suggestion group ve key parse edilecek
        
        return result;
    }
    
}
