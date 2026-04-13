package org.thespheres.betula.schulconnex;

import de.schulconnex.qs.model.Gruppendatensatz;
import de.schulconnex.qs.model.Gruppe;
import de.schulconnex.qs.model.Laufzeit;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOColorLines;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.ServiceConstants;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.parse.NameParser;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;

/**
 *
 * @author boris.heithecker
 */
public class SchulconnexKlasseItem extends ImportTargetsItem {

    private final GeneratedUnitId generatedUnit = new GeneratedUnitId();
    private boolean initialized;
    private boolean existsUnit;
    private final DelayedKlasseStudentSet schueler = new DelayedKlasseStudentSet();
    private final Gruppendatensatz source;

    private SchulconnexKlasseItem(final Gruppendatensatz gds) {
        super(findSourceLabel(gds));
        this.source = gds;
    }

    public static List<SchulconnexKlasseItem> createList(final List<Gruppendatensatz> l, final SchulconnexImportConfiguration config) {
        return l.stream()
                .filter(g -> g != null && g.getGruppe() != null)
                .filter(g -> "KLASSE".equals(g.getGruppe().getTyp()))
                .map(SchulconnexKlasseItem::new)
                .peek(i -> i.initialize(config))
                .collect(Collectors.toList());
    }

    public synchronized void initialize(final SchulconnexImportConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null");
        }
        if (initialized) {
            return;
        }

        uniqueMarkers.add(ServiceConstants.BETULA_PRIMARY_UNIT_MARKER);
        schueler.setConfiguration(config);

        termScheduleProvider = config.getTermSchemeProvider().getInfo().getURL();
        final Laufzeit laufzeit = getLaufzeit();
        if (laufzeit != null && laufzeit.getBis() != null) {
            setDeleteDate(laufzeit.getBis());
        }

        try {
            setClientProperty(PROP_IMPORT_TARGET, config);
        } catch (PropertyVetoException ex) {
            throw new IllegalStateException(ex);
        }

        existsUnit = Units.get(config.getWebServiceProvider().getInfo().getURL())
                .map(u -> u.hasUnit(getUnitId()))
                .orElse(Boolean.FALSE);
        initialized = true;
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

    private static String findSourceLabel(final Gruppendatensatz gds) {
        if (gds == null || gds.getGruppe() == null || gds.getGruppe().getBezeichnung() == null) {
            return "";
        }
        return gds.getGruppe().getBezeichnung();
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
                    baseLevel = Integer.parseInt(bl);
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

    private static final class DelayedKlasseStudentSet {

        private void setConfiguration(final SchulconnexImportConfiguration config) {
            // intentionally no-op for now; student assembly will be added in next step.
        }

        private boolean isValid() {
            return true;
        }
    }

}
