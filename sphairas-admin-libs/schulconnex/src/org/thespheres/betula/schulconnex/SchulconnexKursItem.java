package org.thespheres.betula.schulconnex;

import de.schulconnex.qs.model.Gruppendatensatz;
import de.schulconnex.qs.model.Gruppenzugehoerigkeit;
import de.schulconnex.qs.model.Laufzeit;
import de.schulconnex.qs.model.Referenzgruppe;
import java.awt.Color;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOColorLines;
import org.openide.windows.InputOutput;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.parse.NameParser;
import org.thespheres.betula.xmlimport.utilities.AbstractDelayedStudents;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;

/**
 * Initial target-item representation for Schulconnex "Kurse".
 * <p>
 * For now this class reuses the existing class import behavior and will be
 * specialized in follow-up steps.
 */
public class SchulconnexKursItem extends ImportTargetsItem {

    public enum Typ {
        KLASSENUNTERRICHT, KURSUNTERRICHT
    }
    static final List<String> ROLLEN = List.of("Lern");
    static final Marker WPK_NIEDERSACHSEN = MarkerFactory.find("niedersachsen.unterricht.art", "wpk", null);
    private final Gruppendatensatz source;
    private boolean selected;
    private Boolean existsUnit;
    private final Term currentTerm;
    private final DelayedKursStudentSet participants = new DelayedKursStudentSet();
    private SchulconnexKursItem.Typ typ;
    private UnitId generatedUnitId;
    private DocumentId targetDocBase;
    private Integer baseLevel;

    SchulconnexKursItem(final Gruppendatensatz gds, final List<Gruppendatensatz> gruppen, final SchulconnexImportConfiguration config, final Term current) {
        super(gds.getGruppe().getBezeichnung());
        this.source = gds;
        this.currentTerm = current;
        initialize(gruppen, config);
    }

    private void initialize(final List<Gruppendatensatz> gruppen, final SchulconnexImportConfiguration config) {
        try {
            setClientProperty(PROP_IMPORT_TARGET, config);
        } catch (PropertyVetoException ex) {
            ex.printStackTrace(ImportUtil.getIO().getErr());
            return;
        }

        participants.setConfiguration(config);

        // Klassenunterricht oder Kursunterricht?
        final List<Gruppendatensatz> klasse = new ArrayList<>();
        for (Referenzgruppe rg : getSource().getGruppe().getReferenzgruppen()) {
            Gruppendatensatz found = gruppen.stream()
                    .filter(gds -> rg.getGrupid().equals(gds.getGruppe().getId()))
                    .collect(SchulconnexUtil.collectByUUID(Gruppendatensatz::getGruppe));
            if ("Klasse".equalsIgnoreCase(found.getGruppe().getTyp())) {
                klasse.add(found);
            }
        }
        boolean noLern = getSource().getGruppenzugehoerigkeiten().stream()
                .noneMatch(gz -> gz.getRollen().stream().anyMatch("Lern"::equalsIgnoreCase));
        if (klasse.size() == 1 && noLern) {
            this.typ = SchulconnexKursItem.Typ.KLASSENUNTERRICHT;
        } else {
            this.typ = SchulconnexKursItem.Typ.KURSUNTERRICHT;
        }

        // Subjects bestimmen
        final Marker[] subjects = source.getGruppe().getFaecher().stream()
                .map(f -> f.getCode())
                .map(c -> {
                    return Arrays.stream(config.getSubjectMarkerConventions())
                            .flatMap(mc -> Arrays.stream(mc.getAllMarkers()))
                            .filter(m -> m.getShortLabel().equalsIgnoreCase(c))
                            .findAny()
                            .orElse(Marker.NULL);
                })
                .toArray(Marker[]::new);
        setSubjectMarker(subjects);
        final Marker realm = Optional.ofNullable(source.getGruppe().getBereich())
                .filter("Wahlpflicht"::equalsIgnoreCase)
                .map(b -> WPK_NIEDERSACHSEN)
                .orElse(Marker.NULL);//TODO: use Pflichtunterricht
        if (!Marker.isNull(realm)) {
            uniqueMarkers.add(realm);
        }
        if (config.getAssessmentConventions().length > 0) {
            setAssessmentConvention(config.getAssessmentConventions()[0]);
        }

        // UnitId bestimmen, TargetDocumentIdBase bestimmen
        if (Typ.KURSUNTERRICHT.equals(typ)) {
            generateUnitId(config, source.getGruppe().getBezeichnung(), getSubjectMarker());
        } else {
            generateUnitId(config, klasse.get(0).getGruppe().getBezeichnung(), null);
        }
        existsUnit = Units.get(config.getWebServiceProvider().getInfo().getURL())
                .map(u -> u.hasUnit(getUnitId()))
                .orElse(Boolean.FALSE);
        generateTargetDocumentIdBase(config);

        Integer level = findLevel();
        if (baseLevel != null) {
            setDeleteDate(ImportUtil.calculateDeleteDate(level, baseLevel, Month.JULY));
        } else {
            Optional.ofNullable(source.getGruppe().getLaufzeit())
                    .map(Laufzeit::getBis)
                    .map(bis -> bis.plusYears(1))
                    .ifPresent(this::setDeleteDate);
        }

//        Signees.get(config.getWebServiceProvider().getInfo().getURL())
//                .flatMap(s -> s.findSignee(getSourceSigneeName()))
//                .ifPresent(this::setSignee);
        addParticipants(getSource(), null, gruppen);
    }

