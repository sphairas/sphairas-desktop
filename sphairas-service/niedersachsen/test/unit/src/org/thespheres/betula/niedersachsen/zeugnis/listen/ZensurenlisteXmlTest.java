/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis.listen;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thespheres.betula.assess.AbstractGrade;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.listprint.Formatter;
import org.thespheres.betula.niedersachsen.Faecher;
import org.thespheres.betula.niedersachsen.Profile;

/**
 *
 * @author boris.heithecker
 */
public class ZensurenlisteXmlTest {

    public ZensurenlisteXmlTest() {
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
    public void testSetFooterCenter() {
        System.out.println("setFooterCenter");
        ZensurenListenCollectionXml instance = new ZensurenListenCollectionXml();
        instance.setFooterCenter("Fuß");
        ZensurenListeXml list = new ZensurenListeXml();
        list.setListDate("date");
        list.setListName("Liste");
        instance.LISTS.add(list);
        Faecher facher = new Faecher();
        Marker[] ff = facher.getAllMarkers();
        ZensurenListeXml.DataLineXml l = list.addLine("Name");
//        StudentDetailsXml.TermDataLine l = list.addLine(0, t1.getDisplayName());
        l.setStudentHint("hint");
        l.setNote("note");
        for (int i = 5; i < ff.length; i++) {
            Grade g = new AbstractGrade("conv", Integer.toString(i));
            final Set<Marker> hs = new HashSet<>();
            hs.add(ff[i]);
            list.setValue(l, 0, hs, g, "gn");
        }
        for (int i = 5; i < ff.length; i++) {
            Grade g = new AbstractGrade("conv", Integer.toString(i) + "X");
            final Set<Marker> hs = new HashSet<>();
            hs.add(ff[i]);
            list.setValue(l, 0, hs, g, "gn");
        }
        Profile pp = new Profile();
        for (int i = 0; i < pp.getAllMarkers().length; i++) {
            Grade g = new AbstractGrade("conv", Integer.toString(i));
            final Set<Marker> hs = new HashSet<>();
            hs.add(pp.getAllMarkers()[i]);
            list.setValue(l, 2, hs, g, "gn");
        }
        //
        ZensurenListeXml.DataLineXml l2 = list.addLine("Name2");
//        StudentDetailsXml.TermDataLine l = list.addLine(0, t1.getDisplayName());
        l2.setStudentHint("hint2");
        l2.setNote("note2");
        for (int i = 5; i < ff.length; i++) {
            final Set<Marker> hs = new HashSet<>();
            Grade g = new AbstractGrade("conv", Integer.toString(i) + ff[i].getId().substring(0, 1));
            hs.add(ff[i]);
            list.setValue(l2, 0, hs, g, "gn");
        }
        //
        final Set<Marker> hs = new HashSet<>();
        hs.add(ff[5]);
        Grade g = new AbstractGrade("conv", "Z" + ff[5].getShortLabel());
        list.setValue(l2, 0, hs, g, "gn");
        //
//        list.addText("Header", 1000).setValue("jdhsfkladhsf iouehrw");
        TransformerFactory tf = TransformerFactory.newInstance();
        final InputStream is = DetailsListXml.class.getResourceAsStream("listen.fo.xsl");
        Templates t = null;
        try {
            t = tf.newTemplates(new StreamSource(is));
        } catch (TransformerConfigurationException ex) {
            fail("The test case is a prototype.");
        }
        DOMResult r = null;
        try {
            JAXBContext jaxb = JAXBContext.newInstance(ZensurenListenCollectionXml.class);
            r = new DOMResult();
            jaxb.createMarshaller().marshal(instance, r);
        } catch (JAXBException jAXBException) {
            fail("The test case is a prototype.");
        }
        Formatter f = createFormatter();
        try {
            f.transform(new DOMSource(r.getNode()), t, System.out, "application/pdf");
        } catch (IOException ex) {
            System.out.println(ex);
            fail("The test case is a prototype.");
        }
    }

    private static Formatter createFormatter() {
        Path p = Paths.get(System.getProperty("user.home"));
        URI base = p.toUri();
        return Formatter.create(base);
    }
}
