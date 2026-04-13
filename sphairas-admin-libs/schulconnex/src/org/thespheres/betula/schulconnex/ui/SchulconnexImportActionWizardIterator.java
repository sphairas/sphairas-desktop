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
 * Currently contains only the target-selection / configuration panel.
 * Once the Schulconnex API fetch is implemented, add a Lehrende-review panel
 * (analogous to {@code UntisSigneeImportVisualPanel}) here.
 *
 * @author boris.heithecker
 */
final class SchulconnexImportActionWizardIterator extends AbstractFileImportWizard<SchulconnexImportData> {

    private final String type;

    SchulconnexImportActionWizardIterator(final String type) {
        this.type = type;
    }

    @Override
    protected ArrayList<WizardDescriptor.Panel<SchulconnexImportData>> createPanels() {
        final ArrayList<WizardDescriptor.Panel<SchulconnexImportData>> ret = new ArrayList<>();
        ret.add(new SchulconnexImportConfigVisualPanel.SchulconnexImportConfigPanel());
//        ret.add(new SchulconnexImportTypePlaceholderVisualPanel.SchulconnexImportTypePlaceholderPanel(type));
        return ret;
    }
}
