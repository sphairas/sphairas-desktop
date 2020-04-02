/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.thespheres.betula.xmldefinitions.XmlAssessmentConventionDefintion;
import org.thespheres.betula.xmldefinitions.XmlGradeItem;

/**
 *
 * @author boris.heithecker
 */
public class XmlAssessmentConventionSupportTest {

    public XmlAssessmentConventionSupportTest() {
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
    public void testCreate() throws Exception {
        XmlAssessmentConventionDefintion def = new XmlAssessmentConventionDefintion("ndschoice1");
        def.setDisplayName("Ankreuznoten 1");
        final String[] ID = {"s", "üs", "ts", "as", "u"};
        final String[] sl = {"ss", "s", "ts", "as", "u"};
        final String[] ll = {"sehr sicher", "sicher", "teilweise sicher", "ansatzweise sicher", "unsicher"};
        for (int i = 0; i < ID.length; i++) {
            XmlGradeItem it = new XmlGradeItem(def, ID[i], sl[i], ll[i]);
            def.getGrades().add(it);
        }
//        Path p = Paths.get("xxxxxxxxxxNdsAnkreuz1.xml");
//        JAXBContext jaxb = JAXBContext.newInstance(XmlAssessmentConventionDefintion.class);
//        Marshaller m = jaxb.createMarshaller();
//        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
//        m.marshal(def, Files.newOutputStream(p));
    }

    @Test
    public void testLoad_InputStream() throws Exception {
    }

    @Test
    public void testLoad_Path() throws Exception {
    }

    @Test
    public void testLoad_URL() throws Exception {
    }

    @Test
    public void testLoad_String() throws Exception {
    }

}
