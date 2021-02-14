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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.services.ServiceConstants;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;
import org.thespheres.betula.tableimport.util.TableImportUtilities;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.model.XmlStudentItem;
import org.thespheres.betula.xmlimport.model.XmlUnitItem;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.ImportStudentKey;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;
import org.thespheres.betula.xmlimport.utilities.UpdaterFilter;

/**
 *
 * @author boris.heithecker
 */
public class PrimaryUnitsXmlCsvItem extends AbstractXmlCsvImportItem<XmlUnitItem> {

    static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("d.M.y", Locale.getDefault());
    public static final String PROP_CONFIGURATION = "configuration";
    public static final String PROP_TARGET_ID = "targetId";
    private final ConfigurableImportTargetHelper helper;
    private final PrimaryUnitImportStudentsSet importStudents = new PrimaryUnitImportStudentsSet(this);
    private IOException importStudentsException;
    private IllegalArgumentException unparseableMarkersException;

    public PrimaryUnitsXmlCsvItem(final String sourceNode, final XmlUnitItem source) {
        super(sourceNode, source);
        this.helper = new ConfigurableImportTargetHelper(this);
    }

    public synchronized void initialize(final ConfigurableImportTarget config, final XmlCsvImportSettings wizard) {
        final ConfigurableImportTarget oldCfg = getConfiguration();
        final boolean configChanged = oldCfg == null
                || !oldCfg.getProviderInfo().equals(config.getProviderInfo());

        try {
            final Marker[] sm = TableImportUtilities.parseSourceMarkers(getSource(), ServiceConstants.BETULA_PRIMARY_UNIT_MARKER.getConvention());
            if (sm.length != 0) {
                Arrays.stream(sm)
                        .forEach(uniqueMarkers::add);
            } else {
                uniqueMarkers.add(ServiceConstants.BETULA_PRIMARY_UNIT_MARKER);
            }
        } catch (final IllegalArgumentException ex) {
            unparseableMarkersException = ex;
            ex.printStackTrace(ImportUtil.getIO().getErr());
        }
//
        Term term = (Term) wizard.getProperty(AbstractFileImportAction.TERM);
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

        if (configChanged) {
            Signees.get(config.getWebServiceProvider().getInfo().getURL())
                    .flatMap(s -> s.findSignee(getSourceSigneeName()))
                    .ifPresent(this::setSignee);
        }

        if (configChanged) {
            importStudents.setConfiguration(config);
            if (!getSource().getStudents().isEmpty()) {
                final List<XmlStudentItem> ls = getSource().getStudents();
                setStudents(ls);
            } else {
                importStudents.clear();
                students = null;
            }
        }

        try {
            setClientProperty(PROP_IMPORT_TARGET, config);
        } catch (PropertyVetoException ex) {
            ex.printStackTrace(ImportUtil.getIO().getErr());
        }
    }

    public PrimaryUnitImportStudentsSet getImportStudents() {
        return importStudents;
    }

    private void setStudents(final List<XmlStudentItem> ls) {
        final Map<ImportStudentKey, XmlStudentItem> m = ls.stream()
                .filter(i -> !StringUtils.isBlank(i.getSourceName()))
                .collect(Collectors.groupingBy(ImportItemsUtil::createImportStudentKey, CollectionUtil.requireSingleOrNull()));
        m.entrySet().forEach(me -> {
            try {
                importStudents.put(me.getKey(), me.getValue());
                importStudentsException = null;
            } catch (IOException ex) {
                importStudentsException = ex;
                ex.printStackTrace(ImportUtil.getIO().getErr());
            }
        });

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
        return importStudents.getUnitStudents();
    }

    @Override
    public void setUnitId(final UnitId unit) {
        if (getSource().getStudents().isEmpty()) {
            students = null;
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
    public DocumentId getTargetDocumentIdBase() {
        return null;
    }

    @Override
    public boolean isFragment() {
        return false;
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
    public String getUnitDisplayName() {
        return helper.getUnitDisplayName();
    }

    @Override
    public boolean isValid() {
        return getDeleteDate() != null
                && getUnitId() != null
                && importStudents.isValid()
                && importStudentsException == null
                && unparseableMarkersException == null;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return 97 * hash + Objects.hashCode(this.getSourceNodeLabel());
    }

    @Override
    public boolean equals(Object obj
    ) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PrimaryUnitsXmlCsvItem other = (PrimaryUnitsXmlCsvItem) obj;
        return Objects.equals(this.getSourceNodeLabel(), other.getSourceNodeLabel());
    }

    public static class Filter implements UpdaterFilter<PrimaryUnitsXmlCsvItem, TargetDocumentProperties> {

        @Override
        public boolean accept(PrimaryUnitsXmlCsvItem iti) {
            return iti.isSelected() && iti.isValid();
        }

    }
}
