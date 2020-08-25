/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis;

import org.thespheres.betula.xmlimport.parse.TranslateID;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.DocumentId.Version;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.gpuntis.impl.StudenplanUpdater;
import org.thespheres.betula.gpuntis.xml.General;
import org.thespheres.betula.gpuntis.xml.Lesson;
import org.thespheres.betula.gpuntis.xml.Subject;
import org.thespheres.betula.services.calendar.LessonTimeData;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.LessonId;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.xmlimport.ImportItem.CloneableImport;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.OutlineModelNode;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;

/**
 *
 * @author boris.heithecker
 */
public class ImportedLesson extends ImportTargetsItem implements CloneableImport, OutlineModelNode {

    public static final String[] SUBMITS = new String[]{"target", "timetable", "targettimetable", "none"};
    private String submit = SUBMITS[0];
    public static final String PROP_TARGET_ID = "targetId";
    private final Lesson lesson;
    private final General general;
    private GeneratedUnitId generatedUnit;
    private final static Pattern STUFE_STPLITTER = Pattern.compile("\\D+");
    private String targetId;
    private DocumentId targetDocBase;
    private final Listener listener;
    private Optional<Units> units;
    private Boolean existsUnit;
    private final int clone;
    private final String dates;
    private LessonTimeData[] times;

    public ImportedLesson(Lesson l, General general, String sourcNode, String sourceSubject, String sourceTeacher, int cloneId) {
        super(sourcNode, sourceSubject, sourceTeacher);
        listener = new Listener();
        lesson = l;
        this.general = general;
        clone = cloneId;
        StringJoiner sj = new StringJoiner(" - ");
        if (l.getEffectiveBeginDate() != null) {
            sj.add(ImportUntisUtil.MSG_DATES.format(l.getEffectiveBeginDate()));
        }
        if (l.getEffectiveEndDate() != null) {
            sj.add(ImportUntisUtil.MSG_DATES.format(l.getEffectiveEndDate()));
        }
        dates = sj.toString();
    }

    public static ImportedLesson create(Lesson l, General general) {
        String node = createSourceNodeLabel(l);
        return new ImportedLesson(l, general, node, ImportUntisUtil.subject(l.getLessonSubject()), ImportUntisUtil.dirName(l.getLessonTeacher()), 0);
    }

    public static ImportedLesson create(Lesson l, General general, int id) {
        String node = createSourceNodeLabel(l);
        return new ImportedLesson(l, general, node, ImportUntisUtil.subject(l.getLessonSubject()), ImportUntisUtil.dirName(l.getLessonTeacher()), id);
    }

    private static String createSourceNodeLabel(Lesson lesson) {
        StringBuilder sb = new StringBuilder();
        String text1 = lesson.getText1();
        Subject su = lesson.getLessonSubject();
        if (su != null) {
            sb.append(ImportUntisUtil.subject(su));
        }
        List<org.thespheres.betula.gpuntis.xml.Class> cl = lesson.getLessonClasses();
        StringJoiner sj = new StringJoiner(",", " (", ")");
        sj.setEmptyValue("");
        if (cl != null) {
            cl.stream()
                    .map(c -> c.getId().substring(3))
                    .forEach(sj::add);
        }
        if (text1 != null) {
            sj.add(text1);
        }
        sb.append(sj.toString());
        return sb.toString();
    }

