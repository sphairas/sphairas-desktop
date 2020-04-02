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
import org.thespheres.betula.validation.impl.CareerAwareGradeCondition.Pairing;
import org.thespheres.betula.validation.impl.Policy.GroupingCondition;
import org.thespheres.betula.validation.impl.VersetzungsValidationConfiguration.Property;

/**
 *
 * @author boris.heithecker
 */
public class VersetzungsValidationConfigurationTest {

    public VersetzungsValidationConfigurationTest() {
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
        VersetzungsValidationConfiguration t = new VersetzungsValidationConfiguration();
        t.setGradeConvention("de.notensystem");
        Marker[] arr = Arrays.stream(new String[]{"deutsch", "englisch", "mathematik", "französisch", "spanisch"})
                .map(s -> new AbstractMarker("niedersachsen.unterricht.faecher", s, null))
                .toArray(Marker[]::new);
        ListSubjectGroup hf = new ListSubjectGroup("hauptfaecher", arr, "Hauptfächer");
        t.setSubjectGroups(new SubjectFilter[]{hf, SubjectFilter.DEFAULT});
//        final UnbiasFilter unbiasFilter = new UnbiasFilter();
//        t.setGradeFilters(new Matcher[]{unbiasFilter});
        Property unbias = new Property("unbias", "true");
        Property matchShortLabel = new Property("match.short.label", "true");
        //
        Policy pass = new Policy("versetzt");
//        pass.setGradeFilters(new Matcher[]{unbiasFilter});
        pass.setMarkers(new Marker[]{new AbstractMarker("mc", "test-id", null)});
//pass.setMarkers(new MarkerAdapter[]{new MarkerAdapter("mc", "testPairs-id", null)});
        GradeCondition c1 = new GradeCondition("5");
        c1.setMaxOccurrence(1);
        GradeCondition c2 = new GradeCondition("6");
        c2.setMaxOccurrence(0);
        pass.setConditions(new GradeCondition[]{c1, c2});
        pass.setProperties(new Property[]{unbias, matchShortLabel});
        //
//        PolicyLegalHint h
        //
        Policy passOnSanction = new Policy("2-mangelhaft-ausgleich");
        GroupingCondition gc = new GroupingCondition(null, new SubjectFilter[]{hf, SubjectFilter.DEFAULT});
//        GradeCondition c3 = new GradeCondition("5");
//        c3.setPair(new String[]{"1", "2", "3"});
        CareerAwareGradeCondition kgs = new CareerAwareGradeCondition("kgs.schulzweige", "5");
        kgs.setDefaultPair(new String[]{"1", "2", "3"});
        Pairing p1 = new Pairing(new String[]{"rs-hs", "gy-rs"}, new String[]{"1", "2"});
        Pairing p2 = new Pairing(new String[]{"hs-rs", "rs-gy"}, new String[]{"1", "2", "3", "4"});
        kgs.setPairing(new Pairing[]{p1, p2});
        gc.setConditions(new Condition[]{kgs});
        passOnSanction.setGroupingCondition(new GroupingCondition[]{gc});
        GradeCondition c41 = new GradeCondition("5");
        c41.setMaxOccurrence(2);
        GradeCondition c4 = new GradeCondition("6");
        c4.setMaxOccurrence(0);
        passOnSanction.setConditions(new GradeCondition[]{c4, c41});
        passOnSanction.setProperties(new Property[]{unbias, matchShortLabel});
        //
        Policy passOnSanctionMit6_2 = new Policy("1-ungenügend-1-ausgleich");
        GroupingCondition gc2 = new GroupingCondition(null, new SubjectFilter[]{hf, SubjectFilter.DEFAULT});
        GradeCondition c6 = new GradeCondition("6");
        c6.setPair(new String[]{"1", "2"});
        gc2.setConditions(new GradeCondition[]{c6});
        passOnSanctionMit6_2.setGroupingCondition(new GroupingCondition[]{gc2});
        GradeCondition c7 = new GradeCondition("5");
        c7.setMaxOccurrence(0);
        GradeCondition c71 = new GradeCondition("6");
        c71.setMaxOccurrence(1);
//        gc.setConditions(new GradeCondition[]{c2});
        passOnSanctionMit6_2.setConditions(new GradeCondition[]{c7, c71});
        passOnSanctionMit6_2.setProperties(new Property[]{unbias, matchShortLabel});
        //
        Policy passOnSanctionMit6_3 = new Policy("1-ungenügend-2-befriedigend");
        GroupingCondition gc3 = new GroupingCondition(null, new SubjectFilter[]{hf, SubjectFilter.DEFAULT});
        GradeCondition c9 = new GradeCondition("6");
        c9.setPair(new String[]{"1", "2", "3"});
        c9.setMinPairs(2);
        gc3.setConditions(new GradeCondition[]{c9});
        passOnSanctionMit6_3.setGroupingCondition(new GroupingCondition[]{gc3});
        GradeCondition c10 = new GradeCondition("5");
        c10.setMaxOccurrence(0);
        GradeCondition c101 = new GradeCondition("6");
        c101.setMaxOccurrence(1);
        passOnSanctionMit6_3.setConditions(new GradeCondition[]{c10, c101});
        passOnSanctionMit6_3.setProperties(new Property[]{unbias, matchShortLabel});
        //
        t.setPolicies(new Policy[]{pass, passOnSanction, passOnSanctionMit6_2, passOnSanctionMit6_3});
        JAXBContext ctx = JAXBContext.newInstance(VersetzungsValidationConfiguration.class, CareerAwareGradeCondition.class);
        Marshaller m = ctx.createMarshaller();
        m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        m.marshal(t, System.out);
    }

}
