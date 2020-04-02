/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.web;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author boris.heithecker
 */
public class XmlWebUIConfigurationTest {

    public XmlWebUIConfigurationTest() {
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
    public void testXml() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(XmlWebUIConfiguration.class);
        XmlWebUIConfiguration i = new XmlWebUIConfiguration("www.xxxxxxxxx.de");
        i.setLogoResource("logo.jpg");
        String[] ett = new String[]{"quartalsnoten", "zeugnisnoten", "arbeitsverhalten", "sozialverhalten"};
        i.setEditableTargetTypes(ett);
        i.setDefaultEditingTargetType("zeugnisnoten");
        String[] ltt = new String[]{"zeugnisnoten", "arbeitsverhalten", "sozialverhalten", "vorzensuren"};
        i.setPrimaryUnitListedTargetTypes(ltt);
        i.setLoginProviderDisplayLabel("dl");
        i.setProperty("report.notes.dialog.enabled", "true");
        Marshaller m = ctx.createMarshaller();
        m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        m.marshal(i, System.out);
    }

}
