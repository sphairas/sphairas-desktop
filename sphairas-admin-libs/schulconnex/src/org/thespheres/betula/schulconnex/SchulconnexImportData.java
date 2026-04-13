/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex;

import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.uiutil.AbstractImportAction;
import org.thespheres.betula.xmlimport.uiutil.DefaultImportWizardSettings;

/**
 * Wizard settings / data holder for the Schulconnex import workflow.
 *
 * @author boris.heithecker
 */
public class SchulconnexImportData extends DefaultImportWizardSettings<SchulconnexImportConfiguration, ImportItem> {

    public SchulconnexImportConfiguration getConfiguration() {
        return (SchulconnexImportConfiguration) getProperty(AbstractImportAction.IMPORT_TARGET);
    }
}
