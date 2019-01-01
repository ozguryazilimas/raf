package com.ozguryazilim.raf.forms.builders;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.ozguryazilim.raf.forms.model.AbstractField;
import com.ozguryazilim.raf.forms.model.SelectionField;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author oyas
 */
public class SelectionFieldBuilder extends AbstractFieldBuilder<SelectionField>{
    
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

    @Override
    public AbstractField build(SelectionField field) {
        SelectionField result = new SelectionField();
        baseBuild( field, result);
        result.setDefaultValue(field.getDefaultValue());
               
        result.setValues( new ArrayList<>(field.getValues()));
        
        return result;
    }
}
