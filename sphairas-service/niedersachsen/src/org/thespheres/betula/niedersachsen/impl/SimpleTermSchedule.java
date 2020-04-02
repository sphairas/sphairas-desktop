/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.impl;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;
import org.thespheres.betula.TermId;
import org.thespheres.betula.niedersachsen.LSchB;
import org.thespheres.betula.niedersachsen.NdsTerms;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.UserRepresentation;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermNotFoundException;
import org.thespheres.betula.services.scheme.spi.TermSchedule;

/**
 *
 * @author boris.heithecker
 */
public class SimpleTermSchedule implements TermSchedule, UserRepresentation<Term> {

    private String displayName;
    private final String name;

    public SimpleTermSchedule(String name) {
        this.name = name;
    }

    @Override
    public Term getCurrentTerm() {
        return getTerm(LocalDate.now());
    }

    @Override
    public Term termOf(LocalDate time) {
        return getTerm(time);
    }

    public Term getTerm(int jahr, int halbjahr) {
        if (jahr < 1700 || !(halbjahr == 1 || halbjahr == 2)) {
            throw new IllegalArgumentException();
        }
        return new TermImpl(jahr, halbjahr == 2);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName != null ? displayName : getName();
    }

    @Override
    public Term parse(String userRepresention) throws IllegalArgumentException, ParseException {
        String[] pp = userRepresention.trim().split("/");
        if (pp.length == 2) {
            try {
                int jahr = Integer.valueOf(pp[0]);
                int hj = Integer.valueOf(pp[1]);
                if ((hj == 1 || hj == 2) && jahr > 0) {
                    return getTerm(jahr, hj);
                }
            } catch (NumberFormatException nfex) {
                throw new ParseException(userRepresention, 0);
            }
        }
        throw new IllegalArgumentException(userRepresention + " is not a valid representation of a term handled by TermSchedule " + getName());
    }

//    private static void setNullFields(Calendar begin) {
//        begin.set(Calendar.HOUR_OF_DAY, 0);
//        begin.set(Calendar.MINUTE, 0);
//        begin.set(Calendar.SECOND, 0);
//        begin.set(Calendar.MILLISECOND, 0);
//        begin.set(Calendar.DAY_OF_MONTH, 1);
//    }
    private TermImpl getTerm(LocalDate date) {
        final Month month = date.getMonth();
        boolean zweitesHJ = month.getValue() >= Month.FEBRUARY.getValue() && month.getValue() <= Month.JULY.getValue();
        if (zweitesHJ) {
            int jahr = date.getYear() - 1;
            return new TermImpl(jahr, true);
        } else {
            int jahr = date.getYear();
            if (month.equals(Month.JANUARY)) {
                jahr--;
            }
            return new TermImpl(jahr, false);
        }
    }

    static String buildYearDN(int yearF) {
        return Integer.toString(yearF, 10) + "/" + Integer.toString(yearF + 1, 10);
    }

    public static String buildYearShort(int y) {
        return Integer.toString(y, 10).substring(2) + Integer.toString(y + 1, 10).substring(2);
    }

    public static int getYearFromShort(String s) throws IllegalArgumentException {
        if (s.length() == 4) {
            int year;
            try {
                int fi = Integer.valueOf(s.substring(0, 2));
                int se = Integer.valueOf(s.substring(2, 4));
                if (fi > 69) {
                    year = fi + 1900;
                } else {
                    year = fi + 2000;
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(ex);
            }
            if (s.substring(2, 4).equals(String.valueOf(year + 1).substring(2, 4))) {
                return year;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Term resolve(TermId id) throws TermNotFoundException, IllegalAuthorityException {
        if (!id.getAuthority().equals(LSchB.AUTHORITY)) {
            IllegalAuthorityException ex = new IllegalAuthorityException();
            ex.setIllegalIdentity(id);
            throw ex;
        }
        if (id.getId() == null || id.getId() < 0) {
            throw new IllegalArgumentException("TermId id must be greater or equal zero.");
        }
        return new TermImpl(id);
    }

    class TermImpl implements Term {

        private final int hj;
        private final int jahr;
        private final TermId termId;
        private LocalDate begin = null;
        private LocalDate end = null;
//        private Object exs = null;
        private String displayName = null;
        private String schuljahr;

        TermImpl(int jahr, boolean zweitesHj) {
            this.jahr = jahr;
            int id;
            if (!zweitesHj) {
                this.hj = 1;
                id = (jahr - 1700) * 2 + 1;
            } else {
                this.hj = 2;
                id = (jahr - 1699) * 2;
            }
            this.termId = new TermId(LSchB.AUTHORITY, id);
        }

        private TermImpl(TermId id) {
            this.termId = id;
            this.hj = id.getId() % 2 == 1 ? 1 : 2;
            this.jahr = ((id.getId() - this.hj) / 2) + 1700;
        }

        @Override
        public TermId getScheduledItemId() {
            return termId;
        }

        @Override
        public LocalDate getBeginDate() {
            if (begin == null) {
//                begin = Calendar.getInstance();
//                setNullFields(begin);
                if (hj == 1) {
                    begin = LocalDate.of(jahr, Month.AUGUST, 1);
                } else {
                    begin = LocalDate.of(jahr + 1, Month.FEBRUARY, 1);
                }
            }
            return begin;
        }

        @Override
        public LocalDate getEndDate() {
            if (end == null) {
//                end = Calendar.getInstance();
                final LocalDate ld = LocalDate.from(getBeginDate())
                        .plusMonths(6l)
                        .minusDays(1l);
                end = ld;
            }
            return end;
        }

//        @Override
//        public ExScheme getExScheme(String type) {
//            if (type.equals(ExScheme.HOLIDAYS)) {
//                if (this.exs == null) {
//                    try {
//                        this.exs = new SimpleExScheme("http://127.0.0.100/caldav.php/test.user/schulferien/", this);
//                    } catch (IOException ex) {
//                        this.exs = ex;
//                    }
//                }
//                if (this.exs instanceof ExScheme) {
//                    return (ExScheme) exs;
//                }
//
//            }
//            return null;
//        }
        @Override
        public String getDisplayName() {
            if (displayName == null) {
                String halbjahr;
                if (hj == 1) {
                    halbjahr = "1. Halbjahr";
                } else {
                    halbjahr = "2. Halbjahr";
                }
                displayName = buildYearDN(jahr) + " " + halbjahr;
            }
            return displayName;
        }

        public int getJahr() {
            return jahr;
        }

        public int getHalbjahr() {
            return hj;
        }

        @Override
        public Object getParameter(String parameterName) {
            switch (parameterName) {
                case NdsTerms.JAHR:
                    return jahr;
                case NdsTerms.HALBJAHR:
                    return hj;
                default:
                    return null;
            }
        }

        public String getSchuljahrName() {
            if (schuljahr == null) {
                schuljahr = buildYearDN(jahr);
            }
            return schuljahr;
        }

        @Override
        public SimpleTermSchedule getSchedule() {
            return SimpleTermSchedule.this;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + Objects.hashCode(this.termId);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TermImpl other = (TermImpl) obj;
            return Objects.equals(this.termId, other.termId);
        }

    }

}
