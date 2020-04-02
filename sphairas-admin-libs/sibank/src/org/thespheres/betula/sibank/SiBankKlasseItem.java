/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank;

import org.thespheres.betula.xmlimport.utilities.ImportStudentKey;
import java.awt.Color;
import org.thespheres.betula.xmlimport.uiutil.OutlineModelNode;
import org.thespheres.betula.sibank.impl.DelayedKlasseStudentSet;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOColorLines;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.ical.builder.VCardBuilder;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.ServiceConstants;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.VCard;

/**
 *
 * @author boris.heithecker
 */
public class SiBankKlasseItem extends ImportTargetsItem implements OutlineModelNode {

//    public static final String PROP_CONFIGURATION = "configuration";
//    private final AcceptToTargetFilter filter = null;
//    protected SiBankImportTarget configuration;
    private final GeneratedUnitId generatedUnit = new GeneratedUnitId();
    private boolean existsUnit;
    private Term term;
    private final String klasse;
    private final int stufe;
//    private final Map<StudentId, ImportStudentKey> schueler = new HashMap<>();
    private final DelayedKlasseStudentSet schueler = new DelayedKlasseStudentSet(this);

    public SiBankKlasseItem(String klasse, int stufe) {
        super(klasse);
        this.klasse = klasse;
        this.stufe = stufe;
        //        if (kurs) {
//            if (this.kursnr == null) {
//                //if no kursnr is set, we suppose unit is this.klasse
//                this.klasse = s.klasse;
//            }
//        } else {
//            this.klasse = s.klasse;
//        }
    }

    public String getKlasse() {
        return klasse;
    }

    public int getStufe() {
        return stufe;
    }

    public SiBankImportTarget getConfiguration() {
        return (SiBankImportTarget) getClientProperty(PROP_IMPORT_TARGET);
    }

    public synchronized void initialize(final SiBankImportTarget config, final SiBankImportData wizard) {
        final SiBankImportTarget oldCfg = getConfiguration();
        boolean configChanged = oldCfg == null
                || !oldCfg.getProviderInfo().equals(config.getProviderInfo());

        uniqueMarkers.add(ServiceConstants.BETULA_PRIMARY_UNIT_MARKER);

        term = (Term) wizard.getProperty(AbstractFileImportAction.TERM);

        if (configChanged) {
            schueler.setConfiguration(config);
        }

        if (configChanged || getDeleteDate() == null) {
            setDeleteDate(ImportUtil.calculateDeleteDate(stufe, 5, Month.JULY));
        }

        termScheduleProvider = config.getTermSchemeProvider().getInfo().getURL();
//        configuration = config;
        try {
            setClientProperty(PROP_IMPORT_TARGET, config);
//            vSupport.fireVetoableChange(PROP_CONFIGURATION, oldCfg, configuration);
        } catch (PropertyVetoException ex) {
        }
        //Must be called after config field reset
        existsUnit = Units.get(config.getWebServiceProvider().getInfo().getURL())
                .map(u -> u.hasUnit(getUnitId()))
                .orElse(Boolean.FALSE);
    }

    @Messages({"SiBankKlasseItem.noImport.studentNotAktiv={0} ist nicht \"AKTIV\", wird nicht in Kurs {1} importiert."})
    public void addStudentFromSatz(DatenExportXml.Satz s) throws IOException {
        final String stud = s.getInferredDirectoryName(DatenExportXml.File.SCHUELER);
        final String dob = s.gebdatum;
        if (stud != null && dob != null) {
            final ImportStudentKey key = new ImportStudentKey(stud, dob, LocalDate.parse(dob, SiBankImportData.SIBANK_DATUM));
            final boolean aktiv = s.status != null && s.status.equalsIgnoreCase("aktiv");
//            if ((aktiv) || ignoreStatus) {
            final VCardStudent vcs = findStudentId(s);
            synchronized (schueler) {
                final SiBankImportStudentItem put = schueler.put(vcs, key);
                try {
                    put.setSelected(aktiv);
                } catch (PropertyVetoException ex) {
                }
                try {
                    put.setSourceStatus(s.status);
                } catch (PropertyVetoException ex) {
                    ImportUtil.getIO().getErr().write(ex.getLocalizedMessage());
                }
                try {
                    put.setSourceStudentCareer(s.bildungsgang);
                } catch (PropertyVetoException ex) {
                    ImportUtil.getIO().getErr().write(ex.getLocalizedMessage());
                }
            }
//            } else {
//                String warn = NbBundle.getMessage(SiBankKlasseItem.class, "SiBankKlasseItem.noImport.studentNotAktiv", key, getSourceNodeLabel());
//                ImportUtil.getIO().getOut().write(warn);
//            }
        }
    }

