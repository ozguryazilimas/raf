/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms;

import com.ozguryazilim.raf.forms.model.AbstractField;
import com.ozguryazilim.raf.forms.model.Form;

/**
 *
 * @author oyas
 */
public class FormBuilder {
    
    private Form form = new Form();
    
    public static FormBuilder createForm( String formKey ){
        FormBuilder result = new FormBuilder();
        result.form.setFormKey(formKey);
        result.form.setId(formKey);
        result.form.setVersion("1.0");
        result.form.setTitle(formKey);
        
        return result;
    }
    
    public FormBuilder withId( String id ){
        form.setId(id);
        return this;
    }
    
    public FormBuilder withVersion( String version ){
        form.setVersion(version);
        return this;
    }
    
    public FormBuilder withTitle( String title ){
        form.setTitle(title);
        return this;
    }
    
    public FormBuilder fromBase( String base ){
        form.setBase(base);
        return this;
    }
    
    public FormBuilder addField( AbstractField field ){
        form.getFields().add(field);
        return this;
    }
    
    public Form build(){
        //FIXME: id, formKey yok ise hata ver. Field listesi boş ise uyarı logu ver
        return form;
    }
    
    
}
