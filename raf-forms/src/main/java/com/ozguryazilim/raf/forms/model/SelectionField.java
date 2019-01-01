package com.ozguryazilim.raf.forms.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author oyas
 */
public class SelectionField extends TextField{
 
    private List<String> values = new ArrayList<>();
    
    public List<String> getValues(){
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
    
    @Override
    public String getType() {
        return "Selection";
    }
}
