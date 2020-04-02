/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis;

import org.thespheres.betula.gpuntis.xml.Document;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.DefaultImportWizardSettings;

/**
 *
 * @author boris.heithecker
 */
public class UntisImportData extends DefaultImportWizardSettings<UntisImportConfiguration, ImportedLesson> {

    private boolean uploadDocument;

    public UntisImportConfiguration getConfiguration() {
        return (UntisImportConfiguration) getProperty(AbstractFileImportAction.IMPORT_TARGET);
    }

    public Document getUntisDocument() {
        return (Document) getProperty(AbstractFileImportAction.DATA);
    }

    public void setUploadUntisDocument(boolean upload) {
        uploadDocument = upload;
    }

    public boolean isUploadUntisDocument() {
       return uploadDocument;
    }
}
