/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex.ui;

import java.util.ArrayList;
import org.openide.WizardDescriptor;
import org.thespheres.betula.schulconnex.SchulconnexImportData;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportWizard;

/**
 * Wizard iterator for the Schulconnex import workflow.
 * <p>
 * Current step flow:
 * <ol>
 * <li>Target / provider configuration</li>
 * <li>Type specific data/documents step</li>
 * <li>Student review step (only for Schueler / PRIMARY_UNIT import)</li>
 * </ol>
 *
 * @author boris.heithecker
 */
final class SchulconnexImportActionWizardIterator extends AbstractFileImportWizard<SchulconnexImportData<?>> {

    private final String type;

    SchulconnexImportActionWizardIterator(final String type) {
        this.type = type;
    }

    @Override
    protected ArrayList<WizardDescriptor.Panel<SchulconnexImportData<?>>> createPanels() {
        final ArrayList<WizardDescriptor.Panel<SchulconnexImportData<?>>> ret = new ArrayList<>();
        ret.add(new SchulconnexImportConfigVisualPanel.SchulconnexImportConfigPanel());
        if (SchulconnexImportAction.SIGNEE.equals(type)) {
            ret.add(new SchulconnexSigneeVisualPanel.SchulconnexSigneePanel());
        } else if (SchulconnexImportAction.PRIMARY_UNIT.equals(type)) {
            ret.add(new SchulconnexPrimaryUnitDocumentsVisualPanel.SchulconnexDataDocumentsPanel());
            ret.add(new SchulconnexPrimaryUnitStudentsVisualPanel.SchulconnexPrimaryUnitUpdateStudentsPanel());
        } else if (SchulconnexImportAction.TARGET_ITEM.equals(type)) {
            ret.add(new SchulconnexTargetsDocumentsVisualPanel.SchulconnexTargetsDocumentsPanel());
        }
        return ret;
    }
}
