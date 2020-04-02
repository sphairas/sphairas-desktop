/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.gs;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.thespheres.betula.document.AbstractMarker;

/**
 *
 * @author boris.heithecker
 */
public class CrossmarkSettingsTest {
    
    public CrossmarkSettingsTest() {
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
    public void testFile() throws JAXBException {
        JAXBContext jaxb = JAXBContext.newInstance(CrossmarkSettings.class);
        CrossmarkSettings i = new CrossmarkSettings();
        CrossmarkSettings.Mapping m = new CrossmarkSettings.Mapping(1, "gs.xxx.textzeugnisse.klasse2.deutsch", new AbstractMarker("niedersachsen.unterricht.faecher", "deutsch", null));
        i.setMappings(new CrossmarkSettings.Mapping[]{m});
        Marshaller ms = jaxb.createMarshaller();
        ms.setProperty("jaxb.formatted.output", Boolean.TRUE);
        ms.marshal(i, System.out);
    }
    
}
