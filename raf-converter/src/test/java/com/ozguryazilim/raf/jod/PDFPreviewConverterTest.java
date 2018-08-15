/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jod;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author oyas
 */
public class PDFPreviewConverterTest {
    
    public PDFPreviewConverterTest() {
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
     * Test of convert method, of class PDFPreviewConverter.
     */
    @org.junit.Test
    public void testConvert() throws Exception {
        System.out.println("com.ozguryazilim.raf.jod.PDFPreviewConverterTest.testConvert()");
        InputStream is = this.getClass().getResourceAsStream("/sample.txt");
        String mimeType = "text/plain";
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PDFPreviewConverter instance = new PDFPreviewConverter();
        instance.convert(is, mimeType, os);
        
        System.out.println("Result : " + os.toString("UTF-8"));
        
    }
    
}
