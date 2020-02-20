package com.ozguryazilim.raf.record;

import com.ozguryazilim.raf.record.model.RafRecordDocumentType;
import com.ozguryazilim.raf.record.model.RafRecordProcess;
import com.ozguryazilim.raf.record.model.RafRecordType;
import java.io.InputStream;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author oyas
 */
public class RecordTypeXmlParserTest {

    public RecordTypeXmlParserTest() {
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
     * Test of parse method, of class RecordTypeXmlParser.
     */
    @org.junit.Test
    public void testParse() {
        /*
        System.out.println("parse");
        InputStream is = this.getClass().getResourceAsStream("/testTypes.rts.xml");
        RafRecordType expResult = createTestRecordType();
        List<RafRecordType> result = RecordTypeXmlParser.parse(is);
        assertEquals(1, result.size());
        
        assertEquals(expResult.getName(), result.get(0).getName());
        assertEquals(expResult.getTitle(), result.get(0).getTitle());
        assertEquals(expResult.getMetadata(), result.get(0).getMetadata());
        assertEquals(expResult.getForm(), result.get(0).getForm());
        
        assertEquals(expResult.getDocumentTypes().size(), result.get(0).getDocumentTypes().size());
        assertEquals(expResult.getDocumentTypes(), result.get(0).getDocumentTypes());
        
        assertEquals(expResult.getProcesses(), result.get(0).getProcesses());
         */
    }

    protected RafRecordType createTestRecordType() {
        RafRecordType result = new RafRecordType();
        result.setName("GelenEvrak");
        result.setTitle("Gelen Evrak");
        result.setMetadata("gelenEvrak:metadata");
        result.setForm("GelenEvrak");

        RafRecordDocumentType dt = new RafRecordDocumentType();
        dt.setName("BilgiTalep");
        dt.setTitle("Bilgi Talep");
        dt.setMetadata("bilgiTalep:metadata");
        dt.setForm("BilgiTalep");

        result.getDocumentTypes().add(dt);

        dt = new RafRecordDocumentType();
        dt.setName("DavaDilekce");
        dt.setTitle("Dava Dilekçesi");
        dt.setMetadata("DavaDilekce:metadata");
        dt.setForm("DavaDilekceStart");

        result.getDocumentTypes().add(dt);

        RafRecordProcess ps = new RafRecordProcess();

        ps.setName("GelenEvrak");
        ps.setTitle("Gelen Evrak Süreci");

        result.getProcesses().add(ps);

        return result;
    }

}
