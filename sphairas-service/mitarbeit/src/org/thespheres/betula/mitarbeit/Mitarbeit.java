/*
 * Mitarbeit.java
 *
 * Created on 16. September 2007, 13:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.mitarbeit;

//import betula.assess.GradeObj;
import java.util.Iterator;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeParsingException;

//import betula.assess.GradeObj;
/**
 *
 * @author Boris Heithecker
 */
@ServiceProvider(service = AssessmentConvention.class)
public class Mitarbeit implements AssessmentConvention {

    /**
     * Creates a new instance of Mitarbeit
     */
    public Mitarbeit() {
    }

    public static Grade fromByteValue(byte value) {
        for (Grade gr : ALL) {
            if (((MitarbeitGradeImpl) gr).val == value) {
                return gr;
            }
        }
        return null;
    }

    public static byte getByteValue(Grade grade) {
        if (grade instanceof MitarbeitGradeImpl) {
            return ((MitarbeitGradeImpl) grade).val;
        }
        throw new IllegalArgumentException("Not a Mitarbeit grade. Grade is " + grade.getConvention());
    }

    @Override
    public Grade[] getAllGrades() {
        return ALL;
    }

    @Override
    public Grade[] getAllGradesReverseOrder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Grade> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Grade parseGrade(String text) throws GradeParsingException {
        String trimText = text.trim();
        Grade ret = null;
        for (Grade g : ALL) {
            if (g.getLongLabel(null).equals(trimText)) {
                return g;
                //TODO später wieder weg
            } else if (trimText.equals("*") || trimText.equals("0")) {
                ret = GLEICHBLEIBEND;
            } else if (trimText.equals("?")) {
                ret = UNDEF;
            }
        }
        return ret;
    }

    @Override
    public Grade find(String id) {
        for (Grade g : ALL) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        throw new IllegalArgumentException("No such grade.");
    }

    @Override
    public String getName() {
        return "Mitarbeit";
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    static final class MitarbeitGradeImpl implements Grade { //implements GradeObj {

        private String id;
        private Grade next;
        private Grade previous;
        private final String text;
        private final String slabel;
        private final byte val;
        private final String convention = "Mitarbeit";

        //Use only for unmarshalling
//        public MitarbeitGradeImpl() {
//            this(null, null, (byte) 0, null);
//        }
        MitarbeitGradeImpl(String id, String text, String shortLabel, byte v, Grade n) {
            this.id = id;
            this.text = text;
            this.slabel = shortLabel;
            this.next = n;
            this.val = v;
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
        public String toString() {
            return text;
        }

        @Override
        public String getLongLabel(Object... formattingArgs) {
            return text;
        }

        @Override
        public String getShortLabel() {
            return slabel;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MitarbeitGradeImpl other = (MitarbeitGradeImpl) obj;
            if (this.val != other.val) {
                return false;
            }
            if ((this.convention == null) ? (other.convention != null) : !this.convention.equals(other.convention)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 61 * hash + this.val;
            hash = 61 * hash + (this.convention != null ? this.convention.hashCode() : 0);
            return hash;
        }

        @Override
        public String getConvention() {
            return convention;
        }

        @Override
        public String getId() {
            return this.id;
        }
    }
    public static final Grade UNDEF;
    public static final Grade PLUSPLUS;
    public static final Grade PLUS;
    public static final Grade GLEICHBLEIBEND;
    public static final Grade MINUS;
    public static final Grade MINUSMINUS;
    public static final Grade FEHLEND;
    public static final Grade ENTSCHULDIGT;
    public static final Grade[] ALL;

    static {
        MitarbeitGradeImpl pp = new MitarbeitGradeImpl("plus-plus", "Plus-plus", "++", (byte) 2, null);
        MitarbeitGradeImpl p = new MitarbeitGradeImpl("plus", "Plus", "+", (byte) 1, pp);
        MitarbeitGradeImpl g = new MitarbeitGradeImpl("x", "X", "*", (byte) 0, p);
        MitarbeitGradeImpl m = new MitarbeitGradeImpl("minus", "Minus", "-", (byte) -1, g);
        MitarbeitGradeImpl mm = new MitarbeitGradeImpl("minus-minus", "Minus-Minus", "--", (byte) -2, m);
        MitarbeitGradeImpl f = new MitarbeitGradeImpl("f", "Fehlend", "f", (byte) 127, null);
        MitarbeitGradeImpl fe = new MitarbeitGradeImpl("e", "Entschuldigt", "e", (byte) 126, null);
        MitarbeitGradeImpl ns = new MitarbeitGradeImpl("undef", "---", "?", (byte) -128, null);
        pp.previous = p;
        p.previous = g;
        g.previous = m;
        m.previous = mm;
        mm.previous = null;
        f.previous = null;
        fe.previous = null;
        ns.previous = null;

        UNDEF = ns;
        PLUSPLUS = pp;
        PLUS = p;
        GLEICHBLEIBEND = g;
        MINUS = m;
        MINUSMINUS = mm;
        FEHLEND = f;
        ENTSCHULDIGT = fe;
//        Grade[] allRed = new Grade[] {
//            MINUSMINUS, 
//            MINUS, 
//            GLEICHBLEIBEND, 
//            PLUS, 
//            PLUSPLUS,
//            FEHLEND
//        };
        ALL = new Grade[]{
            UNDEF,
            PLUSPLUS,
            PLUS,
            GLEICHBLEIBEND,
            MINUS,
            MINUSMINUS,
            FEHLEND,
            ENTSCHULDIGT
        };
    }
}
