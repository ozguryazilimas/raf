/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.ozguryazilim.raf.forms.FormBuilder;
import com.ozguryazilim.raf.forms.FieldTypeRegistery;
import com.ozguryazilim.raf.forms.model.Form;
import com.ozguryazilim.raf.forms.model.SelectionField;
import com.ozguryazilim.raf.forms.model.TextField;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author oyas
 */
public class FormBuilderTest {
    
    /**
     * Test of createForm method, of class FormBuilder.
     */
    @org.junit.Test
    public void testFormBuilder_Defaults() {
        System.out.println("testFormBuilder Defaults");
        
        Form expResult = new Form();
        expResult.setFormKey("TestForm");
        expResult.setId("TestForm");
        expResult.setVersion("1.0");
        
        
        
        Form result = FormBuilder.createForm("TestForm")
                                .build();
        
        assertEquals(expResult.getId(), result.getId());
        assertEquals(expResult.getFormKey(), result.getFormKey());
        assertEquals(expResult.getVersion(), result.getVersion());
        
    }

    @org.junit.Test
    public void testFormBuilder_Full() {
        System.out.println("testFormBuilder Full");
        
        Form expResult = new Form();
        expResult.setFormKey("TestForm");
        expResult.setId("123");
        expResult.setVersion("2.0");
        
        
        
        Form result = FormBuilder.createForm("TestForm")
                            .withId("123")
                            .withVersion("2.0")
                            .build();
        
        assertEquals(expResult.getId(), result.getId());
        assertEquals(expResult.getFormKey(), result.getFormKey());
        assertEquals(expResult.getVersion(), result.getVersion());
        
    }
    

    @org.junit.Test
    public void testFormBuilder_Fields() throws InstantiationException, IllegalAccessException {
        System.out.println("testFormBuilder Fields");
        
        Form expResult = new Form();
        expResult.setFormKey("TestForm");
        expResult.setId("123");
        expResult.setVersion("2.0");
        
        TextField tf = new TextField("invoice:account", "invoice.account");
        expResult.getFields().add(tf);
        
        SelectionField sf = (SelectionField) FieldTypeRegistery.getField("Selection");
        sf.setId("111");
        sf.setDataKey("invoice:aaa");
        expResult.getFields().add(sf);
        
        Form result = FormBuilder.createForm("TestForm")
                            .withId("123")
                            .withVersion("2.0")
                            .addField(tf)
                            .addField(sf)
                            .build();
        
        assertEquals(expResult.getId(), result.getId());
        assertEquals(expResult.getFormKey(), result.getFormKey());
        assertEquals(expResult.getVersion(), result.getVersion());
        assertEquals(expResult.getFields().size(), result.getFields().size());
        
    }    
}
