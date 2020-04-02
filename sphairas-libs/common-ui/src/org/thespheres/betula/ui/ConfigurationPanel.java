/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui;

import javax.swing.JComponent;
import org.openide.util.Lookup;

/**
 *
 * @author boris.heithecker
 */
public interface ConfigurationPanel {

    public String getDisplayName();

    public String getDisplayHint();

    default public int position() {
        return Integer.MAX_VALUE;
    }

    public JComponent getComponent();

    public void panelActivated(Lookup context);

    public void panelDeactivated();

    /**
     * Lookup to integrated into panel 
     * 
     * @return Lookup instance or null
     */
    public Lookup getLookup();

}
