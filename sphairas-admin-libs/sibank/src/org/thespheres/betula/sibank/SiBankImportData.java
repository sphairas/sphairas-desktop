/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank;

import java.time.format.DateTimeFormatter;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.uiutil.DefaultImportWizardSettings;

/**
 *
 * @author boris.heithecker
 * @param <T>
 */
public class SiBankImportData<T extends ImportItem> extends DefaultImportWizardSettings<SiBankImportTarget, T> {

    public static final DateTimeFormatter SIBANK_DATUM = DateTimeFormatter.ofPattern("dd.MM.yyyy");
}