    @Override
    public boolean isFragment() {
        return false;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public General getGeneral() {
        return general;
    }

    @Override
    public int id() {
        return clone;
    }

    public boolean isEditableColumn(int i) {
        return false;
    }

    @Override
    public String getHtmlDisplayName() {
        String name = getSourceNodeLabel();
        return "<html>" + name + "</html>";
//                if (isMissing()) {
//            String name = this.klasse != null ? this.klasse : this.kursnr;
//            return "<html><font color=\"RED\">" + "Vermisst: " + "</font>" + name + "</html>";
//        }
    }

    @Override
    public Object getColumn(String id) {
        switch (id) {
            case "dates":
                return dates;
            case "timetable":
                return getTimetable();
            case "misc":
                return getLesson().getText2();
        }
        return "";
    }

    @Override
    public boolean isValid() {
        return !(submit.equals("timetable") || submit.equals("none"))
                && getUnitId() != null
                && getSubjectMarker() != null
                && (getUnitId().getId().contains("klasse") || getUnitId().getId().contains("abitur"));
    }

    public boolean doImportTimetable() {
        return !(submit.equals("target") || submit.equals("none"));
    }

    public synchronized void initialize(final UntisImportConfiguration config, final UntisImportData wizard) {
        final UntisImportConfiguration oldCfg = getUntisImportConfiguration();
        boolean configChanged = oldCfg == null
                || !oldCfg.getProviderInfo().equals(config.getProviderInfo());

        setAssessmentConvention(GradeFactory.findConvention("de.notensystem"));

        Subject su = getLesson().getLessonSubject();
        if (configChanged || getSubjectMarker() == null) {
            Marker fach = Arrays.stream(config.getSubjectMarkerConventions())
                    .flatMap(mc -> Arrays.stream(mc.getAllMarkers()))
                    .filter(m -> (m.getLongLabel().equalsIgnoreCase(su.getLongname()) || m.getShortLabel().equalsIgnoreCase(su.getId().substring(3))))
                    .findAny()
                    .orElse(Marker.NULL);
            setSubjectMarker(fach);
        }
        if (configChanged || generatedUnit == null) {
            List<org.thespheres.betula.gpuntis.xml.Class> cl = getLesson().getLessonClasses();
            if (cl != null) {
                String klasse = null;
                int stufe = -1;
                String kursid = getLesson().getText1() != null ? getLesson().getText1().trim() : null;
                int[] ints = cl.stream()
                        .map(c -> c.getId().substring(3))
                        .map(STUFE_STPLITTER::split)
                        .map(arr -> arr.length > 0 ? arr[0] : null)
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isEmpty())
                        .mapToInt(Integer::parseInt)
                        .toArray();
                if (ints.length > 0) {
                    final int first = ints[0];
                    if (first > 0 && Arrays.stream(ints).allMatch(i -> i == first)) {
                        stufe = first;
                    }
                }
                String[] clArr = cl.stream()
                        .map(c -> c.getId().substring(3))
                        .toArray(String[]::new);
                if (clArr.length == 1) {
                    klasse = clArr[0];
                }
                generatedUnit = new GeneratedUnitId(klasse, stufe, kursid);
                setDeleteDate(ImportUtil.calculateDeleteDate(stufe, 5, Month.JULY));
            } else {
                //Fallback case
                generatedUnit = new GeneratedUnitId(null, -1, null);
                setDeleteDate(LocalDate.now());
            }
        }
        if (configChanged) {
            final String dirName = ImportUntisUtil.dirName(getLesson().getLessonTeacher());
            Signees.get(config.getWebServiceProvider().getInfo().getURL())
                    .flatMap(s -> s.findSignee(dirName))
                    .ifPresent(this::setSignee);
        }
        units = Units.get(config.getWebServiceProvider().getInfo().getURL());

        termScheduleProvider = config.getTermSchemeProvider().getInfo().getURL();

        if (configChanged) {
            Term term = (Term) wizard.getProperty(AbstractFileImportAction.TERM);
            try {
                setClientProperty(ImportTargetsItem.PROP_SELECTED_TERM, term);
            } catch (PropertyVetoException ex) {
            }
        }

        try {
            //Update field configuration only after (!) all readable props have been set.
            //prop change event serves as hint that readable prop have been evaluated
            setClientProperty(PROP_IMPORT_TARGET, config);
        } catch (PropertyVetoException ex) {
        }

        addVetoableChangeListener(listener);
    }

    public UntisImportConfiguration getUntisImportConfiguration() {
        return (UntisImportConfiguration) getClientProperty(PROP_IMPORT_TARGET);
    }

    @Override
    public StudentId[] getUnitStudents() {
        if (students == null) {
            if (existsUnitInSystem()) {
                units.ifPresent(u -> {
                    try {
                        students = u.fetchParticipants(getUnitId(), null).getStudents();
                    } catch (IOException ex) {
                    }
                });
            }
        }
        return super.getUnitStudents();
    }

    @Override
    public UnitId getUnitId() {
        UnitId u = super.getUnitId();
        if (u != null) {
            return u;
        }
        return generatedUnit.getUnitId();
    }

    @Override
    public void setUnitId(UnitId unit) {
        if (unit == null || unit.equals(generatedUnit.getUnitId())) {
            super.setUnitId(null);
        } else {
            super.setUnitId(unit);
        }
        targetDocBase = null;
    }

    @Override
    public String getTooltip() {
        return "";
    }

    public void setCustomDocumentIdIdentifier(String targetId) {
        targetId = StringUtils.trimToNull(targetId);
        String old = getCustomDocumentIdIdentifier();
        this.targetId = targetId;
        try {
            vSupport.fireVetoableChange(PROP_TARGET_ID, old, targetId);
        } catch (PropertyVetoException ex) {
            this.targetId = old;
        }
    }

    public String getCustomDocumentIdIdentifier() {
        return this.targetId;
    }

    @Override
    public DocumentId getTargetDocumentIdBase() {
        if (targetDocBase == null) {
            final UnitId u = getUnitId();
            if (u != null) {
                String id = TranslateID.translateUnitToTarget(u.getId(), getSubjectMarker(), getCustomDocumentIdIdentifier());
                targetDocBase = new DocumentId(getUntisImportConfiguration().getAuthority(), id, Version.LATEST);
            }
        }
        return targetDocBase;
    }

