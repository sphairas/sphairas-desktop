package org.thespheres.betula.niedersachsen.xml;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.ws.CommonDocuments;

/**
 *
 * @author boris.heithecker
 */
public class NdsZeugnisSchulvorlageTest {

    public NdsZeugnisSchulvorlageTest() {
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
        JAXBContext ctx = JAXBContext.newInstance(NdsZeugnisSchulvorlage.class);
        NdsZeugnisSchulvorlage i = new NdsZeugnisSchulvorlage("www.meine-schule-in-xxx.de");
        i.getLogos().setRight("url('logo_35_25sw.png')");
        i.setSchoolName2("– Schule für alle –");
        i.setSchoolName("Schillerschule");
        i.setSchoolLocation("Irgendwo");

        final String AUTHORITY = "www.meine-schule-in-xxx.de/tstest";
        final DocumentId STUDENT_BILDUNGSGANG_DOCID = new DocumentId(AUTHORITY, "students-bildungsgang", DocumentId.Version.LATEST);
        i.documents().put(CommonDocuments.STUDENT_CAREERS_DOCID, STUDENT_BILDUNGSGANG_DOCID);
//        final DocumentId KLASSENLEHRER_DOCID = new DocumentId(AUTHORITY, "klassenlehrer", DocumentId.Version.LATEST);
        final DocumentId AG_NAMEN_DOCID = new DocumentId(AUTHORITY, "arbeitsgemeinschaften", DocumentId.Version.LATEST);
        i.documents().put(CommonDocuments.COMMON_NAMES_DOCID, AG_NAMEN_DOCID);
        final DocumentId SUBJECTS_DOCID = new DocumentId(AUTHORITY, "base-target-assessment-entity-subjects", DocumentId.Version.LATEST);
        i.documents().put(CommonDocuments.SUBJECT_NAMES_DOCID, SUBJECTS_DOCID);

        Marshaller m = ctx.createMarshaller();
        m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        m.marshal(i, System.out);
    }

}
