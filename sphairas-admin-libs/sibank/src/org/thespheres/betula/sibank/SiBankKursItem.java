/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank;

import org.thespheres.betula.xmlimport.utilities.ImportStudentKey;
import org.thespheres.betula.xmlimport.uiutil.OutlineModelNode;
import org.thespheres.betula.sibank.impl.DelayedKursStudentSet;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.assess.GradeParsingException;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.xmlimport.parse.TranslateID;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.sibank.DatenExportXml.File;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.parse.NameParser;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;

/**
 *
 * @author boris.heithecker
 */
public class SiBankKursItem extends ImportTargetsItem implements ImportItem.CloneableImport, OutlineModelNode {

    public static final String PROP_TARGET_ID = "targetId";
    static final Pattern TARGET_ID_PATTERN = Pattern.compile("[\\w]+[-\\w]*", 0); //Centralize
    private String targetId;
    private final GeneratedUnitId generatedUnit = new GeneratedUnitId();
    private DocumentId targetDocBase;
    private boolean existsUnit;
    private final UniqueSatzDistinguisher makeUnique;
    private final int clone;
    private final DatenExportXml.File type;//nur Kurs oder AG
    private final DelayedKursStudentSet delayed = new DelayedKursStudentSet(this);
    private DocumentId[] targetDocBaseOptions;
    private final SiBankImportData<SiBankKursItem> wizard;

    public SiBankKursItem(final UniqueSatzDistinguisher id, final DatenExportXml.File type, final SiBankImportData<SiBankKursItem> wizard, final int clone) {
        super("SiBank-Kurs", id.getFach(), id.getLname());
        this.makeUnique = id;
        this.type = type;
        this.clone = clone;
        this.wizard = wizard;
    }

    @Override
    public String getSourceNodeLabel() {
        return makeUnique.sourceNodeLabel(type);
    }

    public SiBankKursItem(UniqueSatzDistinguisher id, DatenExportXml.File type, SiBankImportData<SiBankKursItem> wizard) {
        this(id, type, wizard, 0);
    }

    @Override
    public int id() {
        return clone;
    }

    public File getImportFile() {
        return type;
    }

    public UniqueSatzDistinguisher getDistinguisher() {
        return makeUnique;
    }

    public SiBankImportData<SiBankKursItem> getImportData() {
        return wizard;
    }

    public int getStufe() {
        return makeUnique.getStufe();
    }

    public String getFach() {
        return makeUnique.getFach();
    }

    public String getFachart() {
        return makeUnique.getFachart();
    }

    public String getKursnr() {
        return makeUnique.getKursnr();
    }

    public String getLkuerzel() {
        return makeUnique.getLkuerzel();
    }

    public String getLname() {
        return makeUnique.getLname();
    }