    @Override
    public String getUnitDisplayName() {
        final UnitId u = getUnitId();
        if (u != null) {
            if (getUntisImportConfiguration() != null) {
                final NamingResolver naming = getUntisImportConfiguration().getNamingResolver();
                try {
                    return naming.resolveDisplayName(u);
                } catch (IllegalAuthorityException ex) {
                }
            }
            return u.getId();
        }
        return null;
    }

    @Override
    public boolean isUnitIdGenerated() {
        return super.getUnitId() == null;
    }

    @Override
    public boolean existsUnitInSystem() {
        if (existsUnit == null) {
            existsUnit = units.map(u -> u.hasUnit(getUnitId())).orElse(Boolean.FALSE);
        }
        return existsUnit;
    }

    @Override    //AG: keine AV;SV sonst alle drei
    public TargetDocumentProperties[] getImportTargets() {
        if (getUntisImportConfiguration() != null) {
            return getUntisImportConfiguration().createTargetDocuments(this);
        }
        return new TargetDocumentProperties[0];
    }

    //!untisKurs.isKlassenfach()
    @Override
    public boolean fileUnitParticipants() {
        return !getUnitId().getId().contains("klasse");
    }

    public String getSubmit() {
        return submit;
    }

    public void setSubmit(String submit) {
        this.submit = submit;
    }

    public LessonTimeData[] getTimes() {
        if (times == null) {
            times = StudenplanUpdater.createTimes(lesson, general, this);
        }
        return times;
    }

    public String getTimetable() {
        final StringJoiner sj = new StringJoiner(", ");
        Arrays.stream(getTimes())
                .sorted(Comparator.comparing(LessonTimeData::getDay))
                .map(ImportedLesson::formatLessonTime)
                .forEach(sj::add);
        return sj.toString();
    }

    public LessonId getUntisLessonId() {
        final String dnum = lesson.getId().replace("LS_", "").trim();
        int del = dnum.length() - 2;
        int untisLesson = Integer.valueOf(dnum.substring(0, del));
        return new LessonId(StudenplanUpdater.untisAuthority(getGeneral()), Integer.toString(untisLesson));
    }

    public int getUntisKopplung() {
        final String dnum = lesson.getId().replace("LS_", "").trim();
        int del = dnum.length() - 2;
        return Integer.valueOf(dnum.substring(del));//Kopplung is max 99
    }

    private static String formatLessonTime(final LessonTimeData t) {;
        final String dowText = t.getDay().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault());
        return dowText + "-" + Integer.toString(t.getPeriod().getId());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.lesson);
        hash = 71 * hash + this.clone;
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
        final ImportedLesson other = (ImportedLesson) obj;
        if (!Objects.equals(this.lesson, other.lesson)) {
            return false;
        }
        return this.clone == other.clone;
    }

    private final class Listener implements VetoableChangeListener {

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            String name = evt.getPropertyName();
            if (PROP_UNIQUE_SUBJECT.equals(name) || PROP_UNITID.equals(name) || PROP_TARGET_ID.equals(name)) {
                targetDocBase = null;
                existsUnit = null;
                students = null;
            }
        }

    }

    class GeneratedUnitId implements VetoableChangeListener {

        private final String klasse;
        private final int stufe;
        private final String kursid;
        private boolean isInit = false;
        private UnitId uid;

        @SuppressWarnings("LeakingThisInConstructor")
        private GeneratedUnitId(String klasse, int stufe, String kursid) {
            this.klasse = klasse;
            this.stufe = stufe;
            this.kursid = kursid;
            ImportedLesson.this.addVetoableChangeListener(this);
        }

        private synchronized UnitId getUnitId() {
            if (!isInit) {
                final int rJahr = getGeneral().getSchoolyearbegindate().getYear();
                if (klasse != null && stufe != -1) {
                    String st = Integer.toString(stufe);
                    String kid = klasse.replace(st, "");
                    String idvalue = TranslateID.findId(stufe, rJahr, null, kid, "kgs");
                    uid = new UnitId(getUntisImportConfiguration().getAuthority(), idvalue);
                } else if (stufe != -1 && kursid != null) {
                    String idvalue = TranslateID.findId(stufe, rJahr, getSubjectMarker(), kursid, "kgs");
                    uid = new UnitId(getUntisImportConfiguration().getAuthority(), idvalue);
                } else {
                    uid = UnitId.NULL;
                }
                isInit = true;
            }
            return uid;
        }

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (PROP_UNIQUE_SUBJECT.equals(evt.getPropertyName())) {
                isInit = false;
            }
        }

    }
}
