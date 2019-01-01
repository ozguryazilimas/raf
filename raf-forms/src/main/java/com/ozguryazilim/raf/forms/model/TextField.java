package com.ozguryazilim.raf.forms.model;

/**
 *
 * @author oyas
 */
public class TextField extends AbstractField<String>{

    public TextField() {
    }

    public TextField(String dataKey, String label) {
        super(dataKey, label);
    }
    
    

    @Override
    public Class<String> getValueClass() {
        return String.class;
    }

    @Override
    public String getType() {
        return "Text";
    }
 
    
}
