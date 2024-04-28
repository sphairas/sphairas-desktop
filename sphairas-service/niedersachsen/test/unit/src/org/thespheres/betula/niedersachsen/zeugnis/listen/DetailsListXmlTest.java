/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis.listen;

import java.io.ByteArrayOutputStream;
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
import org.thespheres.betula.assess.AbstractGrade;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.listprint.Formatter;
import org.thespheres.betula.niedersachsen.Faecher;
import org.thespheres.betula.niedersachsen.Profile;
import org.thespheres.betula.niedersachsen.NdsTerms;
import org.thespheres.betula.services.scheme.spi.Term;

/**
 *
 * @author boris.heithecker
 */
public class DetailsListXmlTest {

    public DetailsListXmlTest() {
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
    public void testSetFooterCenter() throws Exception {
        System.out.println("setFooterCenter");
        DetailsListXml instance = new DetailsListXml();
        instance.setFooterCenter("Fuß");
        addDetails1(instance);
        addDetails2(instance);
        addDetails3(instance);
        addDetails4(instance);
        TransformerFactory tf = TransformerFactory.newInstance();
        final InputStream is = DetailsListXml.class.getResourceAsStream("details.fo.xsl");
        Templates t;
        try {
            t = tf.newTemplates(new StreamSource(is));
        } catch (TransformerConfigurationException ex) {
            throw ex;
        }
        DOMResult r;
        try {
            JAXBContext jaxb = JAXBContext.newInstance(DetailsListXml.class);
            r = new DOMResult();
            jaxb.createMarshaller().marshal(instance, r);
        } catch (JAXBException ex) {
            throw ex;
        }
        Formatter f = createFormatter();
        final byte[] out;
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            f.transform(new DOMSource(r.getNode()), t, baos, "application/pdf");
            out = baos.toByteArray();
        } catch (IOException ex) {
            throw ex;
        }
        final Path outPath = Paths.get("test.pdf");
//        Files.write(outPath, out);
//        System.out.println(outPath.toAbsolutePath().toString());
        System.out.println("Out length: " + out.length);
//        assertTrue(out.length == 7036);
    }

    private void addDetails1(DetailsListXml instance) {
        StudentDetailsXml details = new StudentDetailsXml();
        details.setListDate("date");
        details.setListName("Schüler-Name");
        Term t1 = NdsTerms.getTerm(2015, 2);
        Term t2 = NdsTerms.getTerm(2016, 1);
        StudentDetailsXml.TermDataLine l = details.addLine(0, t1.getDisplayName());
        instance.list.add(details);
        l.setStudentHint("hint");
        l.setNote("Notiz zur Zeile");
        Faecher facher = new Faecher();
        Marker[] ff = facher.getAllMarkers();
        for (int i = 5; i < 10; i++) {
            Grade g = new AbstractGrade("conv", Integer.toString(i));
            final Set<Marker> hs = new HashSet<>();
            hs.add(ff[i]);
            details.setValue(l, 0, hs, g, "gn");
        }
//        Profile pp = new Profile();
//        for (int i = 0; i < pp.getAllMarkers().length; i++) {
//            Grade g = new AbstractGrade("conv", Integer.toString(i));
//            final Set<Marker> hs = new HashSet<>();
//            hs.add(pp.getAllMarkers()[i]);
//            StudentDetailsXml.ColumnValue cv = details.setValue(l, 2, hs, g, "gn");
//            cv.setLabelLeft("lbl");
//            cv.setColor("red");
//        }
        details.addText("Header", 1000).setValue("text");
//        details.setSubjectColumnWidth("8mm");
//        details.setUseShortLabel(true);
    }

