/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.action;

import org.thespheres.betula.curriculumimport.xml.CurriculumAssoziationenCollection;
import org.thespheres.betula.xmlimport.utilities.AbstractSourceOverrides;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Set;
import org.openide.util.Exceptions;
import org.thespheres.betula.curriculumimport.StundentafelImportTargetsItem;
import org.thespheres.betula.curriculumimport.action.CurriculumSourceOverrides.Listener;
import org.thespheres.betula.curriculumimport.xml.CurriculumAssoziation;
import org.thespheres.betula.document.Marker;

/**
 *
 * @author boris.heithecker
 */
public class CurriculumSourceOverrides extends AbstractSourceOverrides<StundentafelImportTargetsItem, Listener, CurriculumAssoziation, StundentafelImportTargetsItem.ID, CurriculumAssoziationenCollection, CurriculumAssoziationenCollection> {

    public CurriculumSourceOverrides(final StundentafelImportSettings wiz) {
        super(wiz);
    }

    @Override
    protected Listener createListener(final StundentafelImportTargetsItem l) {
        return new Listener(l);
    }

    @Override
    protected StundentafelImportTargetsItem.ID getSourceIdentifier(final StundentafelImportTargetsItem t) {
        return t.getIdentifier();
    }

    @Override
    protected StundentafelImportTargetsItem createClone(final StundentafelImportTargetsItem item, final CurriculumAssoziation link) {
        throw new UnsupportedOperationException("Clones not supported yet.");
    }

//    @Override
//    public void setChanged(ChangeSet.SetChangeEvent<StundentafelImportTargetsItem> e) {
//        waitForSourceTargetAccess();
//        super.setChanged(e);
//    }
//
//    private void waitForSourceTargetAccess() {
//        final CurriculumSourceTargetAccess sta = (CurriculumSourceTargetAccess) wizard.getProperty(SourceTargetLinksAccess.PROP_SOURCE_TARGET_LINKS_ACCESS);
//        sta.waitLoadingFinished();
//    }
    public class Listener extends AbstractSourceOverrides<StundentafelImportTargetsItem, Listener, CurriculumAssoziation, StundentafelImportTargetsItem.ID, CurriculumAssoziationenCollection, CurriculumAssoziationenCollection>.AbstractItemListener {

        private Listener(StundentafelImportTargetsItem lesson) {
            super(lesson);
        }

        //Called not before an item is selected in 
        @Override
        protected void initialize() {
            if (links != null) {
                targetItem.removeVetoableChangeListener(this);
                final CurriculumAssoziation sba = links.getLink(targetItem.getIdentifier(), 0);
                if (sba != null) {
                    targetLink = sba;
                    initUserOverrides();
                } else {
                    try {
                        targetLink = links.addLink(targetItem.getIdentifier(), 0);
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
//                targetItem.setCustomDocumentIdIdentifier(targetLink.getTargetSuffix());
//                final ColumnProperty cp = targetLink.getNonDefaultProperty(MultipleSubjectsOverride.COLUMNID_MULTIPLESUBJECTS);
//                if (cp instanceof MultipleSubjectsOverride) {
//                    final Marker[] subjects = ((MultipleSubjectsOverride) cp).getSubjects();
//                    if (subjects != null && subjects.length != 0) {
//                        targetItem.setSubjectMarker(subjects);
//                    }
//                }
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
//            switch (evt.getPropertyName()) {
//                case SiBankKursItem.PROP_TARGET_ID:
//                    updateTargetId((String) evt.getOldValue(), (String) evt.getNewValue());
//                    break;
//                case SiBankKursItem.PROP_IMPORT_TARGET:
//                    initUserOverrides();
//                    break;
//                case SiBankKursItem.PROP_SUBJECTS:
//                    updateMultipleSubjects((Set<Marker>) evt.getOldValue(), (Set<Marker>) evt.getNewValue());
//                    break;
//            }
        }

        private void updateTargetId(final String old, final String newVal) {
            if (doUpdate()) {
                removeWhenUnitialize = removeWhenUnitialize
                        & !targetLink.setTargetSuffix(newVal);
            }
        }

        protected void updateMultipleSubjects(final Set<Marker> old, final Set<Marker> value) {
            if (doUpdate()) {
//                final Marker[] arr = value.isEmpty() ? null : value.stream().toArray(Marker[]::new);
//                targetLink.removeNonDefaultProperty(MultipleSubjectsOverride.COLUMNID_MULTIPLESUBJECTS);
//                if (arr != null) {
//                    targetLink.setNonDefaultProperty(new MultipleSubjectsOverride(arr));
//                }
            }
        }
    }
}
