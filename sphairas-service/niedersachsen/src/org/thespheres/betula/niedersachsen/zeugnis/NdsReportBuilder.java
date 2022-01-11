/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis;

import java.io.IOException;
import org.thespheres.betula.niedersachsen.xml.NdsZeugnisSchulvorlage;
import org.thespheres.betula.niedersachsen.NdsZeugnisFormular;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.niedersachsen.NdsTerms;
import org.thespheres.betula.niedersachsen.NdsZeugnisFormular.Text;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.util.StudentComparator;

/**
 *
 * @author boris.heithecker
 */
public class NdsReportBuilder {

    public static final String DEFAULT_FEHLTAGE_NULL_STRING = "---";
    static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("d. MMMM yyyy");
    private final NdsZeugnisFormular data;
    private final NdsReportBuilderFactory factory;
    private final Marker career;
    private String fehltageNullString = DEFAULT_FEHLTAGE_NULL_STRING;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    NdsReportBuilder(final String dirName, final String type, final Marker sgl, final NdsReportBuilderFactory factory) {
        this.data = new NdsZeugnisFormular(StudentComparator.sortStringFromDirectoryName(dirName));
        this.factory = factory;
        final String art = type != null ? type : NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.art.default");
        data.getKopf().setReportTitle(art);
        career = sgl;
        if (career != null) {
            data.getKopf().setDivison(sgl.getLongLabel());
        }
        factory.getSchulvorlage().getProperty(NdsZeugnisSchulvorlage.PROP_FEHLTAGE_ZERO_STRING)
                .ifPresent(p -> this.fehltageNullString = p.getValue());
        setNamenLogos();
        setTemplateOptions();
    }

    protected void setNamenLogos() {
        final NdsZeugnisSchulvorlage vorlage = factory.getSchulvorlage();
        data.getKopf().setSchoolname(vorlage.getSchoolName());
        Arrays.stream(vorlage.getSchoolName2()).forEach(t -> data.getKopf().addSchoolname2(t.getTitle(), t.getFontSize()));
        data.getKopf().setImageLeft(setSourceLogo(vorlage.getImageLeftUrl()));
        data.getKopf().setImageRight(setSourceLogo(vorlage.getImageRightUrl()));
        data.getKopf().setImageWidth(vorlage.getLogos().getWidth());
    }

    private static String setSourceLogo(final String res) {
        return res == null ? null : "url('" + res + "')";
    }

    public void encodeLogos(final Path base) throws IOException {
        final NdsZeugnisSchulvorlage vorlage = factory.getSchulvorlage();
        final String rUrl = vorlage.getImageRightUrl();
        if (rUrl != null) {
            final String type = findMimeType(rUrl);
            final String src = setEncodedLogo(base, rUrl, type);
            data.getKopf().setImageRight(src);
        }
        final String lUrl = vorlage.getImageLeftUrl();
        if (lUrl != null) {
            final String type = findMimeType(lUrl);
            final String src = setEncodedLogo(base, lUrl, type);
            data.getKopf().setImageLeft(src);
        }
    }

    private String setEncodedLogo(final Path base, final String rel, final String mime) throws IOException {
        final Path res = base.resolve(rel);
        final byte[] b = Files.readAllBytes(res);
        final String enc = Base64.getEncoder().encodeToString(b);
        return "url('data:" + mime + ";base64," + enc + "')";
    }

    static String findMimeType(final String resource) throws IOException {
        final int dotIndex = resource.lastIndexOf(".");
        if (resource.length() >= dotIndex + 4) {
            final String type = resource.substring(dotIndex + 1);
            return "image/" + type;
        }
        throw new IOException("Could not determine MIME type of resource " + resource);
    }

    protected void setTemplateOptions() {
        final NdsZeugnisSchulvorlage vorlage = factory.getSchulvorlage();
        data.setTemplate(vorlage.getTemplate().getName());
    }

    public NdsReportBuilder setGradeCaptionsOnLayout(boolean setCaptions) {
        if (!setCaptions) {
            data.getTemplateOptions().setOption("ZensurenlegendeNichtDrucken", "Ja");
        } else {
            data.getTemplateOptions().removeOption("ZensurenlegendeNichtDrucken");
        }
        return this;
    }

    public GradeEntry newGradeEntry(final Set<Marker> fach, final Grade g, final String flk) {
        return new GradeEntry(fach, g, flk);
    }

