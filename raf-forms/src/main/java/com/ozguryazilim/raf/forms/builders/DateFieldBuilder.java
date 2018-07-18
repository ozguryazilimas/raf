/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms.builders;

import com.ozguryazilim.raf.forms.model.DateField;
import java.util.Map;

/**
 *
 * @author oyas
 */
public class DateFieldBuilder extends AbstractFieldBuilder{

    @Override
    public DateField build(Map<String, String> attributes) {
        DateField result = new DateField();
        baseBuild(result, attributes);
        //FIXME: value parse edilecek
        //result.setDefaultValue(attributes.get("defaultValue"));
        //FIXME: Burada ek olarak suggestion group ve key parse edilecek
        
        return result;
    }
    
}
