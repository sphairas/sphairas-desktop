/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thespheres.betula.document.AbstractMarker;

/**
 *
 * @author boris.heithecker
 */
public class XmlCurriculumTest {

    public XmlCurriculumTest() {
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

    @Test
    public void createFile() throws JAXBException {
        
        XmlCurriculum curr = new XmlCurriculum();
//        XmlCourse de = new XmlCourse("DE", new AbstractMarker("niedersachsen.unterricht.faecher", "deutsch", null), null, null);
        XmlCourse de = curr.addEntry("DE", XmlCourse.class);
        de.setSubject(new AbstractMarker("niedersachsen.unterricht.faecher", "deutsch", null));
        de.getDetails();
        de.setPosition(10);
        XmlSection se = new XmlSection("5.2");
        assertEquals(se.getBase(), 5);
        assertEquals(se.getSequence(), 2);

        XmlCourseSelection sel = new XmlCourseSelection(de);
        se.getSelection().add(sel);
        
        XmlSection se2 = new XmlSection("10");
        assertEquals(se2.getBase(), 10);
        assertEquals(se2.getSequence(), 1);
        curr.getSections().add(se);
        JAXBContext jaxb = JAXBContext.newInstance(XmlCurriculum.class, XmlCourseDetailImpl.class);
        Marshaller m = jaxb.createMarshaller();
        m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        m.marshal(curr, System.out);
    }

}
