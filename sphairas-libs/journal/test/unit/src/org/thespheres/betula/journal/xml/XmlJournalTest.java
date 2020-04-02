/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.xml;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.xml.bind.Unmarshaller;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.journal.RecordNote;

/**
 *
 * @author boris.heithecker
 */
public class XmlJournalTest {
    
    public XmlJournalTest() {
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
     * Test of getRecords method, of class XmlJournal.
     */
    @Test
    public void testGetRecords() {
        System.out.println("getRecords");
        XmlJournal instance = new XmlJournal();
        Map<RecordId, XmlJournalRecord> expResult = null;
        Map<RecordId, XmlJournalRecord> result = instance.getRecords();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getJournalStart method, of class XmlJournal.
     */
    @Test
    public void testGetJournalStart() {
        System.out.println("getJournalStart");
        XmlJournal instance = new XmlJournal();
        LocalDate expResult = null;
        LocalDate result = instance.getJournalStart();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setJournalStart method, of class XmlJournal.
     */
    @Test
    public void testSetJournalStart() {
        System.out.println("setJournalStart");
        LocalDate begin = null;
        XmlJournal instance = new XmlJournal();
        instance.setJournalStart(begin);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getJournalEnd method, of class XmlJournal.
     */
    @Test
    public void testGetJournalEnd() {
        System.out.println("getJournalEnd");
        XmlJournal instance = new XmlJournal();
        LocalDate expResult = null;
        LocalDate result = instance.getJournalEnd();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setJournalEnd method, of class XmlJournal.
     */
    @Test
    public void testSetJournalEnd() {
        System.out.println("setJournalEnd");
        LocalDate end = null;
        XmlJournal instance = new XmlJournal();
        instance.setJournalEnd(end);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNotes method, of class XmlJournal.
     */
    @Test
    public void testGetNotes() {
        System.out.println("getNotes");
        XmlJournal instance = new XmlJournal();
        List<RecordNote> expResult = null;
        List<RecordNote> result = instance.getNotes();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of afterUnmarshal method, of class XmlJournal.
     */
    @Test
    public void testAfterUnmarshal() {
        System.out.println("afterUnmarshal");
        Unmarshaller unmarshaller = null;
        Object parent = null;
        XmlJournal instance = new XmlJournal();
        instance.afterUnmarshal(unmarshaller, parent);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
