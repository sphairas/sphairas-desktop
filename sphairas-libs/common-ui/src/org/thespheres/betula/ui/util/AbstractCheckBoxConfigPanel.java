/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import org.openide.util.Lookup;
import org.thespheres.betula.ui.ConfigurationPanelComponent;

/**
 *
 * @author boris.heithecker
 * @param <T>
 */
public abstract class AbstractCheckBoxConfigPanel<T> extends ConfigurationPanelComponent implements ActionListener {

    protected T current;

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public AbstractCheckBoxConfigPanel(JCheckBox component) {
        super(component);
        component.setBorderPainted(true);
    }

    protected JCheckBox getComponent() {
        return (JCheckBox) viewComponent;
    }

    @Override
    public void panelDeactivated() {
        getComponent().removeActionListener(this);
        current = null;
    }

    @Override
    public void panelActivated(Lookup context) {
        onContextChange(context);
        updateValue();
    }

    protected void updateValue() {
        final Boolean cv = getCurrentValue();
        getComponent().removeActionListener(this);
        if (cv != null) {
            getComponent().setSelected(cv);
            getComponent().setEnabled(true);
        } else {
            getComponent().setSelected(false);
            getComponent().setEnabled(false);
        }
        getComponent().addActionListener(this);
    }

    protected abstract Boolean getCurrentValue();

    protected abstract void updateValue(boolean cn);

    protected abstract void onContextChange(Lookup context);

    @Override
    public void actionPerformed(ActionEvent e) {
        if (current != null) {
            final boolean selected = getComponent().isSelected();
            updateValue(selected);
        }
    }

}