    @NbBundle.Messages({"SchulconnexKursItem.initialize.warning.no.level=Für die Schulconnex-Klasse \"{0}\" mit den Schulconnex-Jahrgangsstufen \"{1}\" kann keine eindeutige Stufe bestimmt werden."})
    protected Integer findLevel() throws MissingResourceException {
        Integer level = null;
        final List<String> jj = getSource().getGruppe().getJahrgangsstufen();
        if (jj != null && jj.size() == 1) {
            try {
                level = Integer.valueOf(jj.get(0));
            } catch (NumberFormatException nfex) {
                Exceptions.printStackTrace(nfex);
            }
        }
        if (level == null) {
            String concat = jj == null ? "null" : jj.stream().collect(Collectors.joining(","));
            final String message = NbBundle.getMessage(SchulconnexKlasseItem.class, "SchulconnexKursItem.initialize.warning.no.level", getKurs(), concat);
            InputOutput io = ImportUtil.getIO();
            try {
                IOColorLines.println(io, message, Color.RED);
            } catch (IOException ex) {
                io.getOut().println(message);
                Exceptions.printStackTrace(ex);
            }
        }
        return level;
    }

    protected void addParticipants(final Gruppendatensatz gds, final List<String> addRollen, final List<Gruppendatensatz> gruppen) {
        gds.getGruppenzugehoerigkeiten().stream()
                .filter(gz -> {
                    final List<String> rollen = gz.getRollen();
                    return rollen.stream()
                            .anyMatch(r -> ROLLEN.stream().anyMatch(ar -> ar.equalsIgnoreCase(r)));
                })
                .filter(gz -> {
                    final List<String> rollen = gz.getRollen();
                    return addRollen == null
                            || rollen.stream()
                                    .anyMatch(r -> addRollen.stream().anyMatch(ar -> ar.equalsIgnoreCase(r)));
                })
                .map(Gruppenzugehoerigkeit::getKtid)
                .forEach(participants::add);
        for (final Referenzgruppe refGrp : gds.getGruppe().getReferenzgruppen()) {
            final Gruppendatensatz found = gruppen.stream()
                    .filter(g -> refGrp.getGrupid().equals(g.getGruppe().getId()))
                    .collect(SchulconnexUtil.collectByUUID(Gruppendatensatz::getGruppe));
            final List<String> refGrpRollen = refGrp.getRollen();
            addParticipants(found, refGrpRollen, gruppen);
        }
    }

