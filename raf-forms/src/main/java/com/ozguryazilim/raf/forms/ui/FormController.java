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
