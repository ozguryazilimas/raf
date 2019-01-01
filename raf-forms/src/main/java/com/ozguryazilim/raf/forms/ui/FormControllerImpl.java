package com.ozguryazilim.raf.forms.ui;

import com.ozguryazilim.raf.forms.FormManager;
import com.ozguryazilim.raf.forms.model.Field;
import com.ozguryazilim.raf.forms.model.Form;
import java.io.Serializable;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@Dependent
public class FormControllerImpl implements Serializable{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormControllerImpl.class);
    
    @Inject
    private FormManager formManager;
    
    private Form form;
    private Map<String,Object> data;
    
    public void init( String formKey, Map<String,Object> data ){
        LOG.debug("Form Controller init with '{}'", formKey);
        form = formManager.getForm(formKey);
        this.data = data;
        for( Field f : form.getFields()){
            f.setData(data);
        }
    }
    
    public Form getForm() {
        return form;
    }

    public Map<String, Object> getData() {
        return data;
    }
    
}
