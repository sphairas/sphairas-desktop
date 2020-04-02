/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.impl;

import java.text.NumberFormat;
import java.util.Locale;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GroupingGrades.GradeGroup;
import org.thespheres.betula.noten.impl.NotenOS.NotenOSGradeImpl;

/**
 *
 * @author boris.heithecker
 */
@Messages({"NoteOSGroup.format={0}-{1} P."})
class NoteOSGroup implements GradeGroup {

    static final NoteOSGroup NULL = new NoteOSGroup(new Grade[]{NotenOS.P0});
    static final NoteOSGroup UNTERKURSE = new NoteOSGroup(new Grade[]{NotenOS.P1, NotenOS.P2, NotenOS.P3, NotenOS.P4});
    static final NoteOSGroup VIER = new NoteOSGroup(new Grade[]{NotenOS.P5, NotenOS.P6});
    static final NoteOSGroup DREI = new NoteOSGroup(new Grade[]{NotenOS.P7, NotenOS.P8, NotenOS.P9});
    static final NoteOSGroup ZWEI = new NoteOSGroup(new Grade[]{NotenOS.P10, NotenOS.P11, NotenOS.P12});
    static final NoteOSGroup EINS = new NoteOSGroup(new Grade[]{NotenOS.P13, NotenOS.P14, NotenOS.P15});
    static final NoteOSGroup[] ALL = new NoteOSGroup[]{NULL, UNTERKURSE, VIER, DREI, ZWEI, EINS};
    private final static NumberFormat NF = NumberFormat.getIntegerInstance(Locale.getDefault());
    final Grade[] grades;

    static {
        NF.setMaximumIntegerDigits(2);
    }

    private NoteOSGroup(Grade[] g) {
        this.grades = g;
    }

    @Override
    public Grade[] grades() {
        return grades;
    }

    @Override
    public String getDisplayLabel() {
        switch (grades.length) {
            case 1:
                return getLabelFor(grades[0]);
            default:
                return NbBundle.getMessage(NoteOSGroup.class, "NoteOSGroup.format", getLabelFor(grades[0]), getLabelFor(grades[grades.length - 1]));
        }
    }

    private String getLabelFor(Grade g) {
        NotenOSGradeImpl ngi = (NotenOSGradeImpl) g;
        return NF.format(ngi.getNumberValue());
    }

}
