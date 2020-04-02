/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.NumberValueGrade;
import org.thespheres.betula.document.model.ReportDocument;
import org.thespheres.betula.document.model.Subject;
import org.thespheres.betula.validation.impl.GradeDoubleConverter.DefaultGradeDoubleConverter;

/**
 *
 * @author boris.heithecker
 */
@XmlSeeAlso({DefaultGradeDoubleConverter.class})
@XmlAccessorType(value = XmlAccessType.FIELD)
public abstract class GradeDoubleConverter {

    static GradeDoubleConverter DEFAULT = new DefaultGradeDoubleConverter();

    protected GradeDoubleConverter() {
    }

    protected Set<Subject> match(final ReportDocument report, final Set<Subject> subjects, final GradeCondition c, final PolicyRun props, final Policy policy) {
        return subjects.stream()
                .filter(s -> matches(report.select(s), c.getMatch(), props))
                .collect(Collectors.toSet());
    }

    protected boolean pair(final ReportDocument report, final Set<Subject> matches, final Set<Subject> subjects, final GradeCondition c, final PolicyRun props, final Policy policy) {
        final int factor = c.getMinPairs() != null ? c.getMinPairs() : 1;
        final Set<Subject> pair = subjects.stream()
                .filter(s -> c.getPair() == null || matches(report.select(s), c.getPair(), props))
                .collect(Collectors.toSet());
        return pair.size() >= (matches.size() * factor);
    }

    public boolean matches(Grade grade, final String[] match, final PolicyRun props) {
//        final String[] match = pair ? c.getPair() : c.getMatch();
        if (props.unbias && grade instanceof Grade.Biasable) {
            grade = ((Grade.Biasable) grade).getUnbiased();
        }
        final String m = props.matchShortLabel ? grade.getShortLabel() : grade.getId();
        return Arrays.stream(match).anyMatch(m::equals);
    }

    public abstract Double toDouble(ReportDocument r, Subject s, Grade g);

    @XmlRootElement(name = "default-grade-double")
    @XmlAccessorType(value = XmlAccessType.FIELD)
    static class DefaultGradeDoubleConverter extends GradeDoubleConverter {

        public DefaultGradeDoubleConverter() {
        }

        @Override
        public Double toDouble(ReportDocument r, Subject s, Grade g) {
            if (g instanceof NumberValueGrade) {
                return ((NumberValueGrade) g).getNumberValue().doubleValue();
            }
            return null;
        }
    }
}
