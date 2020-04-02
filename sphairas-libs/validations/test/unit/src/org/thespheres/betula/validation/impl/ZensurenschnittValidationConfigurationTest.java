/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

import java.util.Arrays;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.thespheres.betula.document.AbstractMarker;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.validation.impl.CareerAwareGradeToDoubleConverter.AdjustCondition;
import org.thespheres.betula.validation.impl.ZensurenschnittValidationConfiguration.Property;

/**
 *
 * @author boris.heithecker
 */
public class ZensurenschnittValidationConfigurationTest {

    public ZensurenschnittValidationConfigurationTest() {
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
    public void testSomeMethod() throws JAXBException {
        ZensurenschnittValidationConfiguration t = new ZensurenschnittValidationConfiguration();
        t.setGradeConvention("de.notensystem");
        Marker[] arr = Arrays.stream(new String[]{"deutsch", "englisch", "mathematik", "französisch", "spanisch"})
                .map(s -> new AbstractMarker("niedersachsen.unterricht.faecher", s, null))
                .toArray(Marker[]::new);
        ListSubjectGroup hf = new ListSubjectGroup("hauptfaecher", arr, "Hauptfächer");
        t.setSubjectGroups(new SubjectFilter[]{hf, SubjectFilter.DEFAULT});
//        final UnbiasFilter unbiasFilter = new UnbiasFilter();
//        t.setGradeFilters(new Matcher[]{unbiasFilter});
        Property unbias = new Property("unbias", "true");
        t.setProperties(new Property[]{unbias});
        final CareerAwareGradeToDoubleConverter c = new CareerAwareGradeToDoubleConverter("kgs.schulzweige");
        AdjustCondition p0 = new AdjustCondition(new String[]{"gy-hs"}, -2);
        AdjustCondition p1 = new AdjustCondition(new String[]{"rs-hs", "gy-rs"}, -1);
        AdjustCondition p2 = new AdjustCondition(new String[]{"hs-rs", "rs-gy"}, 1);
        AdjustCondition p3 = new AdjustCondition(new String[]{"hs-gy"}, 2);
        c.setConditions(new AdjustCondition[]{p0, p1, p2, p3});
        t.setGradeDoubleConverter(c);

        JAXBContext ctx = JAXBContext.newInstance(ZensurenschnittValidationConfiguration.class, CareerAwareGradeToDoubleConverter.class);
        Marshaller m = ctx.createMarshaller();
        m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        m.marshal(t, System.out);
    }

}
