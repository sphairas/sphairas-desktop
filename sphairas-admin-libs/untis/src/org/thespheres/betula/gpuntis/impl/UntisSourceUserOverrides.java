/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import org.openide.util.Exceptions;
import org.thespheres.betula.gpuntis.ImportUntisUtil;
import org.thespheres.betula.gpuntis.ImportedLesson;
import org.thespheres.betula.gpuntis.UntisImportData;
import org.thespheres.betula.gpuntis.impl.UntisSourceUserOverrides.LessonListener;
import org.thespheres.betula.gpuntis.impl.UntisSourceTargetLinks.Schuljahr;
import org.thespheres.betula.gpuntis.ui.ImportAction;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.utilities.AbstractSourceOverrides;

/**
 *
 * @author boris.heithecker
 */
public class UntisSourceUserOverrides extends AbstractSourceOverrides<ImportedLesson, LessonListener, UntisTargetLink, String, UntisSourceTargetLinks, Schuljahr> {

    public UntisSourceUserOverrides(UntisImportData wiz) {
        super(wiz);
    }

    @Override
    protected LessonListener createListener(ImportedLesson l) {
        return new LessonListener(l);
    }

    @Override
    protected String getSourceIdentifier(ImportedLesson t) {
        return t.getLesson().getId();
    }

    @Override
    protected ImportedLesson createClone(ImportedLesson lesson0, UntisTargetLink link) {
        return ImportedLesson.create(lesson0.getLesson(), lesson0.getGeneral(), link.getClone());
    }

    @Override
    protected Schuljahr getLinks(ImportedLesson t, LessonListener ret) {
        return links != null ? links.get(ret.jahr, false) : null;
    }

    @Override
    public void setChanged(ChangeSet.SetChangeEvent<ImportedLesson> e) {
        waitForSourceTargetAccess();
        super.setChanged(e);
    }

    private void waitForSourceTargetAccess() {
        final UntisSourceTargetAccess sta = (UntisSourceTargetAccess) wizard.getProperty(ImportAction.UNTIS_SOURCE_TARGET_ACCESS);
        sta.waitLoadingFinished();
    }

//    private void addWatch(ImportedLesson l) {
//        LessonListener ret = new LessonListener(l);
//        ret.initialize();
//        listeners.add(ret);
//
//        Schuljahr sj = links.get(ret.jahr, false);
//        if (sj != null && l.id() == 0) {
//            sj.getLinks(l.getLesson().getId()).stream()
//                    .filter(utl -> utl.getClone() != 0)
//                    //                    .map(utl -> createClone(utl, l))
//                    //                    .map(LessonListener::new)
//                    //                    .peek(LessonListener::initialize)
//                    //                    .forEach(listeners::add);
//                    .forEach(utl -> createClone(utl, l));
//        }
//    }
    protected final class LessonListener extends AbstractSourceOverrides<ImportedLesson, LessonListener, UntisTargetLink, String, UntisSourceTargetLinks, Schuljahr>.AbstractItemListener {

        private final int jahr;

        private LessonListener(ImportedLesson lesson) {
            super(lesson);
            this.jahr = ImportUntisUtil.findSchuljahr(lesson.getLesson());
        }

        @Override
        protected void initialize() {
            if (links != null) {
                Schuljahr sj = links.get(jahr, true);
                targetItem.removeVetoableChangeListener(this);
                UntisTargetLink l0 = sj.getLink(targetItem.getLesson().getId(), targetItem.id());
                if (l0 != null) {
                    targetLink = l0;
                    initUserOverrides();
                } else {
                    try {
                        targetLink = sj.addLink(targetItem.getLesson().getId(), targetItem.id());
                        removeWhenUnitialize = true;
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                targetItem.addVetoableChangeListener(this);
            }
        }

        //Update ImportedLesson only if it has been initialized aka config is set
        @Override
        protected boolean doUpdate() {
            return targetItem.getUntisImportConfiguration() != null;
        }

        @Override
        protected void uninitialize() {
            targetItem.removeVetoableChangeListener(this);
            if (targetLink != null && (removeWhenUnitialize || targetLink.getClone() != 0)) {
                Schuljahr sj = links.get(jahr, false);
                if (sj != null) {
                    sj.remove(targetLink);
                }
            }
        }

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            super.vetoableChange(evt);
            switch (evt.getPropertyName()) {
                case ImportedLesson.PROP_TARGET_ID:
                    updateTargetId((String) evt.getOldValue(), (String) evt.getNewValue());
                    break;
                case ImportedLesson.PROP_IMPORT_TARGET:
                    initUserOverrides();
                    break;
            }
        }

        private void updateTargetId(String old, String newVal) {
            if (doUpdate()) {
                removeWhenUnitialize = removeWhenUnitialize
                        & !targetLink.setTargetSuffix(newVal);
            }
        }
    }
}
