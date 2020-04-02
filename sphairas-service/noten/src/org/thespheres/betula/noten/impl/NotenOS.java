/*
 * Noten.java
 *
 * Created on 17. Mai 2007, 22:51
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.impl;

import java.io.Serializable;
import org.thespheres.betula.assess.AbstractGrade;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.NumberValueGrade;

/**
 *
 * @author Boris Heithecker
 */
public class NotenOS {

    public static final Grade P15 = new NotenOSGradeImpl("15 P.", 15);
    public static final Grade P14 = new NotenOSGradeImpl("14 P.", 14);
    public static final Grade P13 = new NotenOSGradeImpl("13 P.", 13);
    public static final Grade P12 = new NotenOSGradeImpl("12 P.", 12);
    public static final Grade P11 = new NotenOSGradeImpl("11 P.", 11);
    public static final Grade P10 = new NotenOSGradeImpl("10 P.", 10);
    public static final Grade P9 = new NotenOSGradeImpl("09 P.", 9);
    public static final Grade P8 = new NotenOSGradeImpl("08 P.", 8);
    public static final Grade P7 = new NotenOSGradeImpl("07 P.", 7);
    public static final Grade P6 = new NotenOSGradeImpl("06 P", 6);
    public static final Grade P5 = new NotenOSGradeImpl("05 P.", 5);
    public static final Grade P4 = new NotenOSGradeImpl("04 P.", 4);
    public static final Grade P3 = new NotenOSGradeImpl("03 P.", 3);
    public static final Grade P2 = new NotenOSGradeImpl("02 P.", 2);
    public static final Grade P1 = new NotenOSGradeImpl("01 P.", 1);
    public static final Grade P0 = new NotenOSGradeImpl("00 P.", 0);
    static final Grade[] ALL;
    static final Grade[] ALL_REV;

    static {
        ALL = new Grade[]{
            P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15
        };
        ALL_REV = new Grade[ALL.length];
        int j = ALL.length - 1;
        for (int i = 0; i < ALL.length; i++) {
            ALL_REV[i] = ALL[j--];
        }
    }

    private NotenOS() {
    }

    public static final class NotenOSGradeImpl extends AbstractGrade implements Serializable, NumberValueGrade { //Obj {

        private final String text;
        final int val;

        //Use only for unmarshalling
        public NotenOSGradeImpl() {
            this(null, 0);
        }

        NotenOSGradeImpl(String text, int v) {
            super(NotenOSAssessment.NAME, String.valueOf(v));
            this.text = text;
            this.val = v;
        }

        private int getInternalValue() {
            return val;
        }

        @Override
        public Grade getNextLower() {
            if (this == NotenOS.P0) {
                return null;
            } else {
                return fromInternalValue(this.val - 1);
            }
        }

        @Override
        public Grade getNextHigher() {
            if (this == NotenOS.P15) {
                return null;
            } else {
                return fromInternalValue(this.val + 1);
            }
        }

        private Grade fromInternalValue(int v) {
            for (Grade g : ALL) {
                if (((NotenOSGradeImpl) g).getInternalValue() == v) {
                    return (NotenOSGradeImpl) g;
                }
            }
            return null;
        }

        @Override
        public Number getNumberValue() {
            return getInternalValue();
        }

        @Override
        public String toString() {
            return text;
        }

        @Override
        public String getLongLabel(Object... formattingArgs) {
            return text;
        }

        @Override
        public String getShortLabel() {
            return text;
        }

    }

}
