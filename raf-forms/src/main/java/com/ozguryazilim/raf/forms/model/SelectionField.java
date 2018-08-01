/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
    
}
