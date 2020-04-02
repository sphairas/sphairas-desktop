/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.action;

import java.io.IOException;
import java.util.Properties;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.thespheres.betula.tableimport.csv.XmlCsvDictionary;
import org.thespheres.betula.tableimport.impl.ConfigurableImportTargetHelper;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.model.XmlImport;
import org.thespheres.betula.xmlimport.model.XmlTargetEntryItem;
import org.thespheres.betula.xmlimport.model.XmlTargetItem;

/**
 *
 * @author boris.heithecker
 */
public class XmlImportTest {

    public XmlImportTest() {
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
    public void testFile() {
        try {
            JAXBContext JAXB = JAXBContext.newInstance(XmlImport.class, XmlTargetEntryItem.class);
            XmlImport xi2 = new XmlImport();
            final Product product = new Product("test");
            product.setDisplay("display");
            xi2.getProducts().add(product);
            XmlTargetItem xti = new XmlTargetItem();
            xti.setAssessmentConvention("conv");
            xi2.getItems().add(xti);
            JAXB.createMarshaller().marshal(xi2, System.out);
        } catch (JAXBException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Test
    public void testDictionary() throws IOException {
        try {
            JAXBContext JAXB = JAXBContext.newInstance(XmlCsvDictionary.class);
            Properties p = new Properties();
            p.load(ConfigurableImportTargetHelper.class.getResourceAsStream("column-labels.properties"));
            XmlCsvDictionary.Entry[] arr = p.entrySet().stream()
                    .map(e -> new XmlCsvDictionary.Entry((String) e.getKey(), (String) e.getValue()))
                    .toArray(XmlCsvDictionary.Entry[]::new);
            XmlCsvDictionary dict = new XmlCsvDictionary("default", arr);
            Marshaller m = JAXB.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(dict, System.out);
        } catch (JAXBException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
