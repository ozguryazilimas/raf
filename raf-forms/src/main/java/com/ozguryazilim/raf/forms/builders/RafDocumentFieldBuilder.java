/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms.builders;

import com.ozguryazilim.raf.forms.model.AbstractField;
import com.ozguryazilim.raf.forms.model.RafDocumentField;
import java.util.Map;

/**
 *
 * @author oyas
 */
public class RafDocumentFieldBuilder extends AbstractFieldBuilder<RafDocumentField>{
    @Override
    public RafDocumentField build(Map<String, String> attributes) {
        RafDocumentField result = new RafDocumentField();
        baseBuild(result, attributes);
        //result.setDefaultValue(attributes.get("defaultValue"));
        
        return result;
    }

    @Override
    public AbstractField build(RafDocumentField field) {
        RafDocumentField result = new RafDocumentField();
        baseBuild(field,result);
        //result.setDefaultValue(attributes.get("defaultValue"));
        
        return result;
    }
}
