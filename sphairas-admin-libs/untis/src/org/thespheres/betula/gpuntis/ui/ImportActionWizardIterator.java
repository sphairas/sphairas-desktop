/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.ui;

import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportWizard;
import java.util.ArrayList;
import org.openide.WizardDescriptor;
import org.thespheres.betula.gpuntis.UntisImportData;
import org.thespheres.betula.gpuntis.ui.UntisCreateDocumentsVisualPanel.UntisCreateDocumentsPanel;
import org.thespheres.betula.gpuntis.ui.KursauswahlVisualPanel.KursauswahlPanel;
import org.thespheres.betula.gpuntis.ui.UntisImportConfigVisualPanel.UntisImportConfigPanel;
import org.thespheres.betula.gpuntis.ui.UntisSigneeImportVisualPanel.UntisSigneeImportPanel;

final class ImportActionWizardIterator extends AbstractFileImportWizard<UntisImportData> {

    private final String type;

    public ImportActionWizardIterator(String type) {
        super();
        this.type = type;
    }

    @Override
    protected ArrayList<WizardDescriptor.Panel<UntisImportData>> createPanels() {
        ArrayList<WizardDescriptor.Panel<UntisImportData>> ret = new ArrayList<>();
        if (null != type) {
            ret.add(new UntisImportConfigPanel());
            switch (type) {
                case ImportAction.LESSON:
                    ret.add(new KursauswahlPanel());
                    ret.add(new UntisCreateDocumentsPanel());
                    break;
                case ImportAction.SIGNEE:
                    ret.add(new UntisSigneeImportPanel());
                    break;
            }
        }
        return ret;
    }

}
