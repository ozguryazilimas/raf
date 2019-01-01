package com.ozguryazilim.raf.forms.builders;

import com.ozguryazilim.raf.forms.model.AbstractField;
import com.ozguryazilim.raf.forms.model.RafFolderField;
import java.util.Map;

/**
 *
 * @author oyas
 */
public class RafFolderFieldBuilder extends AbstractFieldBuilder<RafFolderField>{

    @Override
    public RafFolderField build(Map<String, String> attributes) {
        RafFolderField result = new RafFolderField();
        baseBuild(result, attributes);
        //result.setDefaultValue(attributes.get("defaultValue"));
        
        return result;
    }

    @Override
    public AbstractField build(RafFolderField field) {
        RafFolderField result = new RafFolderField();
        baseBuild(field,result);
        //result.setDefaultValue(attributes.get("defaultValue"));
        
        return result;
    }
    
}
