package org.thespheres.betula.schulconnex;

import de.schulconnex.qs.model.Gruppendatensatz;
import de.schulconnex.qs.model.Gruppenzugehoerigkeit;
import de.schulconnex.qs.model.Laufzeit;
import de.schulconnex.qs.model.Personendatensatz;
import de.schulconnex.qs.model.Personenkontext;
import java.awt.Color;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
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
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.parse.NameParser;
import org.thespheres.betula.xmlimport.utilities.AbstractDelayedStudents;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;

/**
 *
 * @author boris.heithecker
 */
public class SchulconnexKlasseItem extends ImportTargetsItem {

    private UnitId generatedUnitId;
    private boolean selected = false;
    private boolean existsUnit;
    private final DelayedKlasseStudentSet schueler = new DelayedKlasseStudentSet();
    private final Gruppendatensatz source;
    private Integer baseLevel;
    private final Term currentTerm;

    SchulconnexKlasseItem(final Gruppendatensatz gds, final List<Personendatensatz> personen, final SchulconnexImportConfiguration config, final Term current) {
        super(gds.getGruppe().getBezeichnung());
        this.source = gds;
        this.currentTerm = current;
        initialize(personen, config);
    }

    private void initialize(final List<Personendatensatz> personen, final SchulconnexImportConfiguration config) {
        try {
            setClientProperty(PROP_IMPORT_TARGET, config);
        } catch (PropertyVetoException ex) {
            ex.printStackTrace(ImportUtil.getIO().getErr());
            return;
        }

        uniqueMarkers.add(ServiceConstants.BETULA_PRIMARY_UNIT_MARKER);
        schueler.setConfiguration(config);
        generateUnitId(config);

        existsUnit = Units.get(config.getWebServiceProvider().getInfo().getURL())
                .map(u -> u.hasUnit(getUnitId()))
                .orElse(Boolean.FALSE);

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
        getSource().getGruppenzugehoerigkeiten().stream()
                .filter(gz -> gz.getRollen().stream().anyMatch("lern"::equalsIgnoreCase))
                .map(gz -> createSchulconnexStudentItem(gz, personen, config))
                .filter(Objects::nonNull)
                .forEach(schueler::add);
    }

    @Messages({"SchulconnexKlasseItem.initialize.warning.no.level=Für die Schulconnex-Klasse \"{0}\" mit den Schulconnex-Jahrgangsstufen \"{1}\" kann keine eindeutige Stufe bestimmt werden."})
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
            final String message = NbBundle.getMessage(SchulconnexKlasseItem.class, "SchulconnexKlasseItem.initialize.warning.no.level", getKlasse(), concat);
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

    @Messages("SchulconnexKlasseItem.generateUnitId.warning.no.generated.unitid=Für die Schulconnex-Klasse \"{0}\" wurde keine Unit-ID gefunden!")
    private void generateUnitId(final SchulconnexImportConfiguration cfg) {
        try {
            final NameParser parser = createNameParser(cfg);
            generatedUnitId = parser.findUnitId(getKlasse(), resolveReferenceYear());
        } catch (Exception ex) {
            generatedUnitId = null;
        }
        if (generatedUnitId == null) {
            final String msg = NbBundle.getMessage(SchulconnexKlasseItem.class,
                    "SchulconnexKlasseItem.generateUnitId.warning.no.generated.unitid", getKlasse());
            try {
                IOColorLines.println(ImportUtil.getIO(), msg, Color.RED);
            } catch (IOException ex) {
                ImportUtil.getIO().getOut().write(msg);
            }
        }
    }

    private NameParser createNameParser(final SchulconnexImportConfiguration cfg) {
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

    private int resolveReferenceYear() {
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
        return source.getGruppe().getBezeichnung();
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
    public int hashCode() {
        int hash = 5;
        return 89 * hash + Objects.hashCode(this.source.getGruppe().getId());
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
        final SchulconnexKlasseItem other = (SchulconnexKlasseItem) obj;
        return Objects.equals(this.source.getGruppe().getId(), other.source.getGruppe().getId());
    }

    private final class DelayedKlasseStudentSet extends AbstractDelayedStudents<SchulconnexKlasseItem> {

        private final Set<SchulconnexStudentItem> studs = new HashSet<>();

        private DelayedKlasseStudentSet() {
            super(SchulconnexKlasseItem.this);
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

    }

}
