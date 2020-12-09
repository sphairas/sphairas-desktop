/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportWizard;
import java.util.ArrayList;
import org.openide.WizardDescriptor;
import org.thespheres.betula.gpuntis.UntisImportData;
import org.thespheres.betula.gpuntis.ui.UntisCreateDocumentsVisualPanel.UntisCreateDocumentsPanel;
import org.thespheres.betula.gpuntis.ui.KursauswahlVisualPanel.KursauswahlPanel;
import org.thespheres.betula.gpuntis.ui.UntisImportConfigVisualPanel.UntisImportConfigPanel;
import org.thespheres.betula.gpuntis.ui.UntisSigneeImportVisualPanel.UntisSigneeImportPanel;
import org.thespheres.betula.gpuntis.ui.UntisSubjectMappingsVisualPanel.UntisSubjectMappingsPanel;

final class ImportActionWizardIterator extends AbstractFileImportWizard<UntisImportData> implements PropertyChangeListener {

    private final String type;
    private UntisImportData data;

    public ImportActionWizardIterator(final String type, final UntisImportData d) {
        super();
        this.type = type;
        setData(d);
    }

    private UntisImportData getData() {
        return data;
    }

    private void setData(final UntisImportData data) {
        if (this.data != null) {
            this.data.removePropertyChangeListener(this);
        }
        this.data = data;
        this.data.addPropertyChangeListener(this);
    }

    @Override
    protected ArrayList<WizardDescriptor.Panel<UntisImportData>> createPanels() {
        final ArrayList<WizardDescriptor.Panel<UntisImportData>> ret = new ArrayList<>();
        if (null != type) {
            ret.add(new UntisImportConfigPanel());
            switch (type) {
                case ImportAction.LESSON:
                    if (getData().isUploadUntisDocument()) {
                        ret.add(new UntisSubjectMappingsPanel());
                    }
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

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (UntisImportData.PROP_UPLOAD_UNTIS_XML.equals(evt.getPropertyName())) {
            if (index == 0) {
                panels = null;
            }
            this.cSupport.fireChange();
        }
    }

}
