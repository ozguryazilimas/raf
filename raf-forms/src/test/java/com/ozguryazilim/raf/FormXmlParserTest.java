/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.ozguryazilim.raf.forms.FormXmlParser;
import com.ozguryazilim.raf.forms.model.Form;
import java.io.InputStream;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author oyas
 */
public class FormXmlParserTest {
    
    public FormXmlParserTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of parse method, of class FormXmlParser.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        InputStream is = getClass().getResourceAsStream("/testForm.frm.xml");
        Form expResult = null;
        Form result = FormXmlParser.parse(is);
        //assertEquals(expResult, result);
        assertEquals("invoice:metadata", result.getFormKey());
        assertEquals("1.0", result.getVersion());
        assertEquals("invoiceMetadata", result.getId());
        
        assertEquals(5, result.getFields().size());
        assertEquals("invoice:account", result.getFields().get(0).getDataKey());
        
    }

    
    
}