    public void initializeFrom(final SiBankKursItem l, final SiBankImportData wizard) throws IOException {
        initialize(l.getConfiguration(), wizard);
        class PCL implements VetoableChangeListener {

            @Override
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                if (PROP_IMPORT_TARGET.equals(evt.getPropertyName())) {
                    try {
                        setCopyFrom();
                    } catch (IOException ex) {
                        PropertyVetoException pex = new PropertyVetoException(ex.getMessage(), evt);
                        pex.initCause(ex);
                        throw pex;
                    } finally {
                        SiBankKursItem.this.removeVetoableChangeListener(this);
                    }
                }
            }

            private void setCopyFrom() throws IOException {
                delayed.copyFrom(l.delayed);
            }

        }
        final PCL n = new PCL();
        if (getConfiguration() != null) {
            n.setCopyFrom();
        } else {
            addVetoableChangeListener(n);
        }
    }

    public synchronized void initialize(final SiBankImportTarget config, final SiBankImportData wizard) {
        final SiBankImportTarget oldCfg = getConfiguration();
        boolean configChanged = oldCfg == null
                || !oldCfg.getProviderInfo().equals(config.getProviderInfo());

        final AssessmentConvention conv = getImportFile().equals(File.AGS) ? GradeFactory.findConvention("niedersachsen.teilnahme") : GradeFactory.findConvention("de.notensystem");
        setAssessmentConvention(conv);

        Term term = (Term) wizard.getProperty(AbstractFileImportAction.TERM);
        try {
            setClientProperty(ImportTargetsItem.PROP_SELECTED_TERM, term);
        } catch (PropertyVetoException ex) {
        }

        if (configChanged) {
            delayed.setConfiguration(config);
        }

        if (configChanged || getDeleteDate() == null) {
            setDeleteDate(ImportUtil.calculateDeleteDate(makeUnique.getStufe(), 5, Month.JULY));
        }

        if (configChanged || getSubjectMarker() == null) {
            final String sourceValue = getSourceSubject();
            if (!StringUtils.isBlank(sourceValue)) {
                final Marker fach = Arrays.stream(config.getSubjectMarkerConventions())
                        .flatMap(mc -> Arrays.stream(mc.getAllMarkers()))
                        .filter(m -> (m.getLongLabel().equalsIgnoreCase(sourceValue)))
                        .findAny()
                        .orElse(Marker.NULL);
                setSubjectMarker(fach);
            }
        }

        if (configChanged && getSourceSigneeName() != null) {
            Signees.get(config.getWebServiceProvider().getInfo().getURL())
                    .flatMap(s -> s.findSignee(getSourceSigneeName()))
                    .ifPresent(this::setSignee);
        }
        termScheduleProvider = config.getTermSchemeProvider().getInfo().getURL();

        try {
            setClientProperty(PROP_IMPORT_TARGET, config);
        } catch (PropertyVetoException ex) {
            ex.printStackTrace(ImportUtil.getIO().getErr());
        }
        //Must be called after config field reset
        updateExistsUnit();
    }

    public SiBankImportTarget getConfiguration() {
        return (SiBankImportTarget) getClientProperty(PROP_IMPORT_TARGET);
    }

    private void updateExistsUnit() {
        existsUnit = Units.get(getConfiguration().getWebServiceProvider().getInfo().getURL())
                .map(u -> u.hasUnit(getUnitId()))
                .orElse(Boolean.FALSE);
    }

    @Messages("SiBankKursItem.noImport.studentNotAktiv={0} ist nicht \"AKTIV\", wird nicht in Kurs {1} importiert.")
    public void addStudentFromSatz(DatenExportXml.Satz s, boolean ignoreStatus) throws IOException {
        final String stud = s.getInferredDirectoryName(type);
        final String dob = s.gebdatum;
        if (stud != null && dob != null) {
            final ImportStudentKey key = new ImportStudentKey(stud, dob, LocalDate.parse(dob, SiBankImportData.SIBANK_DATUM));
            if ((s.status != null && s.status.equalsIgnoreCase("aktiv")) || ignoreStatus) {
                String n = s.note;
                Grade parsed = null;
                if (!StringUtils.isBlank(n) && getAssessmentConvention() != null) {
                    try {
                        parsed = getAssessmentConvention().parseGrade(n);
                    } catch (GradeParsingException ex) {
                    }
                }
                final StudentId studentId = SiBankKlasseItem.createStudentId(s, getConfiguration(), getTerm());
                key.setStudentId(studentId);
                delayed.put(key, parsed);
                final String siBankKlasse = StringUtils.trimToNull(s.klasse);
                if (siBankKlasse != null) {
                    makeUnique.addKlasse(siBankKlasse);
                }
            } else {
                String warn = NbBundle.getMessage(SiBankKursItem.class, "SiBankKursItem.noImport.studentNotAktiv", key, getSourceNodeLabel());
                ImportUtil.getIO().getOut().write(warn);
            }
        }
    }

    @Override
    public StudentId[] getUnitStudents() {
        return delayed.getUnitStudents();
    }

    @Override
    public DocumentId getTargetDocumentIdBase() {
        if (targetDocBase == null) {
            final UnitId u = getUnitId();
            if (u != null) {
                //final String source = u.getId();
                //final String id = TranslateID.translateUnitToTarget(source, getSubjectMarker(), getCustomDocumentIdIdentifier());
                //targetDocBase = new DocumentId(getConfiguration().getAuthority(), id, DocumentId.Version.LATEST);
                final NameParser pn2 = generatedUnit.createNameParser();
                targetDocBase = pn2.translateUnitIdToTargetDocumentBase(u.getId(), getSubjectMarker(), new String[]{getCustomDocumentIdIdentifier()});
                if (!targetDocBase.getId().equals(u.getId())) {
                    //final String alt = TranslateID.translateUnitToTarget(source, null, getCustomDocumentIdIdentifier());
                    //final DocumentId altBase = new DocumentId(getConfiguration().getAuthority(), alt, DocumentId.Version.LATEST);
                    final DocumentId altBase = pn2.translateUnitIdToTargetDocumentBase(u.getId(), (Marker) null, new String[]{getCustomDocumentIdIdentifier()});
                    targetDocBaseOptions = new DocumentId[]{targetDocBase, altBase};
                }
            }
        }
        return targetDocBase;
    }

    @Override
    public DocumentId[] getTargetDocumentIdBaseOptions() {
        //need to call getTargetDocumentIdBase() to initialize fields
        if (getTargetDocumentIdBase() != null && targetDocBaseOptions != null) {
            return targetDocBaseOptions;
        }
        return super.getTargetDocumentIdBaseOptions();
    }

    @Override
    protected void setTargetDocumentIdBaseOption(final DocumentId did) {
        targetDocBase = did;
    }

    public Term getTerm() {
        return (Term) getClientProperty(ImportTargetsItem.PROP_SELECTED_TERM);
    }

    @Override
    public TargetDocumentProperties[] getImportTargets() {
        return getConfiguration().createTargetDocuments(this);
    }

    @Override
    public boolean fileUnitParticipants() {
        //always true!!!!!
//        return !type.equals(DatenExportXml.File.SCHUELER); 
        return !TranslateID.isKlasse(getUnitId());
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
        return delayed.isValid()
                && getUnitId() != null
                && getTargetDocumentIdBase() != null
                && (notEmpty(getSubjectMarkers()) || type.equals(DatenExportXml.File.AGS) || getConfiguration().permitAltSubjectNames());
    }

    private static boolean notEmpty(final Marker[] arr) {
        return arr.length > 0 && !Arrays.stream(arr)
                .allMatch(Marker::isNull);
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
        updateExistsUnit();
    }

    public boolean isCustomDocumentIdIdentifier() {
        return targetId != null;
    }

    public String getCustomDocumentIdIdentifier() {
        return targetId;
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

    public void setCustomDocumentIdIdentifier(String value) {
        value = StringUtils.trimToNull(value);
        String old = getCustomDocumentIdIdentifier();
        if (value != null && TARGET_ID_PATTERN.matcher(value).matches()) {
            targetId = value;
        } else {
            targetId = null;
        }
        try {
            vSupport.fireVetoableChange(PROP_TARGET_ID, old, targetId);
        } catch (PropertyVetoException ex) {
            this.targetId = old;
        }
    }

    @Override
    public String getUnitDisplayName() {
        if (type.equals(DatenExportXml.File.AGS)) {
            final Object p = getClientProperty(org.thespheres.betula.xmlimport.Constants.PROP_USER_UNIT_DISPLAYNAME);
            return p instanceof String ? (String) p : getSourceSubject();
        } else {
            final UnitId u = getUnitId();
            if (u != null) {
                if (getConfiguration() != null && getUnitId() != null) {
                    final NamingResolver naming = getConfiguration().getNamingResolver();
                    try {
                        return naming.resolveDisplayName(u, getTerm());
                    } catch (IllegalAuthorityException ex) {
                    }
                }
                return u.getId();
            }
            return null;
        }
    }

    @Override
    public boolean importUnitDisplayName() {
        return type.equals(DatenExportXml.File.AGS);
    }

    @Override
    public String getHtmlDisplayName() {
        final String name = getUnitDisplayName();
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
                return getSourceSubject();
            case "fachart":
                final String fa = type.equals(File.AGS) ? "AG" : makeUnique.getFachart();
                final String kn = makeUnique.getKursnr();
                final String kl = makeUnique.klassen();
                return fa + " / " + kn + " / " + kl;
            case "lehrer":
                return getSourceSigneeName();
        }
        return "";
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.makeUnique);
        hash = 41 * hash + this.clone;
        return 41 * hash + Objects.hashCode(this.type);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SiBankKursItem other = (SiBankKursItem) obj;
        if (!Objects.equals(this.makeUnique, other.makeUnique)) {
            return false;
        }
        if (this.clone != other.clone) {
            return false;
        }
        return this.type == other.type;
    }

    class GeneratedUnitId implements VetoableChangeListener {

        private boolean isInit = false;
        private UnitId uid;

        @SuppressWarnings("LeakingThisInConstructor")
        private GeneratedUnitId() {
            SiBankKursItem.this.addVetoableChangeListener(this);
        }

        private synchronized UnitId getUnitId() {
            if (!isInit) {
                if (getTerm() != null) {
                    Object jProp = getTerm().getParameter("jahr");
                    if (jProp instanceof Integer && getConfiguration() != null) {
//                        uid = configuration.initPreferredPrimaryUnitId(makeUnique.getStufe(), null, getSubjectMarker(), false, makeUnique.getKursnr(), (int) jProp);
                        final NameParser np = createNameParser();
                        uid = np.findUnitId(makeUnique.getKursnr(), getSubjectMarker(), (int) jProp, makeUnique.getStufe());
                    }
                }
//                if (klasse != null && stufe != -1) {
//                    String st = Integer.toString(stufe);
//                    String kid = klasse.replace(st, "");
//                    int rJahr = 2015;//aus untisdaten
//                    String idvalue = TranslateID.findId(stufe, rJahr, null, kid, "kgs");
//                    uid = new UnitId(configuration.getAuthority(), idvalue);
//                } else if (stufe != -1 && kursid != null) {
//                    int rJahr = 2015;//aus untisdaten
//                    String idvalue = TranslateID.findId(stufe, rJahr, getSubjectMarker(), kursid, "kgs");
//                    uid = new UnitId(configuration.getAuthority(), idvalue);
//                }
//                if (uid == null) {
//                    uid = new UnitId(configuration.getAuthority(), "?");
//                }
                isInit = true;
            }
            return uid;
        }

        private NameParser createNameParser() {
            final NamingResolver nr = getConfiguration().getNamingResolver();
            final String first = nr.properties().get("first-element");
            final String bl = nr.properties().get("base-level");
            Integer baseLevel = null;
            if (bl != null) {
                try {
                    baseLevel = Integer.parseInt(bl);
                } catch (NumberFormatException nfex) {
                }
            }
            final NameParser ret = new NameParser(getConfiguration().getAuthority(), first, baseLevel);
            final SiBankImportTarget cfg = getConfiguration();
            if (cfg instanceof ConfigurableImportTarget) {
                ret.setImportScripts(((ConfigurableImportTarget) cfg).getImportScripts());
            }
            return ret;
        }

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (PROP_UNIQUE_SUBJECT.equals(evt.getPropertyName())
                    || PROP_TARGET_ID.equals(evt.getPropertyName())) {
                isInit = false;
                targetDocBase = null;
            }
        }

    }

}
