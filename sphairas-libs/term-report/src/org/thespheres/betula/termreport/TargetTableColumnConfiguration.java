/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.ui.GradeComboBoxModel;

/**
 *
 * @author boris.heithecker
 */
class TargetTableColumnConfiguration extends TableColumnConfiguration {

    private final GradeComboBoxModel comboBoxModel = new GradeComboBoxModel(new String[0], true);
    private JXComboBox assesComboBox = new JXComboBox(comboBoxModel);

    TargetTableColumnConfiguration(TargetAssessmentProvider tap) {
        super(tap);
        comboBoxModel.setUseLongLabel(false);
        comboBoxModel.setNullValueLabel("---");
        assesComboBox = new JXComboBox(comboBoxModel);
        assesComboBox.setEditable(false);
        assesComboBox.setRenderer(new DefaultListRenderer(comboBoxModel));
        comboBoxModel.initialize(assesComboBox);
    }

    @Override
    public void configureTableColumn(TableModel m, TableColumnExt columnExt) {
        super.configureTableColumn(m, columnExt);
        class ConventionListener implements PropertyChangeListener {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (TargetAssessmentProvider.PROP_EDITABLE.equals(evt.getPropertyName())
                        || TargetAssessment.PROP_PREFERRED_CONVENTION.equals(evt.getPropertyName())
                        || AssessmentProvider.PROP_STATUS.equals(evt.getPropertyName())) {
                    updateColumn();
                }
            }

            private void updateColumn() {
                if (provider.getInitialization().satisfies(AssessmentProvider.READY)) {
                    final String cnv = ((TargetAssessmentProvider) provider).getPreferredConvention();
                    if (provider.isEditable() && cnv != null && GradeFactory.findConvention(cnv) != null) {
                        comboBoxModel.setConventions(new String[]{cnv});
                    } else {
                        comboBoxModel.setConventions(new String[0]);
                    }
                }
            }
        }
        ConventionListener l = new ConventionListener();
        l.updateColumn();
        columnExt.setCellEditor(new DefaultCellEditor(assesComboBox));
        columnExt.setCellRenderer(new DefaultTableRenderer(comboBoxModel));
        provider.addPropertyChangeListener(l);
    }

    @Override
    public String getString(final Object value) {
        if(value instanceof Grade) {
            final Grade g = (Grade) value;
            final Object p = provider.getProperty("PROP_GRADE_USE_LONG_LABEL");
            boolean useLongLabel = false;
            if(p != null && p instanceof Boolean) {
                useLongLabel = (boolean) p;
            }
            return useLongLabel ? g.getLongLabel() : g.getShortLabel();
        }
        return super.getString(value);
    }
    
    
}
