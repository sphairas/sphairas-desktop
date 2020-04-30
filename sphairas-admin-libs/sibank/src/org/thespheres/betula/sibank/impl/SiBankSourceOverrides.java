/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.impl;

import org.thespheres.betula.xmlimport.utilities.AbstractSourceOverrides;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Set;
import org.openide.util.Exceptions;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.sibank.SiBankAssoziation;
import org.thespheres.betula.sibank.SiBankAssoziationenCollection;
import org.thespheres.betula.sibank.SiBankImportData;
import org.thespheres.betula.sibank.SiBankKursItem;
import org.thespheres.betula.sibank.UniqueSatzDistinguisher;
import org.thespheres.betula.sibank.impl.SiBankSourceOverrides.Listener;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.utilities.ColumnProperty;
import org.thespheres.betula.xmlimport.utilities.SourceTargetLinksAccess;

/**
 *
 * @author boris.heithecker
 */
//TODO extract superclass
public class SiBankSourceOverrides extends AbstractSourceOverrides<SiBankKursItem, Listener, SiBankAssoziation, UniqueSatzDistinguisher, SiBankAssoziationenCollection, SiBankAssoziationenCollection> {

    public SiBankSourceOverrides(SiBankImportData<SiBankKursItem> wiz) {
        super(wiz);
    }

    @Override
    protected Listener createListener(SiBankKursItem l) {
        return new Listener(l);
    }

    @Override
    protected UniqueSatzDistinguisher getSourceIdentifier(SiBankKursItem t) {
        return t.getDistinguisher();
    }

    @Override
    protected SiBankKursItem createClone(SiBankKursItem lesson0, SiBankAssoziation link) {
        final SiBankImportData<SiBankKursItem> wiz = (SiBankImportData<SiBankKursItem>) wizard;
        final SiBankKursItem ret = new SiBankKursItem(lesson0.getDistinguisher(), lesson0.getImportFile(), wiz, link.getClone());
        try {
            ret.initializeFrom(lesson0, (SiBankImportData) wizard);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        return ret;
    }

    @Override
    public void setChanged(ChangeSet.SetChangeEvent<SiBankKursItem> e) {
        waitForSourceTargetAccess();
        super.setChanged(e);
    }

    private void waitForSourceTargetAccess() {
        final SiBankSourceTargetAccess sta = (SiBankSourceTargetAccess) wizard.getProperty(SourceTargetLinksAccess.PROP_SOURCE_TARGET_LINKS_ACCESS);
        sta.waitLoadingFinished();
    }

    public class Listener extends AbstractSourceOverrides<SiBankKursItem, Listener, SiBankAssoziation, UniqueSatzDistinguisher, SiBankAssoziationenCollection, SiBankAssoziationenCollection>.AbstractItemListener {

        private Listener(SiBankKursItem lesson) {
            super(lesson);
        }

        //Called not before an item is selected in 
        @Override
        protected void initialize() {
            if (links != null) {
                targetItem.removeVetoableChangeListener(this);
                final SiBankAssoziation sba = links.getLink(targetItem.getDistinguisher(), targetItem.id());
                if (sba != null) {
                    targetLink = sba;
                    initUserOverrides();
                } else {
                    try {
                        targetLink = links.addLink(targetItem.getDistinguisher(), targetItem.id());
                        removeWhenUnitialize = true;
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                targetItem.addVetoableChangeListener(this);
            }
        }

        @Override
        protected void uninitialize() {
            targetItem.removeVetoableChangeListener(this);
            if (targetLink != null && (removeWhenUnitialize || targetLink.getClone() != 0)) {
                links.remove(targetLink);
            }
        }

        @Override
        protected void initUserOverrides() {
            super.initUserOverrides();
            if (doUpdate()) {
                targetItem.setCustomDocumentIdIdentifier(targetLink.getTargetSuffix());
                final ColumnProperty cp = targetLink.getNonDefaultProperty(MultipleSubjectsOverride.COLUMNID_MULTIPLESUBJECTS);
                if (cp instanceof MultipleSubjectsOverride) {
                    final Marker[] subjects = ((MultipleSubjectsOverride) cp).getSubjects();
                    if (subjects != null && subjects.length != 0) {
                        targetItem.setSubjectMarker(subjects);
                    }
                }
            }
        }

        //Update ImportedLesson only if it has been initialized aka config is set
        @Override
        protected boolean doUpdate() {
            return targetItem.getConfiguration() != null;
        }

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            super.vetoableChange(evt);
            switch (evt.getPropertyName()) {
                case SiBankKursItem.PROP_TARGET_ID:
                    updateTargetId((String) evt.getOldValue(), (String) evt.getNewValue());
                    break;
                case SiBankKursItem.PROP_IMPORT_TARGET:
                    initUserOverrides();
                    break;
                case SiBankKursItem.PROP_SUBJECTS:
                    updateMultipleSubjects((Set<Marker>) evt.getOldValue(), (Set<Marker>) evt.getNewValue());
                    break;
            }
        }

        private void updateTargetId(final String old, final String newVal) {
            if (doUpdate()) {
                removeWhenUnitialize = removeWhenUnitialize
                        & !targetLink.setTargetSuffix(newVal);
            }
        }

        protected void updateMultipleSubjects(final Set<Marker> old, final Set<Marker> value) {
            if (doUpdate()) {
                final Marker[] arr = value.isEmpty() ? null : value.stream().toArray(Marker[]::new);
                targetLink.removeNonDefaultProperty(MultipleSubjectsOverride.COLUMNID_MULTIPLESUBJECTS);
                if (arr != null) {
                    targetLink.setNonDefaultProperty(new MultipleSubjectsOverride(arr));
                }
            }
        }
    }
}