    @NbBundle.Messages("SchulconnexKursItem.generateUnitId.warning.no.generated.unitid=Für die Schulconnex-Klasse \"{0}\" wurde keine Unit-ID gefunden!")
    private void generateUnitId(final SchulconnexImportConfiguration cfg, final String name, final Marker subject) {
        try {
            final NameParser parser = createNameParser(cfg);
            generatedUnitId = parser.findUnitId(name, subject, resolveReferenceYear());
        } catch (Exception ex) {
            generatedUnitId = null;
        }
        if (generatedUnitId == null) {
            final String msg = NbBundle.getMessage(SchulconnexKlasseItem.class,
                    "SchulconnexKursItem.generateUnitId.warning.no.generated.unitid", source.getGruppe().getBezeichnung());
            try {
                IOColorLines.println(ImportUtil.getIO(), msg, Color.RED);
            } catch (IOException ex) {
                ImportUtil.getIO().getOut().write(msg);
            }
        }
    }

    protected void generateTargetDocumentIdBase(final SchulconnexImportConfiguration cfg) {
        final UnitId u = getUnitId();
        if (u != null) {
            final NameParser pn2 = createNameParser(cfg);
            targetDocBase = pn2.translateUnitIdToTargetDocumentBase(u.getId(), getSubjectMarker(), null);
        }
    }

    protected NameParser createNameParser(final SchulconnexImportConfiguration cfg) {
        final NamingResolver nr = cfg.getNamingResolver();
        final String first = nr.properties().get("first-element");
        final String bl = nr.properties().get("base-level");
        if (bl != null) {
            try {
                baseLevel = Integer.valueOf(bl);
            } catch (NumberFormatException nfex) {
                baseLevel = null;
            }
        }
        final NameParser ret = new NameParser(cfg.getAuthority(), first, baseLevel);
        ret.setImportScripts(((ConfigurableImportTarget) getConfiguration()).getImportScripts());
        return ret;
    }

    protected int resolveReferenceYear() {
        if (currentTerm != null) {
            final Object j = currentTerm.getParameter("jahr");
            if (j instanceof Integer) {
                return (int) j;
            }
        }
        return Optional.ofNullable(source.getGruppe().getLaufzeit())
                .map(Laufzeit::getVon)
                .map(LocalDate::getYear)
                .orElse(LocalDate.now().getYear());
    }

    public Gruppendatensatz getSource() {
        return source;
    }

    public SchulconnexImportConfiguration getConfiguration() {
        return (SchulconnexImportConfiguration) getClientProperty(PROP_IMPORT_TARGET);
    }

    public void setSubjectAlternativeName(final String n) {
        final String before = this.subjectAlternativeName;
        this.subjectAlternativeName = n;
        try {
            vSupport.fireVetoableChange(ImportTargetsItem.PROP_SUBJECT_ALT_NAME, before, n);
        } catch (final PropertyVetoException ex) {
            this.subjectAlternativeName = before;
        }
    }

    public String getKurs() {
        return source.getGruppe().getBezeichnung();
    }

    public Typ getTyp() {
        return typ;
    }