    public TextEntry newTextEntry(final Set<Marker> fach, final String text) {
        return new TextEntry(fach, text);
    }

    public NdsZeugnisFormular getZeugnisData() {
        return data;
    }

    public NdsReportBuilder setZeugnisId(DocumentId report) {
        data.setReportId(report);
        return this;
    }

    public NdsReportBuilder setAbschluss(String abschluss) {
        if (abschluss != null) {
            data.getKopf().setFinalTitle(abschluss);
        }
        return this;
    }

    public NdsReportBuilder setVornameNachname(String fN) {
        data.getKopf().setStudentName(fN);
        return this;
    }

    public NdsReportBuilder setVornameNachnameZweiteSeite(String fN) {
        String text = NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.text.nameZweiteSeite", fN);
        data.setSecondPageHeader(text);
        return this;
    }

    public NdsReportBuilder setGeburtsdatum(Date date) {
        String text = NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.text.geburtsdatum", date);
        data.getKopf().getDatePlaceOfBirth().setLeft(text);
        return this;
    }

    public NdsReportBuilder setGeburtsort(String placeOfBirth) {
        String text = NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.text.geburtsort", placeOfBirth);
        data.getKopf().getDatePlaceOfBirth().setRight(text);
        return this;
    }

    public void setAbgangsdaten(LocalDate date, String jahrgang) {
        final String format = date == null ? "null" : date.format(DTF);
        data.getKopf().setSchoolLeaving(format, jahrgang);
    }

    public NdsReportBuilder setHalbjahresdatenUndKlasse(TermId termId, NamingResolver.Result result) {//UnitId klasse, String namingResolverProviderUrl) {
        Term term;
        try {
            term = NdsTerms.fromId(termId);
        } catch (IllegalAuthorityException ex) {
            Logger.getLogger(NdsReportBuilder.class.getName()).log(Level.WARNING, ex.getMessage());
            return this;
        }
        result.addResolverHint("klasse.ohne.schuljahresangabe");
        final String kla = result.getResolvedName(term);
        final String klasseDN = NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.text.klasse", kla);
        int jahr = (int) term.getParameter(NdsTerms.JAHR);
        int hj = (int) term.getParameter(NdsTerms.HALBJAHR);
        String links = NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.text.schuljahr", Integer.toString(jahr), Integer.toString(++jahr));
        String mitte = NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.text.halbjahr", hj);
        data.getKopf().setTermData(links, mitte, klasseDN);
        return this;
    }

    public NdsReportBuilder setFehltageUnentschuldigt(TermId termId, Integer fehltage, Integer unentschuldigt) {
        final Term term;
        try {
            term = NdsTerms.fromId(termId);
        } catch (IllegalAuthorityException ex) {
            Logger.getLogger(NdsReportBuilder.class.getName()).log(Level.WARNING, ex.getMessage());
            return this;
        }
        final int hj = (int) term.getParameter(NdsTerms.HALBJAHR);
        final String ft = NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.text.versaeumnis", hj, intToLabel(fehltage));
        final String ue = NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.text.unentschuldigt", intToLabel(unentschuldigt));
        data.getKopf().setDaysAbsent(ft, ue);
        return this;
    }

    private String intToLabel(final Integer value) {
        if (null == value) {
            return "---";
        } else {
            switch (value) {
                case 0:
                    return fehltageNullString;
                default:
                    return value.toString();
            }
        }
    }

    public NdsReportBuilder setAGs(String text) {
        String title = NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.text.title.ag");
        String val = NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.text.title.ag.position");
        int pos = Integer.parseInt(val);
        data.addNote(title, pos).setValue(text);
        return this;
    }

    public NdsReportBuilder setBemerkungen(String text) {
        String title = NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.text.title.bemerkungen");
        String val = NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.text.title.bemerkungen.position");
        int pos = Integer.parseInt(val);
        data.addNote(title, pos).setValue(text);
        return this;
    }

    public NdsReportBuilder setExtraText(final int position, final String title, final String text) {
        data.addNote(title, position).setValue(text);
        return this;
    }

