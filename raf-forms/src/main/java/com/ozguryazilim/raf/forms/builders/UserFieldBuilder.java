/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms.builders;

import com.ozguryazilim.raf.forms.model.AbstractField;
import com.ozguryazilim.raf.forms.model.UserField;
import java.util.Map;

/**
 *
 * @author oyas
 */
public class UserFieldBuilder extends AbstractFieldBuilder<UserField>{

    @Override
    public UserField build(Map<String, String> attributes) {
        UserField result = new UserField();
        baseBuild(result, attributes);
        result.setDefaultValue(attributes.get("defaultValue"));
        
        return result;
    }

    @Override
    public AbstractField build(UserField field) {
        UserField result = new UserField();
        baseBuild( field, result);
        result.setDefaultValue( field.getDefaultValue());
        
        return result;
    }
    
}
