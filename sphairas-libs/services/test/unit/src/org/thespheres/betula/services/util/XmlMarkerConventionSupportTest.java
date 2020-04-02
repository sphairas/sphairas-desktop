/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.thespheres.betula.xmldefinitions.XmlMarkerConventionDefinition;

/**
 *
 * @author boris.heithecker
 */
public class XmlMarkerConventionSupportTest {
    
    public XmlMarkerConventionSupportTest() {
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
        Map<String, String> props = new HashMap<>();
        JAXBContext jaxb = JAXBContext.newInstance(XmlMarkerConventionDefinition.class);
        InputStream is = XmlMarkerConventionSupportTest.class.getResourceAsStream("testConvention.xml");
        jaxb.createUnmarshaller().unmarshal(is);
        is.close();
        props.put("resource", "/org/thespheres/betula/services/util/testConvention.xml");
        XmlMarkerConventionDefinition ret = XmlMarkerConventionSupport.create(props);
    }

    @Test
    public void testLoad_URL() throws Exception {
    }

    @Test
    public void testLoad_String() throws Exception {
    }
    
}
