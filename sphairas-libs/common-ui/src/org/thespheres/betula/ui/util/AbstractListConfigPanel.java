/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.ui.ConfigurationPanelComponent;

/**
 *
 * @author boris.heithecker
 * @param <T>
 * @param <S>
 */
public abstract class AbstractListConfigPanel<T, S> extends ConfigurationPanelComponent implements ActionListener {

    protected final DefaultComboBoxModel<S> model = new DefaultComboBoxModel<>();
    protected T current;
    protected final JXComboBox comboBox;

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public AbstractListConfigPanel(JXComboBox component) {
        super(component);
        comboBox = component;
        model.addElement(null);
        component.setModel(model);
    }

    @NbBundle.Messages({"AbstractConventionConfigPanel.createAssesmentConventionComboBox.name=Notensystem"})
    public static <S> JXComboBox createAssesmentConventionComboBox() {
        final JXComboBox cb = new JXComboBox();
        final DefaultListRenderer r = new DefaultListRenderer((Object v) -> v instanceof AssessmentConvention ? ((AssessmentConvention) v).getDisplayName() : " ");
        cb.setRenderer(r);
        final String n = NbBundle.getMessage(AbstractListConfigPanel.class, "AbstractConventionConfigPanel.createAssesmentConventionComboBox.name");
        cb.setName(n);
        return cb;
    }

    @Override
    public void panelDeactivated() {
        comboBox.removeActionListener(this);
        current = null;
        model.setSelectedItem(null);
    }

    @Override
    public void panelActivated(Lookup context) {
        onContextChange(context);
        final S ac = getCurrentValue();
        model.setSelectedItem(ac);
        comboBox.addActionListener(this);
    }

    protected abstract S getCurrentValue();

    protected abstract void updateValue(S cn);

    protected abstract void onContextChange(Lookup context);

    @Override
    public void actionPerformed(ActionEvent e) {
        if (current != null) {
            final Object item = model.getSelectedItem();
            updateValue((S) item);
        }
    }

}
