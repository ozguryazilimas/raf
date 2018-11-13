/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.encoder;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author oyas
 */
public class RafFileNameEncoderTest {
    
    public RafFileNameEncoderTest() {
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
     * Test of encode method, of class RafFileNameEncoder.
     */
    @org.junit.Test
    public void testEncode() {
        System.out.println("encode");
        String text = "ÜĞİŞÇÖI üğişçöı .%/_-?=";
        RafFileNameEncoder instance = new RafFileNameEncoder();
        String expResult = "UGISCOI_ugiscoi_.%/_-?=";
        String result = instance.encode(text);
        assertEquals(expResult, result);
    }

    /**
     * Test of decode method, of class RafFileNameEncoder.
     */
    @org.junit.Test
    public void testDecode() {
        System.out.println("decode");
        String encodedText = "ugiscoi ugiscoi .%/_-?=";
        RafFileNameEncoder instance = new RafFileNameEncoder();
        String expResult = "ugiscoi ugiscoi .%/_-?=";
        String result = instance.decode(encodedText);
        assertEquals(expResult, result);
    }
    
}