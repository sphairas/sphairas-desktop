/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import javax.swing.JTable;
import org.thespheres.betula.xmlimport.ImportItem;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <W>
 */
public interface CreateDocumentsComponent<I extends ImportItem, W extends ImportWizardSettings> {

    public JTable getTable();

    public W getSettings();

    default public ImportTableModel<I, W> getTableModel() {
        JTable t = getTable();
        return t != null ? (ImportTableModel<I, W>) t.getModel() : null;
    }

}
