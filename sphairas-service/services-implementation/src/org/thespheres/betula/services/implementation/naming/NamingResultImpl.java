/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.naming;

import org.thespheres.betula.services.implementation.TermConstants;
import java.util.Map;
import java.util.StringJoiner;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Lookup;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermSchedule;

/**
 *
 * @author boris.heithecker
 */
final class NamingResultImpl extends NamingResolver.Result {

    private final boolean jahrgang;
    private final boolean klasse;
    private final int defJahr;
    private final int baseLevel;
    private final boolean aufsteigend;
    private final boolean abitur;
    private final TermSchedule termSchedule;
    private final boolean schuelerfa;
    private final String suffix;

    NamingResultImpl(Map<String, String> elements, boolean jahrgang, boolean klasse, int jahr, int stufe, boolean aufsteigend, boolean sfa, boolean abitur, String suffix, TermSchedule sched) {
        super(elements);
        this.jahrgang = jahrgang;
        this.klasse = klasse;
        this.defJahr = jahr;
        this.baseLevel = stufe;
        this.aufsteigend = aufsteigend;
        this.elements.put(Naming.STUFE, Integer.toString(stufe));
        this.elements.put(Naming.START_JAHR, Integer.toString(defJahr));
        this.schuelerfa = sfa;
        this.suffix = suffix;
        this.abitur = abitur;
        this.termSchedule = sched;
    }

    @Override
    public String getResolvedName(Object... params) {
        Term term = null;
        if (params == null || params.length == 0 || params[0] == null) {
            WorkingDate wd = null;
            try {
                wd = Lookup.getDefault().lookup(WorkingDate.class);
            } catch (NoClassDefFoundError e) {
                //We need this in headless server environment
            }
            if (wd != null) {
                term = termSchedule.getTerm(wd.getCurrentWorkingDate());
            } else {
                term = termSchedule.getCurrentTerm();
            }
        } else if (params[0] instanceof Term) {
            term = (Term) params[0];
        }
        if (term != null) {
            return doResolve(term);
        }
        throw new IllegalArgumentException();
    }

    public String doResolve(Term term) {
        StringJoiner ret = new StringJoiner(" ");
        Object yp = term.getParameter(TermConstants.JAHR);
        if (yp instanceof Integer) {
            //append term displayname to current stufe
            boolean appendSJ = !aufsteigend && !hints.contains(Naming.KLASSE_OHNE_SCHULJAHRESANGABE);
            int diff;
            if (!abitur) {
                diff = (int) term.getParameter(TermConstants.JAHR) - defJahr;
                if (diff < 0) {
                    diff = 0;
                    appendSJ = true;
                }
            } else {
                diff = defJahr - (int) term.getParameter(TermConstants.JAHR);
            }
            final int currentStufe;
            if (!abitur) {
                currentStufe = baseLevel + (aufsteigend ? diff : 0);
//                currentStufe = Integer.toString(baseLevel + (aufsteigend ? diff : 0));
            } else {
                currentStufe = baseLevel + 2 - diff;
            }
            if (hints.contains(Naming.HINT_NURSTUFE)) {
                return Integer.toString(currentStufe);
            }
            if (elements.containsKey(Naming.FACH) && !hints.contains(Naming.HINT_OHNE_FACH)) {
                ret.add(elements.get(Naming.FACH));
            } else if (jahrgang && !abitur) {
                ret.add("Jahrgang");
            } else if (schuelerfa) {
                ret.add("Schülerfirma Jg.");
            }
            if (klasse) {
                String t = Integer.toString(currentStufe);
                String klId = elements.get(Naming.KURS_KLASSE_ID);
                if (klId.startsWith("hr")) {
                    klId = "." + klId.substring(2);
                } else {
                    try {
                        final int nklid = Integer.parseInt(klId);
                        if (nklid > 0) {
                            klId = "." + klId;
                        }
                    } catch (final NumberFormatException ignore) {
                    }
                }
                t = t + klId;
                ret.add(t);
            } else {
                final String t;
                if (abitur) {
//                    if (diff <= 2) {
//                        t = "Q" + Integer.toString(3 - diff);
//                    } else {
                    t = "Abitur " + Integer.toString(defJahr);
//                    }
                } else {
                    t = Integer.toString(currentStufe);
                }
                ret.add(t);
                String id1 = elements.get(Naming.KURS_KLASSE_ID);
                String id2 = elements.get(Naming.KURS_KLASSE_ID2);
                if (id1 != null) {
                    if (id1.equals("sfa") && id2 != null) {
                        ret.add("Schülerfa.");
                        String sfaname = id2.equals("gus") ? "GuS" : StringUtils.capitalize(id2);
                        ret.add(sfaname);
//                        String sgl = elements.get(Naming.SGL);//Deprecated, notuse
//                        if (sgl != null) {
//                            ret.add("(" + sgl + ")");
//                        }
                    } else {
                        ret.add(id1);
                        if (id2 != null) {
                            ret.add(id2);
                        }
                    }
                }
            }
            if (appendSJ) {
                String app = "(" + Integer.toString(defJahr) + "/" + Integer.toString(defJahr + 1) + ")";
                ret.add(app);
            }
            if (suffix != null) {
                ret.add(StringUtils.capitalize(suffix));
            }
            return ret.toString();
        }
        throw new IllegalArgumentException();
    }

}
