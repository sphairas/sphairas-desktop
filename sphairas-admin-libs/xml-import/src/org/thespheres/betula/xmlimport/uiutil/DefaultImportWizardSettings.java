/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTarget;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <T>
 */
public class DefaultImportWizardSettings<I extends ImportTarget, T extends ImportItem> extends AbstractImportWizardSettings<I> implements ImportWizardSettings.TargetItemSettings<I, T> {

    @Override
    public ChangeSet<T> getSelectedNodesProperty() {
        return (ChangeSet<T>) getProperty(AbstractFileImportAction.SELECTED_NODES);
    }

}
