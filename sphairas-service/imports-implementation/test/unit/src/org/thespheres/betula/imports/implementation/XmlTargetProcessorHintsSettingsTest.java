/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.imports.implementation;

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
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.xmlimport.model.XmlTargetImportSettings;
import org.thespheres.betula.xmlimport.model.XmlTargetSelector;

/**
 *
 * @author boris.heithecker
 */
public class XmlTargetProcessorHintsSettingsTest {

    public XmlTargetProcessorHintsSettingsTest() {
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
    public void testGetTargetDefaults() throws JAXBException {
        System.out.println("getTargetDefaults");
        JAXBContext jaxb = null;
        try {
            jaxb = JAXBContext.newInstance(XmlTargetProcessorHintsSettings.class, XmlMarkerTargetSelector.class);
        } catch (JAXBException ex) {
            fail(ex.getLocalizedMessage());
        }

        XmlTargetProcessorHintsSettings n = new XmlTargetProcessorHintsSettings();

        XmlMarkerTargetSelector sel = new XmlMarkerTargetSelector("nicht-ag");
        sel.setMarkers(new Marker[]{new AbstractMarker("kgs.unterricht", "ag", null)});
        sel.setType(XmlMarkerTargetSelector.Type.ABSENCE);
        XmlTargetSelector[] ss = new XmlTargetSelector[]{sel};

        n.setTargetSelectors(ss);

        XmlTargetProcessorHintsSettings.Hint h = new XmlTargetProcessorHintsSettings.Hint("process-bulk", "true");

        XmlTargetProcessorHintsSettings.Hint hag = new XmlTargetProcessorHintsSettings.Hint("update-pu-links", "true");
        hag.setSelector(sel);

        XmlTargetImportSettings.TargetDefault tdef = new XmlTargetImportSettings.TargetDefault("zeugnisnoten");
        tdef.setSelector(ss);

        XmlTargetImportSettings.TargetDefault[] td = new XmlTargetImportSettings.TargetDefault[]{tdef};

        n.setHints(new XmlTargetProcessorHintsSettings.Hint[]{h, hag});
        final Marshaller m = jaxb.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(n, System.out);
    }

    @Test
    public void testWrite() {
        JAXBContext jaxb = null;
        try {
            jaxb = JAXBContext.newInstance(XmlTargetImportSettings.class, XmlMarkerTargetSelector.class, StufeTargetSelector.class);
        } catch (JAXBException ex) {
            fail(ex.getLocalizedMessage());
        }
        XmlMarkerTargetSelector sel = new XmlMarkerTargetSelector("nicht-ag");
        sel.setMarkers(new Marker[]{new AbstractMarker("kgs.unterricht", "ag", null)});
        sel.setType(XmlMarkerTargetSelector.Type.ABSENCE);
        XmlTargetSelector[] ss = new XmlTargetSelector[]{sel};
        
        XmlTargetImportSettings n = new XmlTargetImportSettings();        
        n.setTargetSelectors(ss);

        XmlTargetImportSettings.TargetDefault tdef = new XmlTargetImportSettings.TargetDefault("zeugnisnoten");
        tdef.setSelector(ss);

        XmlTargetImportSettings.TargetDefault[] td = new XmlTargetImportSettings.TargetDefault[]{tdef};
        n.setTargetDefaults(td);

        try {
            final Marshaller m = jaxb.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(n, System.out);
        } catch (JAXBException jAXBException) {
        }
    }

}
