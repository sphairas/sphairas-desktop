/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.impl;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.assess.GradeParsingException;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.services.util.UnitInfo;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.model.XmlStudentItem;
import org.thespheres.betula.xmlimport.model.XmlTargetEntryItem;
import org.thespheres.betula.xmlimport.model.XmlTargetItem;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.ImportStudentKey;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;

/**
 *
 * @author boris.heithecker
 */
public class TargetItemsXmlCsvItem extends AbstractXmlCsvImportItem<XmlTargetItem> {

    enum StudentsOrigin {
        ENTRIES, STUDENTS, NONE
    };
    static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("d.M.y", Locale.getDefault());
    public static final String PROP_TARGET_ID = "targetId";
    private final ConfigurableImportTargetHelper<TargetItemsXmlCsvItem> helper;
    private final TableImportStudentsSet<TargetItemsXmlCsvItem> importStudents = new TableImportStudentsSet<>(this);
    private IOException importStudentsException;
    private final boolean allowNullUnit;
    private StudentsOrigin studentsSource;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public TargetItemsXmlCsvItem(String sourceNode, XmlTargetItem source, boolean allowNullUnit) {
        super(sourceNode, source, source.getSourceSubject(), source.getSourceSignee());
        this.allowNullUnit = allowNullUnit;
        this.unit = source.getUnit();
        this.helper = new ConfigurableImportTargetHelper<>(this);
    }

    public synchronized void initialize(final ConfigurableImportTarget config, final XmlCsvImportSettings wizard) {
        final ConfigurableImportTarget oldCfg = getConfiguration();
        boolean configChanged = oldCfg == null
                || !oldCfg.getProviderInfo().equals(config.getProviderInfo());

        final String ac = getSource().getAssessmentConvention();
        final AssessmentConvention conv;
        if (ac != null && (conv = GradeFactory.findConvention(ac)) != null) {
            setAssessmentConvention(conv);
        } else if (config.getAssessmentConventions().length > 0) {
            setAssessmentConvention(config.getAssessmentConventions()[0]);
        }

        final Term term = (Term) wizard.getProperty(AbstractFileImportAction.TERM);
        try {
            setClientProperty(ImportTargetsItem.PROP_SELECTED_TERM, term);
        } catch (PropertyVetoException ex) {
        }

        if (configChanged || getDeleteDate() == null) {
            final Integer level = helper.findLevel();
            if (level != null) {
                setDeleteDate(ImportUtil.calculateDeleteDate(level, 5, Month.JULY));
            }
        }

        if (configChanged || getSubjectMarker() == null) {
            if (!StringUtils.isBlank(getSourceSubject())) {
                final Marker fach = Arrays.stream(config.getSubjectMarkerConventions())
                        .flatMap(mc -> Arrays.stream(mc.getAllMarkers()))
                        .filter(m -> (m.getLongLabel().equalsIgnoreCase(getSourceSubject())))
                        .findAny()
                        .orElse(Marker.NULL);
                setSubjectMarker(fach);
            }
        }
        final String san = getSource().getSubjectAlternativeName();
        subjectAlternativeName = StringUtils.trimToNull(san);

        if (configChanged) {
            Signees.get(config.getWebServiceProvider().getInfo().getURL())
                    .flatMap(s -> s.findSignee(getSourceSigneeName()))
                    .ifPresent(this::setSignee);
        }

        if (configChanged) {
            importStudents.setConfiguration(config);
            if (!getSource().getStudents().isEmpty()) {
                this.studentsSource = StudentsOrigin.STUDENTS;
                final List<XmlStudentItem> ls = getSource().getStudents();
                setStudents(ls);
            } else if (!getSource().getEntries().isEmpty()) {
                this.studentsSource = StudentsOrigin.ENTRIES;
                final List<XmlTargetEntryItem> ls = getSource().getEntries();
                setStudents(ls);
            } else {
                this.studentsSource = StudentsOrigin.NONE;
                importStudents.clear();
                students = null;//Reset students, lazy load in getUnitStudents();
            }
        }

        final String ts = config.getTermSchemeProvider().getInfo().getURL();
        if (ts != null) {
            setPreferredTermScheduleProvider(ts);
        }

        try {
            setClientProperty(PROP_IMPORT_TARGET, config);
        } catch (PropertyVetoException ex) {
            ex.printStackTrace(ImportUtil.getIO().getErr());
        }
    }