    protected VCardStudent findStudentId(DatenExportXml.Satz s) throws IOException {
        StudentId studentId = createStudentId(s, getConfiguration(), term);
        final VCardStudent ret = new VCardStudent(studentId);
        final VCardBuilder vb = new VCardBuilder();
        try {
            vb.addProperty(VCard.FN, s.findFN())
                    .addProperty(VCard.N, s.findN())
                    .addProperty(VCard.GENDER, s.findGender())
                    .addProperty(VCard.BDAY, s.findBDay())
                    .addProperty(VCard.BIRTHPLACE, s.geburtsOrt);
        } catch (InvalidComponentException icex) {
            throw new IOException(icex);
        }
        ret.setVCard(vb.toVCard());
        return ret;
    }

    static StudentId createStudentId(final DatenExportXml.Satz s, final SiBankImportTarget configuration, final Term term) throws IOException {
        final String id;
        if (s.identNummer == null || StringUtils.isBlank(id = s.identNummer.trim())) {
            return null;
        }
        if (term == null || configuration == null) {
            throw new IOException("Parameter siBankImportTarget and/or term cannot be null.");
        }
        final Long ident;
        try {
            ident = Long.parseLong(id);
        } catch (NumberFormatException nex) {
            throw new IOException(nex);
        }
        final String auth = configuration.getStudentsAuthority(term);
        return new StudentId(auth, ident);
    }

    @Override
    public StudentId[] getUnitStudents() {
        return schueler.getUnitStudents();
    }

    public Map<StudentId, SiBankImportStudentItem> getStudents() {
        return schueler.getStudents();
    }

    public Term getTerm() {
        return term;
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
    public void setUnitId(UnitId unit) {
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

    public String resolveUnitDisplayName(UnitId u) {
        final SiBankImportTarget cfg = getConfiguration();
        if (cfg != null) {
            final NamingResolver naming = cfg.getNamingResolver();
            try {
                return naming.resolveDisplayName(u, term);
            } catch (IllegalAuthorityException ex) {
            }
        }
        return u.getId();
    }

    @Override
    public String getHtmlDisplayName() {
        String name = getKlasse();
        return "<html>" + name + "</html>";
//                if (isMissing()) {
//            String name = this.klasse != null ? this.klasse : this.kursnr;
//            return "<html><font color=\"RED\">" + "Vermisst: " + "</font>" + name + "</html>";
//        }
    }

    @Override
    public String getTooltip() {
        return "";
    }

    @Override
    public Object getColumn(String id) {
        switch (id) {
            case "fach":
                return getUnitId() != null ? getUnitId().getId() : null;
            case "fachart":
                return null;
            case "lehrer":
                return null;
        }
        return "";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.klasse);
        hash = 37 * hash + this.stufe;
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
        final SiBankKlasseItem other = (SiBankKlasseItem) obj;
        if (!Objects.equals(this.klasse, other.klasse)) {
            return false;
        }
        return this.stufe == other.stufe;
    }

    @Messages({"SiBankKlasseItem.GeneratedUnitId.notify.generated.unitid=Für die SiBank-Plus Klasse \"{0}\" wurde die ID {1} gefunden.",
        "SiBankKlasseItem.GeneratedUnitId.warning.no.generated.unitid=Für die SiBank-Plus Klasse \"{0}\" wurde keine ID gefunden!"})
    class GeneratedUnitId implements VetoableChangeListener {

        private boolean isInit = false;
        private UnitId uid;

        @SuppressWarnings("LeakingThisInConstructor")
        private GeneratedUnitId() {
            SiBankKlasseItem.this.addVetoableChangeListener(this);
        }

        private synchronized UnitId getUnitId() {
            if (!isInit) {
                if (term != null) {
                    Object jProp = term.getParameter("jahr");
                    if (jProp != null && jProp instanceof Integer) {
                        int rJahr = (int) jProp;//aus untisdaten
                        try {
                            uid = getConfiguration().initPreferredPrimaryUnitId(getKlasse(), (Integer) rJahr);
//                            uid = PreferredNames.create(getConfiguration().getAuthority()).initPreferredPrimaryUnitId(getKlasse(), (Integer) rJahr);
                        } catch (Exception e) {
                        }
//                        String st = Integer.toString(stufe);
//                        String kid = klasse.replace(st, "");
//                        String idvalue = TranslateID.findId(stufe, rJahr, null, kid, "kgs");
//                        uid = new UnitId(configuration.getAuthority(), idvalue);
                    }
                }
                if (uid != null) {
//                    String msg = NbBundle.getMessage(SiBankKlasseItem.class, "SiBankKlasseItem.GeneratedUnitId.notify.generated.unitid", getKlasse(), uid.getId());
//                    ImportUtil.getIO().getOut().write(msg);
                } else {
                    String msg = NbBundle.getMessage(SiBankKlasseItem.class, "SiBankKlasseItem.GeneratedUnitId.warning.no.generated.unitid", getKlasse());
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

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        }

    }

}
