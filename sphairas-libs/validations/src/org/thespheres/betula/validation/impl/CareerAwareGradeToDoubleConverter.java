/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.model.ReportDocument;
import org.thespheres.betula.document.model.Subject;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "career-aware-grade-double", namespace = "http://www.thespheres.org/xsd/niedersachsen/versetzung.xsd")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class CareerAwareGradeToDoubleConverter extends GradeDoubleConverter {

    @XmlAttribute(name = "career-convention", required = true)
    private String sglConvention;
    @XmlElement(name = "adjust-values")
    private AdjustCondition[] pairing;

    public CareerAwareGradeToDoubleConverter() {
    }

    public CareerAwareGradeToDoubleConverter(String sglConvention) {
        this.sglConvention = sglConvention;
    }

    public String getCareerConvention() {
        return sglConvention;
    }

    public void setCareerConvention(String convention) {
        this.sglConvention = convention;
    }

    public AdjustCondition[] getConditions() {
        return pairing;
    }

    public void setConditions(AdjustCondition[] pairing) {
        this.pairing = pairing;
    }

    private Map<String, Integer> adjustConditions() {
        return Arrays.stream(pairing)
                .map(p -> Arrays.stream(p.getKey())
                .collect(Collectors.toMap(Function.identity(), k -> p.getSteps())))
                .collect(HashMap::new, Map::putAll, Map::putAll);
    }

    static String kursunterschied(final Marker reportSGL, final Marker kursSGL) {
        final String id = reportSGL.getId();
        final String kid = kursSGL.getId();
        return id + "-" + kid;
    }

    @Override
    public Double toDouble(final ReportDocument r, final Subject s, Grade g) {
        if (g != null) {
            final Marker kursSGL = s.getRealmMarker();
            final String career = getCareerConvention();
            final Marker reportSGL = Arrays.stream(r.markers())
                    .filter(marker -> marker.getConvention().equals(career))
                    .collect(CollectionUtil.singleOrNull());
            if (kursSGL != null && kursSGL.getConvention().equals(career) && reportSGL != null && reportSGL.getConvention().equals(career)) {
                final String diff = kursunterschied(reportSGL, kursSGL);
                final Integer steps = adjustConditions().get(diff);
                if (steps != null && steps != 0) {
                    for (int i = 0; i++ < Math.abs(steps);) {
                        final Grade n;
                        if (steps > 0) {
                            n = g.getNextHigher();
                        } else {
                            n = g.getNextLower();
                        }
                        if (n != null) {
                            g = n;
                        }
                    }
                }
            }
        }
        return GradeDoubleConverter.DEFAULT.toDouble(r, s, g);
    }

    class Pair {

        private final Subject match;
        private final Set<Subject> paring = new HashSet<>();

        Pair(Subject match) {
            this.match = match;
        }

    }

    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static class AdjustCondition {

        @XmlList
        @XmlAttribute(name = "conditions")
        private String[] key;
        @XmlAttribute(name = "steps")
        private int steps;

        public AdjustCondition() {
        }

        public AdjustCondition(String[] key, int steps) {
            this.key = key;
            this.steps = steps;
        }

        public String[] getKey() {
            return key;
        }

        public int getSteps() {
            return steps;
        }

    }
}
