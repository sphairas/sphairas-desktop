/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnControlButton;

/**
 *
 * @author boris.heithecker
 */
class ColumnControlButtonExt extends ColumnControlButton {

    private boolean quiet = false;

    ColumnControlButtonExt(JXTable table) {
        super(table);
    }

    @Override
    protected void populatePopup() {
        if (!quiet) {
            super.populatePopup();
        }
    }

    void quiet(boolean b) {
        if (b == this.quiet) {
            return;
        }
        this.quiet = b;
        populatePopup();
    }

}
