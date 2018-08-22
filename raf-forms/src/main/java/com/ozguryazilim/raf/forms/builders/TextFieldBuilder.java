/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms.builders;

import com.ozguryazilim.raf.forms.model.AbstractField;
import com.ozguryazilim.raf.forms.model.TextField;
import java.util.Map;

/**
 *
 * @author oyas
 */
public class TextFieldBuilder extends AbstractFieldBuilder<TextField>{

    public TextField build( Map<String,String> attributes){
        
        TextField result = new TextField();
        baseBuild(result, attributes);
        result.setDefaultValue(attributes.get("defaultValue"));
        
        return result;
    }

    @Override
    public AbstractField build(TextField field) {
        TextField result = new TextField();
        baseBuild( field, result);
        result.setDefaultValue(field.getDefaultValue());
        
        return result;
    }
    
}
