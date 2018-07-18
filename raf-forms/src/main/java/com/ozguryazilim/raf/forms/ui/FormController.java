/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms.ui;

import com.ozguryazilim.raf.forms.model.Form;
import java.util.Map;

/**
 *
 * @author oyas
 */
public interface FormController {
    
    Form getForm();
    Map<String,Object> getData();
    
}
