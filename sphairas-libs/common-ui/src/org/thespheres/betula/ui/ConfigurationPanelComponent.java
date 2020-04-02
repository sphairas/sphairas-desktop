/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEditSupport;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.painter.MattePainter;
import org.openide.util.Lookup;

/**
 *
 * @author boris.heithecker
 */
public abstract class ConfigurationPanelComponent extends JXTitledPanel {

    protected final JComponent viewComponent;
    protected final UndoableEditSupport undoSupport = new UndoableEditSupport(this);

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public ConfigurationPanelComponent(JComponent component) {
        this.viewComponent = component;
        getContentContainer().setLayout(new java.awt.BorderLayout());
        MattePainter p = new MattePainter(UIManager.getColor("Label.background"));
        setTitlePainter(p);
        component.setBorder(BorderFactory.createLineBorder(getTitleForeground(), 2));
        getContentContainer().add(component, java.awt.BorderLayout.CENTER);
        setTitle(component.getName());
    }

    public void panelActivated(Lookup context) {
    }

    public void panelDeactivated() {
    }

    public synchronized void addUndoableEditListener(UndoableEditListener l) {
        undoSupport.addUndoableEditListener(l);
    }

    public synchronized void removeUndoableEditListener(UndoableEditListener l) {
        undoSupport.removeUndoableEditListener(l);
    }
}
