package com.ozguryazilim.raf.webdav.ui;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author oyas
 */
public class OfficeEditActionTest {
    
    /**
     * Test of buildWebDAvRequestPath method, of class OfficeEditAction.
     */
    @org.junit.Test
    public void testBuildWebDAvRequestPath1() {
        System.out.println("buildWebDAvRequestPath1");
        String path = "/PRIVATE/telve/deneme/deneme.odt";
        OfficeEditAction instance = new OfficeEditAction();
        String expResult = "/PRIVATE/deneme/deneme.odt";
        String result = instance.buildWebDAvRequestPath(path);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of buildWebDAvRequestPath method, of class OfficeEditAction.
     */
    @org.junit.Test
    public void testBuildWebDAvRequestPath2() {
        System.out.println("buildWebDAvRequestPath2");
        String path = "/RAF/deneme/deneme/deneme.odt";
        OfficeEditAction instance = new OfficeEditAction();
        String expResult = "/deneme/deneme/deneme.odt";
        String result = instance.buildWebDAvRequestPath(path);
        assertEquals(expResult, result);
    }
    
}