    public Marker getRealm() {
        final String[] names = ((ConfigurableImportTarget) getConfiguration()).getRealmMarkerConventionNames();
        return getUniqueMarkerSet().getUnique(names);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    @Override
    public DocumentId getTargetDocumentIdBase() {
        return targetDocBase;
    }

    @Override
    public TargetDocumentProperties[] getImportTargets() {
        return getConfiguration().createTargetDocuments(this);
    }

    @Override
    public StudentId[] getUnitStudents() {
        return participants.getParticipants();
    }

    public int getUnitStudentsSize() {
        synchronized (participants.studs) {
            return participants.studs.size();
        }
    }

    @Override
    public UnitId getUnitId() {
        UnitId u = super.getUnitId();
        if (u != null) {
            return u;
        }
        return generatedUnitId;
    }

    @Override
    public void setUnitId(final UnitId unit) {
        if (unit == null || unit.equals(generatedUnitId)) {
            super.setUnitId(null);
        } else {
            super.setUnitId(unit);
        }
    }

    @Override
    public String getUnitDisplayName() {
        final UnitId u = getUnitId();
        if (u != null) {
            return resolveUnitDisplayName(u);
        }
        return null;
    }

    protected String resolveUnitDisplayName(final UnitId u) {
        final SchulconnexImportConfiguration cfg = getConfiguration();
        if (cfg != null) {
            final NamingResolver naming = cfg.getNamingResolver();
            try {
                return naming.resolveDisplayName(u, null);
            } catch (IllegalAuthorityException ex) {
                // fall through to default id rendering
            }
        }
        return u.getId();
    }

    @Override
    public boolean fileUnitParticipants() {
        return Typ.KURSUNTERRICHT.equals(typ);
    }

    @Override
    public boolean isFragment() {
        return false;
    }

    @Override
    public boolean isUnitIdGenerated() {
        return super.getUnitId() == null;
    }

    @Override
    public boolean existsUnitInSystem() {
        return existsUnit;
    }

    @Override
    public boolean isValid() {
        return participants.isValid()
                && getUnitId() != null;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return 79 * hash + Objects.hashCode(this.source);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SchulconnexKursItem other = (SchulconnexKursItem) obj;
        return Objects.equals(this.source.getGruppe().getId(), other.source.getGruppe().getId());
    }

    private final class DelayedKursStudentSet extends AbstractDelayedStudents<SchulconnexKursItem> {

        private final Map<String, StudentId> studs = new HashMap<>();

        private DelayedKursStudentSet() {
            super(SchulconnexKursItem.this);
        }

        void add(String uuid) {
            synchronized (studs) {
                studs.put(uuid, StudentId.NULL);
                initialize(uuid);
            }
        }

        @Override
        protected void onLoad() {
            final VCardStudents students;
            try {
                students = getVCardStudents();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }
            students.getStudents().stream()
                    .map(s -> s.getVCard().toString())
                    .forEach(System.out::println);
            synchronized (studs) {
                studs.forEach((k, v) -> initialize(k));
            }
        }

        private void initialize(final String uuid) {
            final VCardStudents students;
            try {
                students = getVCardStudents();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }
            if (students != null && students.getLoadTask().isFinished()) {
                StudentId sid = studs.get(uuid);
                if (StudentId.isNull(sid)) {
                    final List<VCardStudent> found = students.getStudents().stream()
                            .filter(vcs -> vcs.getVCard().getAnyPropertyValue(Schulconnex.VCARD_PROP_SCHULCONNEX_UUID).filter(p -> p.equals(uuid)).isPresent())
                            .collect(Collectors.toList());
                    if (found.size() > 1) {
                        handleConflict(found, uuid);
                    } else if (found.size() == 1) {
                        studs.put(uuid, found.get(0).getStudentId());
                    } else {
                        handleConflict(found, uuid);
                    }
                }
            }
        }

        @NbBundle.Messages("SchulconnexKursItem.message.participantNotFound=Es wurde kein Schüler/-in oder mehrere Schüler/-innen mit der Schulconnex-UUID {0} in der Datenbank gefunden: {1}")
        protected void handleConflict(final List<VCardStudent> found, String uuid) {
            final String names = found.stream()
                    .map(vcs -> vcs.getDirectoryName())
                    .collect(Collectors.joining(";"));
            InputOutput io = ImportUtil.getIO();
            String message = NbBundle.getMessage(SchulconnexKursItem.class, "SchulconnexKursItem.message.participantNotFound", uuid, names);
            try {
                IOColorLines.println(io, message, Color.RED);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public StudentId[] getParticipants() {
            synchronized (studs) {
                return studs.entrySet().stream()
                        .map(Map.Entry::getValue)
                        .filter(sid -> !StudentId.isNull(sid))
                        .distinct()
                        .toArray(StudentId[]::new);
            }
        }

        private boolean isValid() {
            synchronized (studs) {
                return studs.entrySet().stream()
                        .filter(i -> StudentId.isNull(i.getValue()))
                        .count() == 0;
            }
        }

        public void clear() {
            synchronized (studs) {
                studs.clear();
            }
        }

    }
}
