package org.thespheres.betula.schulconnex;

import de.schulconnex.qs.model.Gruppendatensatz;
import de.schulconnex.qs.model.Gruppe;
import de.schulconnex.qs.model.Gruppenzugehoerigkeit;
import de.schulconnex.qs.model.Laufzeit;
import de.schulconnex.qs.model.Personendatensatz;
import de.schulconnex.qs.model.Personenkontext;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOColorLines;
import org.openide.windows.InputOutput;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.ServiceConstants;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.parse.NameParser;
import org.thespheres.betula.xmlimport.utilities.AbstractDelayedStudents;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;

/**
 *
 * @author boris.heithecker
 */
public class SchulconnexKlasseItem extends ImportTargetsItem {

    private final GeneratedUnitId generatedUnit = new GeneratedUnitId();
    private boolean selected = false;
    private boolean existsUnit;
    private final DelayedKlasseStudentSet schueler = new DelayedKlasseStudentSet();
    private final Gruppendatensatz source;

    SchulconnexKlasseItem(final Gruppendatensatz gds, final List<Personendatensatz> personen, final SchulconnexImportConfiguration config) {
        super(findSourceLabel(gds));
        this.source = gds;
        initialize(personen, config);
    }

    static String findSourceLabel(final Gruppendatensatz gds) {
        return gds.getGruppe().getBezeichnung();
    }