    private void addDetails2(DetailsListXml instance) {
        StudentDetailsXml details = new StudentDetailsXml();
        details.setListDate("date");
        details.setListName("Schüler-Name");
        Term t1 = NdsTerms.getTerm(2015, 2);
        Term t2 = NdsTerms.getTerm(2016, 1);
        StudentDetailsXml.TermDataLine l = details.addLine(0, t1.getDisplayName());
        instance.list.add(details);
        l.setStudentHint("hint");
        l.setNote("Notiz zur Zeile");
        Faecher facher = new Faecher();
        Marker[] ff = facher.getAllMarkers();
        for (int i = 5; i < 10; i++) {
            Grade g = new AbstractGrade("conv", Integer.toString(i));
            final Set<Marker> hs = new HashSet<>();
            hs.add(ff[i]);
            details.setValue(l, 0, hs, g, "gn");
        }
        Profile pp = new Profile();
        for (int i = 0; i < pp.getAllMarkers().length; i++) {
            Grade g = new AbstractGrade("conv", Integer.toString(i));
            final Set<Marker> hs = new HashSet<>();
            hs.add(pp.getAllMarkers()[i]);
            StudentDetailsXml.ColumnValue cv = details.setValue(l, 2, hs, g, "gn");
            cv.setLabelLeft("lbl");
            cv.setColor("red");
        }
        details.addText("Header", 1000).setValue("text");
//        details.setSubjectColumnWidth("8mm");
//        details.setUseShortLabel(true);
    }

    private void addDetails3(DetailsListXml instance) {
        StudentDetailsXml details = new StudentDetailsXml();
        details.setListDate("date");
        details.setListName("Schüler-Name");
        Term t1 = NdsTerms.getTerm(2015, 2);
        Term t2 = NdsTerms.getTerm(2016, 1);
        StudentDetailsXml.TermDataLine l = details.addLine(0, t1.getDisplayName());
        instance.list.add(details);
        l.setStudentHint("hint");
        l.setNote("Notiz zur Zeile");
        Faecher facher = new Faecher();
        Marker[] ff = facher.getAllMarkers();
        for (int i = 5; i < 10; i++) {
            Grade g = new AbstractGrade("conv", Integer.toString(i));
            final Set<Marker> hs = new HashSet<>();
            hs.add(ff[i]);
            details.setValue(l, 0, hs, g, "gn");
        }
//        Profile pp = new Profile();
//        for (int i = 0; i < pp.getAllMarkers().length; i++) {
//            Grade g = new AbstractGrade("conv", Integer.toString(i));
//            final Set<Marker> hs = new HashSet<>();
//            hs.add(pp.getAllMarkers()[i]);
//            StudentDetailsXml.ColumnValue cv = details.setValue(l, 2, hs, g, "gn");
//            cv.setLabelLeft("lbl");
//            cv.setColor("red");
//        }
        details.addText("Header", 1000).setValue("text");
        details.setSubjectColumnWidth("8mm");
        details.setUseShortLabel(true);
    }

    private void addDetails4(DetailsListXml instance) {
        StudentDetailsXml details = new StudentDetailsXml();
        details.setListDate("date");
        details.setListName("Schüler-Name");
        Term t1 = NdsTerms.getTerm(2015, 2);
        Term t2 = NdsTerms.getTerm(2016, 1);
        StudentDetailsXml.TermDataLine l = details.addLine(0, t1.getDisplayName());
        instance.list.add(details);
        l.setStudentHint("hint");
        l.setNote("Notiz zur Zeile");
        Faecher facher = new Faecher();
        Marker[] ff = facher.getAllMarkers();
        for (int i = 5; i < 10; i++) {
            Grade g = new AbstractGrade("conv", Integer.toString(i));
            final Set<Marker> hs = new HashSet<>();
            hs.add(ff[i]);
            details.setValue(l, 0, hs, g, "gn");
        }
        Profile pp = new Profile();
        for (int i = 0; i < pp.getAllMarkers().length; i++) {
            Grade g = new AbstractGrade("conv", Integer.toString(i));
            final Set<Marker> hs = new HashSet<>();
            hs.add(pp.getAllMarkers()[i]);
            StudentDetailsXml.ColumnValue cv = details.setValue(l, 2, hs, g, "gn");
            cv.setLabelLeft("lbl");
            cv.setColor("red");
        }
        details.addText("Header", 1000).setValue("text");
        details.setSubjectColumnWidth("8mm");
        details.setUseShortLabel(true);
    }

    private static Formatter createFormatter() {
        Path p = Paths.get(System.getProperty("user.home"));
        URI base = p.toUri();
        return Formatter.create(base);
    }
}
