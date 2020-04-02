/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis;

import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.openide.util.NbBundle;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.niedersachsen.NdsTerms;
import org.thespheres.betula.niedersachsen.zeugnis.ZeugnisData.Report;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;

/**
 *
 * @author boris.heithecker
 */
public class ZeugnisBuilder {

    private final ZeugnisData data = new ZeugnisData();
    private final Marker schulzweig;
    private static final Pattern APOSTROPH_GENITIV = Pattern.compile(".+[sßzx]|(ce)");
    public static final SubjectOrderDefinition FACH_COMPARATOR;
    private static final SubjectOrderDefinition FACH_COMPARATOR_HS;
    private static final SubjectOrderDefinition FACH_COMPARATOR_RS;
    private static final SubjectOrderDefinition FACH_COMPARATOR_GY;
    private final ThreadLocal<SimpleDateFormat> sdf = ThreadLocal.withInitial(() -> new SimpleDateFormat("d. MMMM yyyy"));

    static {
        try {
            FACH_COMPARATOR = SubjectOrderDefinition.load(SubjectOrderDefinition.class.getResourceAsStream("/org/thespheres/betula/niedersachsen/resources/default_order.xml"));
            FACH_COMPARATOR_HS = SubjectOrderDefinition.load(SubjectOrderDefinition.class.getResourceAsStream("/org/thespheres/betula/niedersachsen/resources/order_hs.xml"));
            FACH_COMPARATOR_RS = SubjectOrderDefinition.load(SubjectOrderDefinition.class.getResourceAsStream("/org/thespheres/betula/niedersachsen/resources/order_rs.xml"));
            FACH_COMPARATOR_GY = SubjectOrderDefinition.load(SubjectOrderDefinition.class.getResourceAsStream("/org/thespheres/betula/niedersachsen/resources/order_gy.xml"));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public ZeugnisBuilder(Marker sgl) {
        this(sgl, NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.art.default"));
    }

    public ZeugnisBuilder(Marker sgl, String zeugnisart) {
        this.schulzweig = sgl;
        data.getKopf().setReportTitle(zeugnisart);
        if (schulzweig != null) {
            data.getKopf().setDivison(schulzweig.getLongLabel());
        }
    }

    public static SubjectOrderDefinition forSGL(Marker sgl) {
        if (sgl != null && "kgs.schulzweige".equals(sgl.getConvention())) {
            switch (sgl.getId()) {
                case "hs":
                    return FACH_COMPARATOR_HS;
                case "rs":
                    return FACH_COMPARATOR_RS;
                case "gy":
                    return FACH_COMPARATOR_GY;
            }
        }
        return FACH_COMPARATOR;
    }

    public ZeugnisBuilder setNamenLogos(String schule, String schule2, String logoLinks, String logoRechts) {
        data.getKopf().setSchoolname(schule);
        data.getKopf().setSchoolname2(schule2);
        data.getKopf().setImageLeft(logoLinks);
        data.getKopf().setImageRight(logoRechts);
        return this;
    }

    public ZeugnisBuilder setAbschluss(String abschluss) {
        if (abschluss != null) {
            data.getKopf().setFinalTitle(abschluss);
        }
        return this;
    }

    public ZeugnisBuilder setVornameNachname(String fN) {
        data.getKopf().setStudentName(fN);
        return this;
    }

    public ZeugnisBuilder setVornameNachnameZweiteSeite(String fN) {
        String text = NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.text.nameZweiteSeite", fN);
        data.setSecondPageHeader(text);
        return this;
    }

    public ZeugnisBuilder setGeburtsdatum(Date date) {
        String text = NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.text.geburtsdatum", date);
        data.getKopf().getDatePlaceOfBirth().setLeft(text);
        return this;
    }

    public ZeugnisBuilder setGeburtsort(String placeOfBirth) {
        String text = NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.text.geburtsort", placeOfBirth);
        data.getKopf().getDatePlaceOfBirth().setRight(text);
        return this;
    }

    public void setAbgangsdaten(Date aDatum, String jahrgang) {
        data.getKopf().setSchoolLeaving(sdf.get().format(aDatum), jahrgang);
    }

    public ZeugnisBuilder setHalbjahresdatenUndKlasse(TermId termId, UnitId klasse, NamingResolver nr) {
        Term term;
        try {
            term = NdsTerms.fromId(termId);
        } catch (IllegalAuthorityException ex) {
            Logger.getLogger(ZeugnisBuilder.class.getName()).log(Level.WARNING, ex.getMessage());
            return this;
        }
        String klasseDN = null;
        final NamingResolver res = nr;
        try {
            NamingResolver.Result result = res.resolveDisplayNameResult(klasse);
            result.addResolverHint("klasse.ohne.schuljahresangabe");
            String kla = result.getResolvedName(term);
            klasseDN = NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.text.klasse", kla);
        } catch (IllegalAuthorityException ex) {
            Logger.getLogger(ZeugnisBuilder.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        int jahr = (int) term.getParameter(NdsTerms.JAHR);
        int hj = (int) term.getParameter(NdsTerms.HALBJAHR);
        String links = NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.text.schuljahr", Integer.toString(jahr), Integer.toString(++jahr));
        String mitte = NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.text.halbjahr", hj);
        data.getKopf().setTermData(links, mitte, klasseDN);
        return this;
    }

    public ZeugnisBuilder setFehltageUnentschuldigt(TermId termId, Integer fehltage, Integer unentschuldigt) {
        final Term term;
        try {
            term = NdsTerms.fromId(termId);
        } catch (IllegalAuthorityException ex) {
            Logger.getLogger(ZeugnisBuilder.class.getName()).log(Level.WARNING, ex.getMessage());
            return this;
        }
        final int hj = (int) term.getParameter(NdsTerms.HALBJAHR);
        final String ft = NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.text.versaeumnis", hj, fehltage == null ? "---" : fehltage);
        final String ue = NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.text.unentschuldigt", unentschuldigt == null ? "---" : unentschuldigt);
        data.getKopf().setDaysAbsent(ft, ue);
        return this;
    }

    public ZeugnisBuilder setAGs(String text) {
        String title = NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.text.title.ag");
        String val = NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.text.title.ag.position");
        int pos = Integer.parseInt(val);
        data.addNote(title, pos).setValue(text);
        return this;
    }

    public ZeugnisBuilder setBemerkungen(String text) {
        String title = NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.text.title.bemerkungen");
        String val = NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.text.title.bemerkungen.position");
        int pos = Integer.parseInt(val);
        data.addNote(title, pos).setValue(text);
        return this;
    }

    public ZeugnisBuilder createKopfnote(Grade kn, String vorname, String defaultConvention) {
        if (kn == null) {
            return this;
        }
        if (defaultConvention == null) {
            defaultConvention = kn.getConvention();
        }
        String bname = GradeFactory.findConvention(defaultConvention).getDisplayName() + ":";
        String genitiv = getGenitiv(vorname);
        String val = NbBundle.getMessage(ZeugnisBuilder.class, defaultConvention + ".position");
        int pos = Integer.parseInt(val);
        data.addNote(bname, pos).setValue(kn.getLongLabel(genitiv));
        return this;
    }

    public ZeugnisBuilder setZeungisdatum(String ort, Date date) {
        String text = NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.text.zeugnisdatum_alt", ort, date);
        data.setPlaceDate(text);
        return this;
    }

    public ZeugnisBuilder createArea(String headerId, ArrayList<ZeugnisField> felder, boolean required) {
        if (headerId == null || felder == null) {
            return this;
        }
        if (felder.isEmpty() && !required) {
            return this;
        }
        final String header = NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.text.area.header." + headerId);
        final ZeugnisData.Area pf = data.addArea(header);
        if (felder.isEmpty()) {
            pf.addEmptyLine();
            return this;
        }

        boolean fleven = (felder.size() % 2) == 0;
        int linecount = (fleven ? felder.size() : felder.size() + 1) / 2;
        boolean flkBemerkung = false;
        for (int i = 0; i < linecount; i++) {
            final ZeugnisField links = felder.get(i);
            if (links.getType() != null) {
                flkBemerkung = true;
            }
            ZeugnisField rechts = null;
            if (linecount + i < felder.size()) {
                rechts = felder.get(linecount + i);
                if (rechts.getType() != null) {
                    flkBemerkung = true;
                }
            }
            pf.addLine(links, rechts);
        }
        if (flkBemerkung && schulzweig != null && !"gy".equals(schulzweig.getId())) {
            String text = NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.text.flkbemerkung." + schulzweig.getId());
            pf.setNote(text);
        }
        final Map<String, List<String>> fn = felder.stream()
                .filter(f -> f.getFootnoteRef() != null)
                .collect(Collectors.groupingBy(f -> f.getFootnoteRef(), Collectors.mapping(f -> f.getFootnoteText(), Collectors.toList())));
        fn.forEach((k, v) -> data.addFootnote(k, v.get(0)));
        return this;
    }

    public ZeugnisBuilder createArea(String headerId, ArrayList<ZeugnisBericht> berichte) {
        if (headerId == null || berichte == null) {
            return this;
        }
        if (berichte.isEmpty()) {
            return this;
        }
        String header = NbBundle.getMessage(ZeugnisBuilder.class, "zeugnis.text.area.header." + headerId);
        ZeugnisData.Area pf = data.addArea(header);

        int i = 1;
        for (ZeugnisBericht zb : berichte) {
            Report report = new Report(zb.getSubject(), i++);
            report.setValue(zb.getValue());
            pf.getReports().add(report);
        }
        return this;
    }

    public ZeugnisData getZeugnisData() {
        return data;
    }

    public static String getGenitiv(String vorname) {
        String genitiv = null;
        if (vorname != null) {
            if (APOSTROPH_GENITIV.matcher(vorname).matches()) {
                genitiv = vorname + "'";
            } else {
                genitiv = vorname + "s";
            }
        }
        return genitiv;
    }

    public static String getPossessivPronomen(String gender) {
        if (gender != null) {
            switch (gender.toLowerCase()) {
                case "f":
                    return "ihre";
                case "m":
                    return "seine";
            }
        }
        return null;
    }

    public static String getPossessivPronomenGenitiv(String gender) {
        if (gender != null) {
            switch (gender.toLowerCase()) {
                case "f":
                    return "ihrer";
                case "m":
                    return "seiner";
            }
        }
        return null;
    }

    public ZeugnisField createField(Set<Marker> fach, Grade g, String flk) {
        return new ZeugnisField(fach, g, flk);
    }

    public final class ZeugnisField implements ZeugnisData.LineFieldCallback, Comparable<ZeugnisField> {

        private final Set<Marker> fach;
        private final Grade grade;
        private final String fachleistungskurs;
        private String fnIndex;
        private String fnText;

        private ZeugnisField(Set<Marker> fach, Grade g, String flk) {
            this.fach = fach;
            this.grade = g;
            this.fachleistungskurs = flk;
        }

        @Override
        public String getSubject() {
            final SubjectOrderDefinition fComparator = forSGL(schulzweig);
            return fach.size() == 1 ? fach.iterator().next().getLongLabel() : fach.stream()
                    .sorted(fComparator)
                    .map(Marker::getLongLabel)
                    .collect(NdsReportBuilderFactory.SUBJECT_JOINING_COLLECTOR);
        }

        @Override
        public String getValue() {
            if (grade == null) {
                return "FEHLER"; //grade ist null, wenn ambigousdocumentcollectionexception......
            } else {
                final Grade ret;
                if (grade instanceof Grade.Biasable) {
                    ret = ((Grade.Biasable) grade).getUnbiased();
                } else {
                    ret = grade;
                }
                return ret.getShortLabel();
            }
        }

        @Override
        public String getType() {
            return fachleistungskurs;
        }

        public void setFootnote(String index, String text) {
            this.fnIndex = index;
            this.fnText = text;
        }

        @Override
        public String getFootnoteRef() {
            return fnIndex;
        }

        public String getFootnoteText() {
            return fnText;
        }

        @Override
        public int compareTo(ZeugnisField o) {
            final SubjectOrderDefinition fComparator = forSGL(schulzweig);
            final Marker f1 = fach.stream().min(fComparator).get();
            final Marker f2 = o.fach.stream().min(fComparator).get();
            return fComparator.compare(f1, f2);
        }

    }

    public static final class ZeugnisBericht implements ZeugnisData.LineFieldCallback, Comparable<ZeugnisBericht> {

        private final Set<Marker> fach;
        private final Comparator<Marker> fComparator;
        private final String text;
        private String alternativeHeader;
        private final static Collator COLLATOR = Collator.getInstance(Locale.GERMANY);

        private ZeugnisBericht(Set<Marker> fach, String header, String text, Comparator<Marker> comp) {
            this.fach = fach;
            this.alternativeHeader = header;
            this.text = text;
            this.fComparator = comp;
        }

        public ZeugnisBericht(Set<Marker> fach, String text, Comparator<Marker> comp) {
            this(fach, null, text, comp);
        }

        public ZeugnisBericht(String header, String text, Comparator<Marker> comp) {
            this(null, header, text, comp);
        }

        @Override
        public String getSubject() {
            return fach.size() == 1 ? fach.iterator().next().getLongLabel() : fach.stream().map(Marker::getLongLabel).collect(NdsReportBuilderFactory.SUBJECT_JOINING_COLLECTOR);
        }

        @Override
        public String getValue() {
            if (text == null) {
                return "FEHLER"; //grade ist null, wenn ambigousdocumentcollectionexception......
            } else {
                return text;
            }
        }

        @Override
        public int compareTo(ZeugnisBericht o) {
            if (fach != null && o.fach != null) {
                final Marker f1 = fach.stream().min(fComparator).get();
                final Marker f2 = o.fach.stream().min(fComparator).get();
                return fComparator.compare(f1, f2);
            }
            if (fach != null) {
                return o.fach != null ? COLLATOR.compare(alternativeHeader, o.alternativeHeader) : 1;
            }
            return -1;
        }

    }
}
