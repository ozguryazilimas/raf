/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
    
}
