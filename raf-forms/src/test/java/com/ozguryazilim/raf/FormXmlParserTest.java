package com.ozguryazilim.raf;

import com.ozguryazilim.raf.forms.FormXmlParser;
import com.ozguryazilim.raf.forms.model.Form;
import java.io.InputStream;
import java.util.List;
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
     * FIXME: Bu test çalışmaz hale gelmiş bir bakalım
     */
    //@Test
    public void testParse() {
        System.out.println("parse");
        InputStream is = getClass().getResourceAsStream("/testForm.frm.xml");
        Form expResult = null;
        List<Form> result = FormXmlParser.parse(is);
        assertEquals(1, result.size());
        //assertEquals(expResult, result);
        assertEquals("invoice:metadata", result.get(0).getFormKey());
        assertEquals("1.0", result.get(0).getVersion());
        assertEquals("invoiceMetadata", result.get(0).getId());
        
        assertEquals(5, result.get(0).getFields().size());
        assertEquals("invoice:account", result.get(0).getFields().get(0).getDataKey());
        
    }

    
    
}
