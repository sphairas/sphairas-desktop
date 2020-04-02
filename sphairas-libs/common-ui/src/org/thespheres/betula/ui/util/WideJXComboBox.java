/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.awt.Dimension;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import org.jdesktop.swingx.JXComboBox;

/**
 *
 * @author boris.heithecker
 */
public class WideJXComboBox extends JXComboBox {

    private boolean layingOut = false;

    public WideJXComboBox() {
    }

    public WideJXComboBox(ComboBoxModel model) {
        super(model);
    }

    public WideJXComboBox(Object[] items) {
        super(items);
    }

    public WideJXComboBox(Vector<?> items) {
        super(items);
    }

    @Override
    public void doLayout() {
        try {
            layingOut = true;
            super.doLayout();
        } finally {
            layingOut = false;
        }
    }

    @Override
    public Dimension getSize() {
        Dimension dim = super.getSize();
        if (!layingOut) {
            dim.width = Math.max(dim.width, getPreferredSize().width);
        }
        return dim;
    }

}