    @Messages({"SchulconnexKlasseItem.initialize.warning.no.level=Für die Schulconnex-Klasse \"{0}\" mit den Schulconnex-Jahrgangsstufen \"{1}\" kann keine eindeutige Stufe bestimmt werden."})
    private void initialize(final List<Personendatensatz> personen, final SchulconnexImportConfiguration config) {
        try {
            setClientProperty(PROP_IMPORT_TARGET, config);
        } catch (PropertyVetoException ex) {
            ex.printStackTrace(ImportUtil.getIO().getErr());
            return;
        }
//        Term term = (Term) wizard.getProperty(AbstractFileImportAction.TERM);
//        try {
//            setClientProperty(ImportTargetsItem.PROP_SELECTED_TERM, term);
//        } catch (PropertyVetoException ex) {
//        }

        uniqueMarkers.add(ServiceConstants.BETULA_PRIMARY_UNIT_MARKER);
        schueler.setConfiguration(config);

//        termScheduleProvider = config.getTermSchemeProvider().getInfo().getURL();
//        final Laufzeit laufzeit = getLaufzeit();
//        if (laufzeit != null && laufzeit.getBis() != null) {
//            setDeleteDate(laufzeit.getBis());
//        }
        existsUnit = Units.get(config.getWebServiceProvider().getInfo().getURL())
                .map(u -> u.hasUnit(getUnitId()))
                .orElse(Boolean.FALSE);

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
            final String message = NbBundle.getMessage(SchulconnexKlasseItem.class, "SchulconnexKlasseItem.initialize.warning.no.level", getKlasse(), concat);
            InputOutput io = ImportUtil.getIO();
            try {
                IOColorLines.println(io, message, Color.RED);
            } catch (IOException ex) {
                io.getOut().println(message);
                Exceptions.printStackTrace(ex);
            }
        }
        final int baseLevel = 5;
        setDeleteDate(ImportUtil.calculateDeleteDate(level, baseLevel, Month.JULY));

//        Signees.get(config.getWebServiceProvider().getInfo().getURL())
//                .flatMap(s -> s.findSignee(getSourceSigneeName()))
//                .ifPresent(this::setSignee);
        schueler.setConfiguration(config);
        schueler.clear();
//             transformer.setParameter("authority", config.getAuthority());
        getSource().getGruppenzugehoerigkeiten().stream()
                .filter(gz -> gz.getRollen().stream().anyMatch("lern"::equalsIgnoreCase))
                .map(gz -> createSchulconnexStudentItem(gz, personen, config))
                .filter(Objects::nonNull)
                .forEach(schueler::add);
    }

    private SchulconnexStudentItem createSchulconnexStudentItem(final Gruppenzugehoerigkeit gz, final List<Personendatensatz> personen, final SchulconnexImportConfiguration config) {
        for (final Personendatensatz pds : personen) {
            for (final Personenkontext pek : pds.getPersonenkontexte()) {
                if (pek.getId().equals(gz.getKtid())) {
                    final String label = SchulconnexUtil.createSortableName(pds.getPerson().getName()) + " (" + getKlasse() + ")";
                    return new SchulconnexStudentItem(label, pds.getPerson(), pek, config);
                }
            }
        }
        return null;
    }

    public Gruppendatensatz getSource() {
        return source;
    }

    public SchulconnexImportConfiguration getConfiguration() {
        return (SchulconnexImportConfiguration) getClientProperty(PROP_IMPORT_TARGET);
    }

    public String getKlasse() {
        return findSourceLabel(source);
    }

    public Set<SchulconnexStudentItem> getImportStudents() {
        return schueler.studs;
    }

    @Override
    public StudentId[] getUnitStudents() {
        return schueler.getUnitStudents();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    @Override
    public DocumentId getTargetDocumentIdBase() {
        return null;
    }

    @Override
    public TargetDocumentProperties[] getImportTargets() {
        return new TargetDocumentProperties[0];
    }

    @Override
    public boolean fileUnitParticipants() {
        return true;
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
        return schueler.isValid()
                && getUnitId() != null;
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
    public void setUnitId(final UnitId unit) {
        if (unit == null || unit.equals(generatedUnit.getUnitId())) {
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

    public String resolveUnitDisplayName(final UnitId u) {
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
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(source != null && source.getGruppe() != null ? source.getGruppe().getOrgid() : null);
        hash = 67 * hash + Objects.hashCode(source != null && source.getGruppe() != null ? source.getGruppe().getBezeichnung() : null);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SchulconnexKlasseItem other = (SchulconnexKlasseItem) obj;
        return Objects.equals(source != null && source.getGruppe() != null ? source.getGruppe().getOrgid() : null,
                other.source != null && other.source.getGruppe() != null ? other.source.getGruppe().getOrgid() : null)
                && Objects.equals(source != null && source.getGruppe() != null ? source.getGruppe().getBezeichnung() : null,
                        other.source != null && other.source.getGruppe() != null ? other.source.getGruppe().getBezeichnung() : null);
    }

    private Laufzeit getLaufzeit() {
        final Gruppe g = source != null ? source.getGruppe() : null;
        return g != null ? g.getLaufzeit() : null;
    }

    @Messages({
        "SchulconnexKlasseItem.GeneratedUnitId.warning.no.generated.unitid=Für die Schulconnex Klasse \"{0}\" wurde keine ID gefunden!"
    })
    class GeneratedUnitId implements VetoableChangeListener {

        private boolean isInit;
        private UnitId uid;

        @SuppressWarnings("LeakingThisInConstructor")
        private GeneratedUnitId() {
            SchulconnexKlasseItem.this.addVetoableChangeListener(this);
        }

        private synchronized UnitId getUnitId() {
            if (!isInit) {
                final SchulconnexImportConfiguration cfg = getConfiguration();
                if (cfg != null) {
                    try {
                        final NameParser parser = createNameParser(cfg);
                        uid = parser.findUnitId(getKlasse(), resolveReferenceYear());
                    } catch (Exception ex) {
                        uid = null;
                    }
                }
                if (uid == null) {
                    final String msg = NbBundle.getMessage(SchulconnexKlasseItem.class,
                            "SchulconnexKlasseItem.GeneratedUnitId.warning.no.generated.unitid", getKlasse());
                    try {
                        IOColorLines.println(ImportUtil.getIO(), msg, Color.RED);
                    } catch (IOException ex) {
                        ImportUtil.getIO().getOut().write(msg);
                    }
                }
                isInit = true;
            }
            return uid;
        }

        private NameParser createNameParser(final SchulconnexImportConfiguration cfg) {
            final NamingResolver nr = cfg.getNamingResolver();
            final String first = nr.properties().get("first-element");
            final String bl = nr.properties().get("base-level");
            Integer baseLevel = null;
            if (bl != null) {
                try {
                    baseLevel = Integer.valueOf(bl);
                } catch (NumberFormatException nfex) {
                    baseLevel = null;
                }
            }
            return new NameParser(cfg.getAuthority(), first, baseLevel);
        }

        private int resolveReferenceYear() {
            final Laufzeit lz = getLaufzeit();
            final LocalDate from = lz != null ? lz.getVon() : null;
            return from != null ? from.getYear() : LocalDate.now().getYear();
        }

        @Override
        public void vetoableChange(final PropertyChangeEvent evt) throws PropertyVetoException {
        }
    }

    private final class DelayedKlasseStudentSet extends AbstractDelayedStudents<SchulconnexKlasseItem> {

        private final Set<SchulconnexStudentItem> studs = new HashSet<>();

        private DelayedKlasseStudentSet() {
            super(SchulconnexKlasseItem.this);
        }

        @Override
        protected void onLoad() {
            // TODO Schulconnex: initialize student/VCard mapping from getVCardStudents()
            // once Personendatensatz to VCardStudent mapping is implemented
            final VCardStudents students;
            try {
                students = getVCardStudents();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }
            synchronized (studs) {
                studs.forEach(i -> i.initialize(students));
            }
        }

        void add(SchulconnexStudentItem item) {
            final VCardStudents students;
            try {
                students = getVCardStudents();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }
            synchronized (studs) {
                studs.add(item);
                if (students.getLoadTask().isFinished()) {
                    item.initialize(students);
                }
            }
        }

        public StudentId[] getUnitStudents() {
            synchronized (studs) {
                return studs.stream()
                        .map(SchulconnexStudentItem::getStudentId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toArray(StudentId[]::new);
            }
        }

        private boolean isValid() {
            synchronized (studs) {
                return studs.stream()
                        .filter(i -> i.getStudentId() == null)
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