    public NdsReportBuilder createKopfnote(Grade kn, String vorname, String defaultConvention, String reason) {
        if (kn == null) {
            return this;
        }
        if (defaultConvention == null) {
            defaultConvention = kn.getConvention();
        }
        String bname = GradeFactory.findConvention(defaultConvention).getDisplayName() + ":";
        String genitiv = DeReportBuilderUtil.getGenitiv(vorname);
        String val = NbBundle.getMessage(NdsReportBuilder.class, defaultConvention + ".position");
        int pos = Integer.parseInt(val);
        final StringJoiner sj = new StringJoiner(" ");
        sj.add(kn.getLongLabel(genitiv));
        if (reason != null && !reason.isEmpty()) {
            sj.add(reason);
        }
        data.addNote(bname, pos).setValue(sj.toString());
        return this;
    }

    public NdsReportBuilder setZeungisdatum(final LocalDate date) {
        final String format = date == null ? "null" : date.format(DTF);
        final String text = NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.text.zeugnisdatum", factory.getSchulvorlage().getSchoolLocation(), format);
        data.setPlaceDate(text);
        return this;
    }

    public NdsZeugnisFormular.Area createGradesArea(final String id, final ArrayList<GradeEntry> felder) {
        if (felder == null) {
            return null;
        }
//        if (felder.isEmpty() && !required) {
//            return this;
//        }
        final String header = id != null ? NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.text.area.header." + id) : null;
        final NdsZeugnisFormular.Area pf = data.addArea(header);
        if (felder.isEmpty()) {
            pf.addEmptyLine();
            return pf;
        }

        boolean fleven = (felder.size() % 2) == 0;
        final int linecount = (fleven ? felder.size() : felder.size() + 1) / 2;
        boolean flkBemerkung = false;
        for (int i = 0; i < linecount; i++) {
            final GradeEntry links = felder.get(i);
            if (links.getFachleistungskurs() != null) {
                flkBemerkung = true;
            }
            GradeEntry rechts = null;
            if (linecount + i < felder.size()) {
                rechts = felder.get(linecount + i);
                if (rechts.getFachleistungskurs() != null) {
                    flkBemerkung = true;
                }
            }
            final NdsZeugnisFormular.Line l = pf.addLine(links.getSubjectString(), rechts != null ? rechts.getSubjectString() : null);
            //
            l.getFieldLinks().setFootnoteRef(links.getFootnoteReference());
            l.getFieldLinks().setValue(links.getValue());
            l.getFieldLinks().setType(links.getFachleistungskurs());
            l.getFieldLinks().setSignee(links.getSignee());
            l.getFieldLinks().setTarget(links.getTarget());
            //
            if (rechts != null) {
                l.getFieldRechts().setFootnoteRef(rechts.getFootnoteReference());
                l.getFieldRechts().setValue(rechts.getValue());
                l.getFieldRechts().setType(rechts.getFachleistungskurs());
                l.getFieldRechts().setSignee(rechts.getSignee());
                l.getFieldRechts().setTarget(rechts.getTarget());
            }
        }
        if (flkBemerkung && career != null && !"gy".equals(career.getId())) {
            final String text = NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.text.flkbemerkung." + career.getId());
            pf.setNote(text);
        }
        final Map<String, List<String>> fn = felder.stream()
                .filter(f -> f.getFootnoteReference() != null)
                .collect(Collectors.groupingBy(f -> f.getFootnoteReference(), Collectors.mapping(f -> f.getFootnoteText(), Collectors.toList())));
        fn.forEach((k, v) -> data.addFootnote(k, v.get(0)));
        return pf;
    }

    public NdsZeugnisFormular.CrossMarkArea createCrossMarkArea() {
        final NdsZeugnisFormular.CrossMarkArea ret = new NdsZeugnisFormular.CrossMarkArea();
        data.setCrossMarkArea(ret);
        return ret;
    }

    public NdsReportBuilder setUnit(final UnitId unit) {
        data.getKopf().setPrimaryUnit(unit);
        return this;
    }

    public NdsReportBuilder setStudent(final StudentId sid) {
        data.getKopf().setStudent(sid);
        return this;
    }

    public NdsReportBuilder createTextArea(String headerId, ArrayList<TextEntry> berichte) {
        if (headerId == null || berichte == null) {
            return this;
        }
        if (berichte.isEmpty()) {
            return this;
        }
        String header = NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.text.area.header." + headerId);
        NdsZeugnisFormular.Area pf = data.addArea(header);

        int i = 1;
        for (TextEntry zb : berichte) {
            Text report = new Text(zb.getSubjectString(), i++);
            report.setValue(zb.getValue());
            pf.getReports().add(report);
        }
        return this;
    }