    private void setStudents(final List<? extends XmlStudentItem> ls) {
        final List<ImportStudentKey> l = ls.stream()
                .map(ImportItemsUtil::createImportStudentKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        try {
            importStudents.set(l);
            importStudentsException = null;
        } catch (IOException ex) {
            importStudentsException = ex;
            Exceptions.printStackTrace(ex);
        }
    }

    public Marker getRealm() {
        final String[] names = getConfiguration().getRealmMarkerConventionNames();
        return getUniqueMarkerSet().getUnique(names);
    }

    @Override
    public UnitId getUnitId() {
        final UnitId u = super.getUnitId();
        if (u != null) {
            return u;
        }
        return helper.getGeneratedUnitId();
    }

    @Override
    public StudentId[] getUnitStudents() {
        final ConfigurableImportTarget cfg = getConfiguration();
        final UnitId uid = getUnitId();
        if (studentsSource.equals(StudentsOrigin.NONE)) {
            if (students == null && cfg != null && uid != null) {
                students = Units.get(cfg.getWebServiceProvider().getInfo().getURL())
                        .filter(u -> u.hasUnit(uid))
                        .map(u -> {
                            try {
                                return u.fetchParticipants(uid, null);
                            } catch (IOException ex) {
                                return null;
                            }
                        })
                        .map(UnitInfo::getStudents)
                        .orElse(new StudentId[0]);
            }
        } else {
            return importStudents.getUnitStudents();
        }
        return super.getUnitStudents();
    }

    @Override
    public void setUnitId(UnitId unit) {
        if (studentsSource.equals(StudentsOrigin.NONE)) {
            students = null; //Reset students, lazy load in getUnitStudents();
        }
        super.setUnitId(unit);
    }

    @Override
    public boolean isUnitIdGenerated() {
        return super.getUnitId() == null;
    }

    @Override
    public boolean existsUnitInSystem() {
        return helper.existsUnit();
    }

    @Override
    protected void setTargetDocumentIdBaseOption(final DocumentId did) {
        helper.setTargetDocBase(did);
    }

    @Override
    public DocumentId getTargetDocumentIdBase() {
        return helper.getTargetDocumentIdBase();
    }

    @Override
    public boolean isFragment() {
        return false;
    }

    @Override
    public TargetDocumentProperties[] getImportTargets() {
        if (getConfiguration() != null && isValid()) {
            return getConfiguration().createTargetDocuments(this);
        }
        return new TargetDocumentProperties[0];
    }

    @Override
    public boolean fileUnitParticipants() {
        return importStudentsException == null && !importStudents.isEmpty();
    }

    @Override
    public boolean importUnitDisplayName() {
        return hasUnitDisplayNameOverride();
    }

    public boolean hasUnitDisplayNameOverride() {
        final boolean hasUserDisplayName = getClientProperty(org.thespheres.betula.xmlimport.Constants.PROP_USER_UNIT_DISPLAYNAME) instanceof String;
        final UnitId unitId = getUnitId();
        final boolean isUnitResolvable = unitId != null && !unitId.getId().equals(helper.getUnitDisplayName());
        return hasUserDisplayName
                || (isUnitResolvable && !StringUtils.isBlank(getSource().getSourceUnitName()));
    }

    @Override
    public String getUnitDisplayName() {
        if (hasUnitDisplayNameOverride()) {
            final Object p = getClientProperty(org.thespheres.betula.xmlimport.Constants.PROP_USER_UNIT_DISPLAYNAME);
            if (p instanceof String) {
                return (String) p;
            }
            return StringUtils.trimToNull(getSource().getSourceUnitName());
        }
        return helper.getUnitDisplayName();
    }

    @Override
    public void setAssessmentConvention(AssessmentConvention ac) {
        super.setAssessmentConvention(ac);
        final List<XmlTargetEntryItem> ls = getSource().getEntries();
        final AssessmentConvention conv = getAssessmentConvention();
        final Term current = (Term) getClientProperty(ImportTargetsItem.PROP_SELECTED_TERM);
        if (!ls.isEmpty()) {
            for (final XmlTargetEntryItem i : ls) {
                final ImportStudentKey key = ImportItemsUtil.createImportStudentKey(i);
                if (key != null) {
                    final String sg = StringUtils.stripToNull(i.getSourceGrade());
                    Grade g = null;
                    if (sg != null && conv != null) {
                        try {
                            g = conv.parseGrade(sg);
                        } catch (GradeParsingException ex) {
                            ImportUtil.getIO().getErr().println(ex.getLocalizedMessage());
                        }
                    }
                    if (g != null) {
                        final StudentId sid = importStudents.find(key);
                        if (sid != null) {
                            if (current != null) {
                                submit(sid, current.getScheduledItemId(), g, Timestamp.now());
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    public boolean isValid() {
        return getDeleteDate() != null
                && (getUnitId() != null || allowNullUnit)
                && getTargetDocumentIdBase() != null
                && (!Marker.isNull(getSubjectMarker()) || StringUtils.isNotBlank(getSubjectAlternativeName()) || !Marker.isNull(getRealm()))
                && importStudents.isValid();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return 97 * hash + Objects.hashCode(this.getSourceNodeLabel());
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
        final TargetItemsXmlCsvItem other = (TargetItemsXmlCsvItem) obj;
        return Objects.equals(this.getSourceNodeLabel(), other.getSourceNodeLabel());
    }
}
