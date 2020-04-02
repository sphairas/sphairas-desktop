/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.action;

import org.openide.WizardDescriptor;
import org.thespheres.betula.curriculum.Curriculum;
import org.thespheres.betula.xmlimport.uiutil.DefaultImportWizardSettings;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
public class StundentafelImportSettings extends DefaultImportWizardSettings<ConfigurableImportTarget, StundentafelImportTargetsItem> {

    protected final Curriculum curriculum;

    protected StundentafelImportSettings(final Curriculum file) {
        this.curriculum = file;
    }

    public Curriculum getCurriculum() {
        return curriculum;
    }

    WizardDescriptor.Iterator<StundentafelImportSettings> createIterator() {
        return new ContextStundentafelImportSettings.XmlDataImportActionWizardIterator();
    }

}
