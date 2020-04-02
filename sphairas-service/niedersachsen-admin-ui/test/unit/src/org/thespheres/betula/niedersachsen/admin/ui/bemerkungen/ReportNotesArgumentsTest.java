/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author boris.heithecker
 */
public class ReportNotesArgumentsTest {

    public ReportNotesArgumentsTest() {
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
    public void testWriteSample() throws JAXBException {
        JAXBContext jaxb = null;
        try {
            jaxb = JAXBContext.newInstance(ReportNotesArguments.class);
        } catch (JAXBException ex) {
            fail(ex.getLocalizedMessage());
        }
        ReportNotesArguments rna = new ReportNotesArguments();
        ReportNotesArguments.Argument n = rna.addArgument("name", 0);
        n.setDisplayName("Vorname");
        ReportNotesArguments.Argument ng = rna.addArgument("name-genitiv", 1);
        ng.setDisplayName("Vorname Genitiv");
        ReportNotesArguments.Argument pp = rna.addArgument("possesiv-pronomen", 2);
        pp.setDisplayName("Possesivpronomen");
        ReportNotesArguments.Argument zkd = rna.addArgument("datum-zeungniskonferenz", 3);
        zkd.setDisplayName("Datum Zeugniskonferenz");
        ReportNotesArguments.Argument ns = rna.addArgument("nächste-stufe", 4);
        ns.setDisplayName("Nächste Stufe");
        ReportNotesArguments.Argument nsj = rna.addArgument("nächstes-schuljahr", 5);
        nsj.setDisplayName("Nächstes Schuljahr");
        ReportNotesArguments.Argument g = rna.addArgument("geschlecht", 6);
        g.setDisplayName("Geschlecht");
        ReportNotesArguments.Argument ppg = rna.addArgument("possesiv-pronomen-genitiv", 7);
        ppg.setDisplayName("Possesivpronomen Genitiv");
        final Marshaller m = jaxb.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(rna, System.out);
    }
}
