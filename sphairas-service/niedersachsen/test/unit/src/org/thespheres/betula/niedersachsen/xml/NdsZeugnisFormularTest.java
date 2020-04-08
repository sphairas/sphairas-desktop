/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.xml;

import org.thespheres.betula.niedersachsen.NdsZeugnisFormular;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.niedersachsen.NdsZeugnisFormular.Area;
import org.thespheres.betula.niedersachsen.NdsZeugnisFormular.ZeugnisMappe;
import org.thespheres.betula.niedersachsen.zeugnis.NdsReportBuilderFactory;
import org.thespheres.betula.niedersachsen.zeugnis.ZeugnisData;
import org.thespheres.betula.xmldefinitions.XmlMarkerConventionDefinition;
import org.thespheres.betula.xmldefinitions.XmlMarkerDefinition;

/**
 *
 * @author boris.heithecker
 */
public class NdsZeugnisFormularTest {

    public NdsZeugnisFormularTest() {
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
    public void testZeugnisformular() throws JAXBException, TransformerConfigurationException, TransformerException, IOException, ParserConfigurationException {
        JAXBContext ctx = JAXBContext.newInstance(ZeugnisMappe.class);
        TransformerFactory factory = TransformerFactory.newInstance(); //newInstance("net.sf.saxon.TransformerFactoryImpl", null);//
//        final InputStream is = ZeugnisData.class.getResourceAsStream(NdsReportBuilderFactory.getXslLocation());
//        Templates template = factory.newTemplates(new StreamSource(is));

        final InputStream idis = NdsZeugnisFormularTest.class.getResourceAsStream("identity.xsl");
        Templates idtemplate = factory.newTemplates(new StreamSource(idis));

        NdsZeugnisFormular i = new NdsZeugnisFormular("Doppelbiß, Nora Marie");
        i.getKopf().setStudentName("Nora Marie Doppelbiß");
        i.getKopf().setSchoolname("Meine Schule");
        i.getKopf().setReportTitle("Zeugnis");
        i.getKopf().setDatePlaceOfBirth("geboren am 24. Dezember 2010", "auf Helgoland");
        i.getKopf().setDaysAbsent("versäumte Unterrichtstag im 1. Halbjahr: 1", "davon unentschuldigt: 0");
        i.getKopf().getTermData().setLeft("Schuljahr 2018/2019");
        i.getKopf().getTermData().setCenter("1. Halbjahr");
        i.getKopf().getTermData().setRight("Klasse 2b");
        i.getKopf().setPrimaryUnit(new UnitId("authority", "unit-id"));
        i.setPlaceDate("Irgendwo, den 31. Januar 1899");
        i.setSecondPageHeader("Zeugnis für Nora Marie Doppelbiß");
        i.getKopf().setImageRight("jjjj");
        i.getKopf().setSchoolname2("Schulname Zwei");
        i.getKopf().setDivison("Schulzweig");
        
        i.getKopf().setImageWidth("10mm");
        i.setTemplate("Alleschule");

        Area a = new Area(null);
        //
        NdsZeugnisFormular.Line l = a.addLine("Religion", "Sport");
        l.getFieldLinks().setValue("3");
        l.getFieldRechts().setValue("2");
        i.getAreas().add(a);
        //
        NdsZeugnisFormular.Note n2 = i.addNote("Arbeitsverhalten", 2000);
        n2.setValue("Noras Arbeitsverhalten entspricht den Erwartungen mit Einschränkungen.");
        NdsZeugnisFormular.Note n3 = i.addNote("Sozialverhalten", 3000);
        n3.setValue("Noras Sozialverhalten entspricht den Erwartungen.");
        NdsZeugnisFormular.Note n4 = i.addNote("Bemerkungen", 4000);
        n4.setValue("Nora Marie wird nach dem Förderschwerpunkt Lernen unterricht.");
        //
        NdsZeugnisFormular.CrossMarkArea cma = new NdsZeugnisFormular.CrossMarkArea();
        i.setCrossMarkArea(cma);
        //
        JAXBContext jaxb = JAXBContext.newInstance(XmlMarkerConventionDefinition.class);
//                XmlMarkerConventionDefinition t = (XmlMarkerConventionDefinition) jaxb.createUnmarshaller().unmarshal(NdsZeugnisFormularTest.class.getResource("Ersatzfach.xml"));
        XmlMarkerConventionDefinition de = (XmlMarkerConventionDefinition) jaxb.createUnmarshaller().unmarshal(NdsZeugnisFormularTest.class.getResource("DeutschAnkreuz2.xml"));
        XmlMarkerConventionDefinition ma = (XmlMarkerConventionDefinition) jaxb.createUnmarshaller().unmarshal(NdsZeugnisFormularTest.class.getResource("MatheAnkreuz2.xml"));

        NdsZeugnisFormular.CrossMarkSubject cmsde = new NdsZeugnisFormular.CrossMarkSubject(de.getDisplayName());
        cma.getSubjects().add(cmsde);
        for (XmlMarkerConventionDefinition.XmlMarkerSubsetDefinition s : de.getMarkerSubsets()) {
            NdsZeugnisFormular.CrossMarkLine h = new NdsZeugnisFormular.CrossMarkLine(s.getCategory(), 1);
            cmsde.getLines().add(h);
            for (XmlMarkerDefinition m : s.getMarkerDefinitions()) {
                NdsZeugnisFormular.CrossMarkLine cml = new NdsZeugnisFormular.CrossMarkLine(m.getLongLabel());
                cml.setPosition(3);
                cmsde.getLines().add(cml);
            }
        }
        NdsZeugnisFormular.CrossMarkSubject cmsma = new NdsZeugnisFormular.CrossMarkSubject(ma.getDisplayName());
        cma.getSubjects().add(cmsma);
        for (XmlMarkerConventionDefinition.XmlMarkerSubsetDefinition s : ma.getMarkerSubsets()) {
            NdsZeugnisFormular.CrossMarkLine h = new NdsZeugnisFormular.CrossMarkLine(s.getCategory(), 1);
            cmsma.getLines().add(h);
            for (XmlMarkerDefinition m : s.getMarkerDefinitions()) {
                NdsZeugnisFormular.CrossMarkLine cml = new NdsZeugnisFormular.CrossMarkLine(m.getLongLabel());
                cml.setPosition(3);
                cmsma.getLines().add(cml);
            }
        }
        //
        //
        ZeugnisMappe coll = new ZeugnisMappe();
        coll.add(i);
        Marshaller m = ctx.createMarshaller();
        m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        DOMResult result = new DOMResult();
        m.marshal(coll, result);
        m.marshal(coll, System.out);

        Transformer t2 = idtemplate.newTransformer();
        DOMSource src2 = new DOMSource(result.getNode());

        t2.setParameter("versionParam", "2.0");
        // Setup input for XSLT transformation

        StreamResult finalres2 = new StreamResult(System.out);
        t2.setOutputProperty(OutputKeys.INDENT, "yes");
        t2.transform(src2, finalres2);

//        Transformer transformer = template.newTransformer();
//        // Set the value of a <param> in the stylesheet
//        transformer.setParameter("versionParam", "2.0");
//        // Setup input for XSLT transformation
//        Source src = new DOMSource(result.getNode());
//        // Resulting SAX events (the generated FO) must be piped through to FOP
//        DOMResult res = new DOMResult();
//        // Start XSLT transformation and FOP processing
//        transformer.transform(src, res);
//        transformer = factory.newTransformer();
//        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//        src = new DOMSource(res.getNode());
//        StreamResult finalres = new StreamResult(System.out);
//        transformer.transform(src, finalres);

    }

}
