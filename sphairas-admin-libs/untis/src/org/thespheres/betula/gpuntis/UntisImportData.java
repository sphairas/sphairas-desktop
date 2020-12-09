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

    public static final String PROP_UPLOAD_UNTIS_XML = "upload.untis.xml";
    private boolean uploadDocument;

    public UntisImportConfiguration getConfiguration() {
        return (UntisImportConfiguration) getProperty(AbstractFileImportAction.IMPORT_TARGET);
    }

    public Document getUntisDocument() {
        return (Document) getProperty(AbstractFileImportAction.DATA);
    }

    public void setUploadUntisDocument(final boolean upload) {
        final boolean old = isUploadUntisDocument();
        uploadDocument = upload;
        pSupport.firePropertyChange(PROP_UPLOAD_UNTIS_XML, old, isUploadUntisDocument());
    }

    public boolean isUploadUntisDocument() {
        return uploadDocument;
    }
}
