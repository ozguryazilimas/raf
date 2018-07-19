/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
    
}
