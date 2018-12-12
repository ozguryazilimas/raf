/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.ozguryazilim.raf.forms.FieldBuilder;
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
public class FieldBuilderTest {
    
    public FieldBuilderTest() {
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
     * Test of fieldFor method, of class FieldBuilder.
     * FIXME: bu test çalışmaz olmuş niye ki?
     */
    //@Test
    public void testFieldBuilder() throws InstantiationException, IllegalAccessException {
        System.out.println("FieldBuilder");
        String dataKey = "invoice:account";
        FieldBuilder expResult = null;
        FieldBuilder result = FieldBuilder.type("Text");
        assertEquals(expResult, result);
        
    }
    
}
