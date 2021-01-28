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
public class FileNameEncoderTest {

    public FileNameEncoderTest() {
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
     * Test of encode method, of class FileNameEncoder.
     */
    @org.junit.Test
    public void testEncode() {
        System.out.println("encode");
        String text = "ÜĞİŞÇÖI üğişçöı :.%/_-?=.txt";
        FileNameEncoder instance = new FileNameEncoder();
        String expResult = "UGISCOI_ugiscoi__.%/_-?=.txt";
        String result = instance.encode(text);
        assertEquals(expResult, result);
    }

    /**
     * Test of decode method, of class FileNameEncoder.
     */
    @org.junit.Test
    public void testDecode() {
        System.out.println("decode");
        String encodedText = "ugiscoi ugiscoi .%/_-?=";
        FileNameEncoder instance = new FileNameEncoder();
        String expResult = "ugiscoi ugiscoi .%/_-?=";
        String result = instance.decode(encodedText);
        assertEquals(expResult, result);
    }

}
