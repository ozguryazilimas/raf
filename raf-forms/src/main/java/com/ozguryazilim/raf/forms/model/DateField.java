package com.ozguryazilim.raf.forms.model;

import java.util.Date;

/**
 *
 * @author oyas
 */
public class DateField extends AbstractField<Date>{

    
    
    @Override
    public Class<Date> getValueClass() {
        return Date.class;
    }

    @Override
    public String getType() {
        return "Date";
    }
    
}
