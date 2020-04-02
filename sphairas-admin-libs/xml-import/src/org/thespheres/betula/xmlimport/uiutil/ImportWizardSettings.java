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
 */
public interface ImportWizardSettings<I extends ImportTarget> {

    public I getImportTargetProperty();

    public interface TargetItemSettings<I extends ImportTarget, T extends ImportItem> extends ImportWizardSettings<I> {

        public ChangeSet<T> getSelectedNodesProperty();
    }
}
