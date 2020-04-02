/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.parse;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.thespheres.betula.document.AbstractMarker;
import org.thespheres.betula.document.Marker;

/**
 *
 * @author boris.heithecker
 */
public class TranslateIDTest {

    public TranslateIDTest() {
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
    public void testIDs() {
        System.out.println("testIDs");
        Marker fach = new AbstractMarker("conv", "deutsch", null);
        int refJahr = 2014;
        int stufe = 5;
        String kurs = "dh4";

        String id = TranslateID.findId(stufe, refJahr, fach, kurs, "kgs");
        assertEquals("kgs-deutsch-2014-dh4", id);

        stufe = 7;
        id = TranslateID.findId(stufe, refJahr, fach, kurs, "kgs");
        assertEquals("kgs-deutsch-2012-dh4", id);

        id = TranslateID.findId(stufe, refJahr, null, kurs, "kgs");
        assertEquals("kgs-klasse-2012-dh4", id);

        id = TranslateID.findId(stufe, refJahr, 7, true, fach, kurs, "kgs");
        assertEquals("kgs-deutsch-2014-jg7a-dh4", id);

        id = TranslateID.findId(stufe, refJahr, 7, false, fach, kurs, "kgs");
        assertEquals("kgs-deutsch-2014-jg7-dh4", id);

        stufe = 10;
        id = TranslateID.findId(stufe, refJahr, 10, false, fach, kurs, "kgs");
        assertEquals("kgs-deutsch-2014-jg10-dh4", id);

        id = TranslateID.findId(stufe, refJahr, 10, true, fach, kurs, "kgs");
        assertEquals("kgs-deutsch-2014-jg10a-dh4", id);

        id = TranslateID.findId(stufe, refJahr, 10, false, fach, kurs, null);
        assertEquals("deutsch-2014-jg10-dh4", id);

        id = TranslateID.findId(-1, 2016, Integer.MAX_VALUE, false, fach, kurs, null);
        assertEquals("deutsch-2016-dh4", id);

        id = TranslateID.findId(-1, 2016, 0, false, fach, kurs, null);
        assertEquals("deutsch-2016-dh4", id);
    }
}
