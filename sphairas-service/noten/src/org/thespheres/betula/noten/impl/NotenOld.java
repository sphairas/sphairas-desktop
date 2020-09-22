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
import org.thespheres.betula.assess.Grade;

/**
 *
 * @author Boris Heithecker
 */
@Deprecated //use sLabel but keep for backward comp.
public class NotenOld {

    private NotenOld() {
    }

    public static final class NotenOldGradeImpl extends NotenGrade implements Serializable, Grade.Biasable { //Obj {

        private NotenOldGradeImpl next;
        private NotenOldGradeImpl previous;
        private final String text;
        private final int val;
        private final double calcVal;
        private final String sLabel;

        //Use only for unmarshalling
        public NotenOldGradeImpl() {
            this(null, null, 0, null, 0d);
        }

        NotenOldGradeImpl(String text, String sLabel, int v, NotenOldGradeImpl n, double calcVal) {
            super(NotenAssessment.NAME, String.valueOf(v));
            this.text = text;
            this.next = n;
            this.val = v;
            this.calcVal = calcVal;
            this.sLabel = sLabel;
        }

        private int getInternalValue() {
            return val;
        }

        @Override
        public Grade getNextLower() {
            return previous;
        }

        @Override
        public Grade getNextHigher() {
            return next;
        }

        private NotenGrade fromInternalValue(int v) {
            for (Grade g : ALL) {
                if (((NotenOldGradeImpl) g).getInternalValue() == v) {
                    return (NotenGrade) g;
                }
            }
            return null;
        }

        @Override
        public boolean isBiased() {
            return isCeilingBiased() || isFloorBias();
        }

        @Override
        public boolean isCeilingBiased() {
            int value = getInternalValue();
            return value > 11 && value < 16;
        }

        @Override
        public boolean isFloorBias() {
            int value = getInternalValue();
            return value > 20 && value < 26;
        }

        @Override
        public NotenGrade getCeilingBiased() {
            if (isCeilingBiased()
                    || this == NotenOld.SECHS
                    || this == NotenOld.EINS
                    || this == NotenOld.EINS_MINUS) {
                return this;
            } else if (isFloorBias()) {
                return fromInternalValue(this.getInternalValue() - 10);
            } else {
                return fromInternalValue(this.getInternalValue() + 10);
            }
        }

        @Override
        public NotenGrade getFloorBiased() {
            if (isFloorBias() || this == NotenOld.SECHS) {
                return this;
            } else if (isCeilingBiased()) {
                return fromInternalValue(this.getInternalValue() + 10);
            } else {
                return fromInternalValue(this.getInternalValue() + 20);
            }
        }

        @Override
        public NotenGrade getUnbiased() {
            if (!isBiased()) {
                return this;
            } else if (isCeilingBiased()) {
                return fromInternalValue(this.getInternalValue() - 10);
            } else {
                return fromInternalValue(this.getInternalValue() - 20);
            }
        }

        @Override
        public String getLongLabel(Object... formattingArgs) {
            return text;
        }

        @Override
        public String getShortLabel() {
            return sLabel;
        }

        @Override
        public Number getNumberValue() {
            return calcVal;
        }

    }

    public static final Grade EINS;
    public static final Grade EINS_MINUS;
    public static final Grade ZWEI_PLUS;
    public static final Grade ZWEI;
    public static final Grade ZWEI_MINUS;
    public static final Grade DREI_PLUS;
    public static final Grade DREI;
    public static final Grade DREI_MINUS;
    public static final Grade VIER_PLUS;
    public static final Grade VIER;
    public static final Grade VIER_MINUS;
    public static final Grade FUENF_PLUS;
    public static final Grade FUENF;
    public static final Grade FUENF_MINUS;
    public static final Grade SECHS;

    static final Grade[] ALL;
    static final Grade[] ALL_REV;
    static final Grade[] ALL_LINKED;

    static {
        NotenOldGradeImpl eins = new NotenOldGradeImpl("Sehr gut", "1", 1, null, 1d);
        NotenOldGradeImpl einsm = new NotenOldGradeImpl("Sehr gut (-)", "1-", 21, null, 1.3d);
        NotenOldGradeImpl zweip = new NotenOldGradeImpl("Gut (+)", "2+", 12, eins, 1.7d);
        NotenOldGradeImpl zwei = new NotenOldGradeImpl("Gut", "2", 2, eins, 2d);
        NotenOldGradeImpl zweim = new NotenOldGradeImpl("Gut (-)", "2-", 22, eins, 2.3d);
        NotenOldGradeImpl dreip = new NotenOldGradeImpl("Befriedigend (+)", "3+", 13, zwei, 2.7d);
        NotenOldGradeImpl drei = new NotenOldGradeImpl("Befriedigend", "3", 3, zwei, 3d);
        NotenOldGradeImpl dreim = new NotenOldGradeImpl("Befriedigend (-)", "3-", 23, zwei, 3.3d);
        NotenOldGradeImpl vierp = new NotenOldGradeImpl("Ausreichend (+)", "4+", 14, drei, 3.7d);
        NotenOldGradeImpl vier = new NotenOldGradeImpl("Ausreichend", "4", 4, drei, 4d);
        NotenOldGradeImpl vierm = new NotenOldGradeImpl("Ausreichend (-)", "4-", 24, drei, 4.3d);
        NotenOldGradeImpl fünfp = new NotenOldGradeImpl("Mangelhaft (+)", "5+", 15, vier, 4.7d);
        NotenOldGradeImpl fünf = new NotenOldGradeImpl("Mangelhaft", "5", 5, vier, 5d);
        NotenOldGradeImpl fünfm = new NotenOldGradeImpl("Mangelhaft (-)", "5-", 25, vier, 5.3d);
        NotenOldGradeImpl sechs = new NotenOldGradeImpl("Ungenügend", "6", 6, fünf, 6d);
        eins.previous = zwei;
        einsm.previous = zwei;
        zweip.previous = drei;
        zwei.previous = drei;
        zweim.previous = drei;
        dreip.previous = vier;
        drei.previous = vier;
        dreim.previous = vier;
        vierp.previous = fünf;
        vier.previous = fünf;
        vierm.previous = fünf;
        fünfp.previous = sechs;
        fünf.previous = sechs;
        fünfm.previous = sechs;
        sechs.previous = null;
        EINS = eins;
        EINS_MINUS = einsm;
        ZWEI_PLUS = zweip;
        ZWEI = zwei;
        ZWEI_MINUS = zweim;
        DREI_PLUS = dreip;
        DREI = drei;
        DREI_MINUS = dreim;
        VIER_PLUS = vierp;
        VIER = vier;
        VIER_MINUS = vierm;
        FUENF_PLUS = fünfp;
        FUENF = fünf;
        FUENF_MINUS = fünfm;
        SECHS = sechs;
        ALL_LINKED = new Grade[]{
            SECHS,
            FUENF,
            VIER,
            DREI,
            ZWEI,
            EINS
        };
        ALL = new Grade[]{
            SECHS,
            FUENF_MINUS,
            FUENF,
            FUENF_PLUS,
            VIER_MINUS,
            VIER,
            VIER_PLUS,
            DREI_MINUS,
            DREI,
            DREI_PLUS,
            ZWEI_MINUS,
            ZWEI,
            ZWEI_PLUS,
            EINS_MINUS,
            EINS
        };
        ALL_REV = new Grade[ALL.length];
        int j = ALL.length - 1;
        for (int i = 0; i < ALL.length; i++) {
            ALL_REV[i] = ALL[j--];
        }
    }

}
