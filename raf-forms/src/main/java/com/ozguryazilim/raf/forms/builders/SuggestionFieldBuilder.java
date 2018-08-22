/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms.builders;

import com.ozguryazilim.raf.forms.model.AbstractField;
import com.ozguryazilim.raf.forms.model.SuggestionField;
import java.util.Map;

/**
 *
 * @author oyas
 */
public class SuggestionFieldBuilder extends AbstractFieldBuilder<SuggestionField>{

    public SuggestionField build( Map<String,String> attributes){
        
        SuggestionField result = new SuggestionField();
        baseBuild(result, attributes);
        result.setDefaultValue(attributes.get("defaultValue"));
        //FIXME: Burada ek olarak suggestion group ve key parse edilecek
        
        result.setGroup(attributes.getOrDefault("group", "forms"));
        result.setKey(attributes.getOrDefault("key", ""));
        
        return result;
    }

    @Override
    public AbstractField build(SuggestionField field) {
        SuggestionField result = new SuggestionField();
        baseBuild(field, result);
        result.setDefaultValue(field.getDefaultValue());
        //FIXME: Burada ek olarak suggestion group ve key parse edilecek
        
        result.setGroup(field.getGroup());
        result.setKey(field.getKey());
        
        return result;
    }
    
}