    String subjectsToString(final Set<Marker> subject) {
        final SubjectOrderDefinition fComparator = factory.forCareer(career);
        return subject.size() == 1 ? subject.iterator().next().getLongLabel() : subject.stream()
                .sorted(fComparator)
                .map(Marker::getLongLabel)
                .collect(factory.subjectJoiningCollector());
    }

    public void addException(final Exception ex) {
        final StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        final String msg = NbBundle.getMessage(NdsReportBuilder.class, "zeugnis.exception", ex.getLocalizedMessage(), sw.toString());
        data.addExceptionMessage(msg);
    }

    public class GradeEntry {

        protected final Set<Marker> subject;
        private String altSubject;
        protected final Grade value;
        private String altValue;
        private final String fachleistungskurs;
        private String fnIndex;
        private String fnText;
        private boolean useLongLabel = false;
        private DocumentId target;
        private Signee signee;

        GradeEntry(final Set<Marker> fach, final Grade g, final String flk) {
            this.subject = fach;
            this.value = g;
            this.fachleistungskurs = flk;
        }

        public Set<Marker> getSubject() {
            return subject;
        }

        public String getAltSubject() {
            return altSubject;
        }

        public void setAltSubject(String altSubject) {
            this.altSubject = altSubject;
        }

        public String getSubjectString() {
            final String alt = getAltSubject();
            if (alt != null) {
                return alt;
            }
            return subjectsToString(subject);
        }

        public Grade getGrade() {
            return value;
        }

        public String getAltValue() {
            return altValue;
        }

        public void setAltValue(String altValue) {
            this.altValue = altValue;
        }

        public String getValue() {
            final String alt = getAltValue();
            if (alt != null) {
                return alt;
            }
            if (value == null) {
                return NbBundle.getMessage(NdsReportBuilder.class, "NdsReportBuilder.Entry.missing"); //grade ist null, wenn ambigousdocumentcollectionexception......
            } else {
                final Grade ret;
                if (value instanceof Grade.Biasable) {
                    ret = ((Grade.Biasable) value).getUnbiased();
                } else {
                    ret = value;
                }
                return useLongLabel ? ret.getLongLabel() : ret.getShortLabel();
            }
        }

        public String getFachleistungskurs() {
            return fachleistungskurs;
        }

        public void setFootnote(final String index, final String text) {
            this.fnIndex = index;
            this.fnText = text;
        }

        public String getFootnoteReference() {
            return fnIndex;
        }

        public String getFootnoteText() {
            return fnText;
        }

        public boolean isUseLongLabel() {
            return useLongLabel;
        }

        public void setUseLongLabel(boolean useLongLabel) {
            this.useLongLabel = useLongLabel;
        }

        public Signee getSignee() {
            return signee;
        }

        public void setSignee(Signee signee) {
            this.signee = signee;
        }

        public DocumentId getTarget() {
            return target;
        }

        public void setTarget(DocumentId target) {
            this.target = target;
        }
    }

    public class TextEntry {

        private final Set<Marker> subject;
        private String altSubject;
        private final String text;

        TextEntry(final Set<Marker> fach, final String text) {
            this.subject = fach;
            this.text = text;
        }

        public Set<Marker> getSubject() {
            return subject;
        }

        public String getAltSubject() {
            return altSubject;
        }

        public void setAltSubject(String altSubject) {
            this.altSubject = altSubject;
        }

        public String getSubjectString() {
            final String alt = getAltSubject();
            if (alt != null) {
                return alt;
            }
            return subjectsToString(subject);
        }

        public String getValue() {
            if (text == null) {
                return NbBundle.getMessage(NdsReportBuilder.class, "NdsReportBuilder.Entry.missing");
            } else {
                return text;
            }
        }

    }

    public static class Subtitle {

        @XmlValue
        private final String title;

        @XmlAttribute(name = "Schriftgröße")
        private final String fontSize;

        public Subtitle(String title, String fontSize) {
            this.title = title;
            this.fontSize = fontSize;
        }

        public String getTitle() {
            return title;
        }

        public String getFontSize() {
            return fontSize;
        }

    }
}
