package com.ozguryazilim.raf.forms.model;

/**
 *
 * @author oyas
 */
public class UserField extends AbstractField<String>{

    @Override
    public Class<String> getValueClass() {
        return String.class;
    }
    
    @Override
    public String getType() {
        return "User";
    }
}
