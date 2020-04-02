/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.util;

import java.time.LocalDate;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;
import org.thespheres.betula.xmlimport.utilities.UpdaterFilter;

/**
 *
 * @author boris.heithecker
 */
public abstract class ActionImportTargetsItem extends ImportTargetsItem {

    private boolean selected;
    protected final DocumentId document;
//    private XmlImportTarget xmlimptarget;
    private ConfigurableImportTarget target;
    private final boolean fragment;

    public ActionImportTargetsItem(String sourceNode, DocumentId id, boolean fragment) {
        super(sourceNode);
        this.document = id;
        this.fragment = fragment;
    }

    @Override
    public boolean isFragment() {
        return fragment;
    }

    @Override
    public DocumentId getTargetDocumentIdBase() {
        return document;
    }

    @Override
    public abstract UnitId getUnitId();

    @Override
    public TargetDocumentProperties[] getImportTargets() {
        //Legacy case
//        if (xmlimptarget != null && isValid()) {
//            return xmlimptarget.createTargetDocuments(this);
//        }
        if (target != null && isValid()) {
            return target.createTargetDocuments(this);
        }
        return new TargetDocumentProperties[0];
    }

    @Override
    public boolean fileUnitParticipants() {
        return false;
    }

    @Override
    public String getUnitDisplayName() {
        return null;
    }

    @Override
    public boolean isUnitIdGenerated() {
        return false;
    }

    @Override
    public boolean existsUnitInSystem() {
        return false;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

//    @Deprecated
//    public void initialize(XmlImportTarget cfg) {
//        if (target != null) {
//            throw new IllegalStateException();
//        }
//        this.xmlimptarget = cfg;
//        setDeleteDate(LocalDate.now());
//    }

    public void initialize(ConfigurableImportTarget cfg) {
//        if (xmlimptarget != null) {
//            throw new IllegalStateException();
//        }
        this.target = cfg;
        setDeleteDate(LocalDate.now());
    }

    @Override
    public boolean isValid() {
        return getPreferredConvention() != null && getDeleteDate() != null;
    }

    public static class Filter implements UpdaterFilter<ActionImportTargetsItem, TargetDocumentProperties> {

        @Override
        public boolean accept(ActionImportTargetsItem iti) {
            return iti.isSelected() && iti.isValid();
        }

    }
}
