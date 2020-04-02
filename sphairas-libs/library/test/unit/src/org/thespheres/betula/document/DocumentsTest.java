/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author boris.heithecker
 */
public class DocumentsTest {

    public DocumentsTest() {
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
//
//    @Test
//    public void testInc() {
//        System.out.println("inc");
//        DocumentId.Version version = null;
//        int index = 0;
//        DocumentId.Version expResult = null;
//        DocumentId.Version result = Documents.inc(version, index);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testSize() {
//        System.out.println("size");
//        DocumentId.Version version = null;
//        int expResult = 0;
//        int result = Documents.size(version);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }

    @Test
    public void testCompare() {
        System.out.println("compare");
        DocumentId.Version v1 = DocumentId.Version.parse("1.0.1");
        DocumentId.Version v2 = DocumentId.Version.parse("1.2");
        DocumentId.Version v3 = DocumentId.Version.parse("2.0.1");
        DocumentId.Version v4 = DocumentId.Version.parse("2.0");
        int result = Documents.compare(v1, v2);
        assertTrue(result < 0);
        result = Documents.compare(v2, v1);
        assertTrue(result > 0);
        result = Documents.compare(v3, v3);
        assertTrue(result == 0);
        result = Documents.compare(v4, v3);
        assertTrue(result < 0);
    }

}
