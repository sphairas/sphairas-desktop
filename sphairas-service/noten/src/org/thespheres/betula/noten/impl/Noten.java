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
import java.util.Arrays;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class Noten {

    private Noten() {
    }

    public static final class NotenGradeImpl extends NotenGrade implements Serializable, Grade.Biasable { //Obj {

        private NotenGradeImpl next;
        private NotenGradeImpl previous;
        private final String text;
        private final double calcVal;

        //Use only for unmarshalling
        public NotenGradeImpl() {
            this(null, null, null, 0d);
        }

        NotenGradeImpl(String text, String id, NotenGradeImpl n, double calcVal) {
            super(NotenAssessment.NAME, id);
            this.text = text;
            this.next = n;
            this.calcVal = calcVal;
        }

        @Override
        public Grade getNextLower() {
            return previous;
        }

        @Override
        public Grade getNextHigher() {
            return next;
        }

        @Override
        public boolean isBiased() {
            return isCeilingBiased() || isFloorBias();
        }

        @Override
        public boolean isCeilingBiased() {
            return getId().endsWith("+");
        }

        @Override
        public boolean isFloorBias() {
            return getId().endsWith("-");
        }

        private NotenGrade find(final String id, final String bias) {
            final String find = id.substring(0, 1) + (bias != null ? bias : "");
            return (NotenGrade) Arrays.stream(ALL)
                    .filter(g -> g.getId().equals(find))
                    .collect(CollectionUtil.singleOrNull());
        }

        @Override
        public NotenGrade getCeilingBiased() {
            if (isCeilingBiased()
                    || this == Noten.SECHS
                    || this == Noten.EINS) {
                return this;
            } else if (this == Noten.EINS_MINUS) {
                return (NotenGrade) Noten.EINS;
            }
            return find(getId(), "+");
        }

        @Override
        public NotenGrade getFloorBiased() {
            if (isFloorBias() || this == Noten.SECHS) {
                return this;
            }
            return find(getId(), "-");
        }

        @Override
        public NotenGrade getUnbiased() {
            if (!isBiased()) {
                return this;
            }
            return find(getId(), null);
        }
        
        @Override
        public String getLongLabel(Object... formattingArgs) {
            return text;
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
        NotenGradeImpl eins = new NotenGradeImpl("Sehr gut", "1", null, 1d);
        NotenGradeImpl einsm = new NotenGradeImpl("Sehr gut (-)", "1-", null, 1.3d);
        NotenGradeImpl zweip = new NotenGradeImpl("Gut (+)", "2+", eins, 1.7d);
        NotenGradeImpl zwei = new NotenGradeImpl("Gut", "2", eins, 2d);
        NotenGradeImpl zweim = new NotenGradeImpl("Gut (-)", "2-", eins, 2.3d);
        NotenGradeImpl dreip = new NotenGradeImpl("Befriedigend (+)", "3+", zwei, 2.7d);
        NotenGradeImpl drei = new NotenGradeImpl("Befriedigend", "3", zwei, 3d);
        NotenGradeImpl dreim = new NotenGradeImpl("Befriedigend (-)", "3-", zwei, 3.3d);
        NotenGradeImpl vierp = new NotenGradeImpl("Ausreichend (+)", "4+", drei, 3.7d);
        NotenGradeImpl vier = new NotenGradeImpl("Ausreichend", "4", drei, 4d);
        NotenGradeImpl vierm = new NotenGradeImpl("Ausreichend (-)", "4-", drei, 4.3d);
        NotenGradeImpl fünfp = new NotenGradeImpl("Mangelhaft (+)", "5+", vier, 4.7d);
        NotenGradeImpl fünf = new NotenGradeImpl("Mangelhaft", "5", vier, 5d);
        NotenGradeImpl fünfm = new NotenGradeImpl("Mangelhaft (-)", "5-", vier, 5.3d);
        NotenGradeImpl sechs = new NotenGradeImpl("Ungenügend", "6", fünf, 6d);
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
