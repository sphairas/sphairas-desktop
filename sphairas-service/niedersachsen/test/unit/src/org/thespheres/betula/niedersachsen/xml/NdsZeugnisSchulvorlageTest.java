/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.thespheres.betula.assess.AbstractGrade;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.niedersachsen.xml.NdsZeugnisSchulvorlage.Coloring;
import org.thespheres.betula.niedersachsen.xml.NdsZeugnisSchulvorlage.Property;
import org.thespheres.betula.services.ws.CommonDocuments;

/**
 *
 * @author boris.heithecker
 */
public class NdsZeugnisSchulvorlageTest {

    public NdsZeugnisSchulvorlageTest() {
    }

    @Test
    public void testXml() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(NdsZeugnisSchulvorlage.class);
        NdsZeugnisSchulvorlage i = new NdsZeugnisSchulvorlage("www.meine-schule-in-xxx.de");
        i.getLogos().setRight("url('logo_35_25sw.png')");
        i.setSchoolName2(new NdsZeugnisSchulvorlage.Subtitle[]{new NdsZeugnisSchulvorlage.Subtitle("– Schule für alle –", "9pt")});
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

        i.setProperty("property.key", "property-value");
        String v = i.getProperty("property.key")
                .map(Property::getValue)
                .orElse(null);
        assertEquals("property-value", v);
        String v2 = i.getProperty("property2.key")
                .map(Property::getValue)
                .orElse(null);
        assertNull(v2);

        final Coloring col1 = new Coloring("red", "cnv", new String[]{"a", "b"});
        i.getColorings().add(col1);
        final Coloring col2 = new Coloring("blue", "cnv2", null);
        i.getColorings().add(col2);

        Marshaller m = ctx.createMarshaller();
        m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        m.marshal(i, System.out);

        final byte[] res;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            m.marshal(i, baos);
            res = baos.toByteArray();
        }
        final NdsZeugnisSchulvorlage p;
        try (ByteArrayInputStream is = new ByteArrayInputStream(res)) {
            p = (NdsZeugnisSchulvorlage) ctx.createUnmarshaller().unmarshal(is);
        }

        String vv = p.getProperty("property.key")
                .map(Property::getValue)
                .orElse(null);
        assertEquals("property-value", vv);
        String vv2 = p.getProperty("property2.key")
                .map(Property::getValue)
                .orElse(null);
        assertNull(vv2);

        String c1 = p.getColoring(new AbstractGrade("cnv", "a"));
        assertEquals("red", c1);
        String c2 = p.getColoring(new AbstractGrade("cnv", "c"));
        assertNull(c2);
        String c3 = p.getColoring(new AbstractGrade("cnv2", "b"));
        assertEquals("blue", c3);
    }

}
