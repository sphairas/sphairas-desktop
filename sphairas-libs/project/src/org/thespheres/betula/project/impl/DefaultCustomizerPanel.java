/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import javax.swing.DefaultComboBoxModel;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.services.CommonTargetProperties;

/**
 *
 * @author boris.heithecker
 */
class DefaultCustomizerPanel extends javax.swing.JPanel implements PropertyChangeListener {

    private final DefaultCustomizerPanelModel model;
    private final DefaultComboBoxModel<AssessmentConvention> conventionModel = new DefaultComboBoxModel<>();
    private final StringValue contextStringValue = v -> v instanceof AssessmentConvention ? ((AssessmentConvention) v).getDisplayName() : " ";
    private final DefaultListRenderer r = new DefaultListRenderer(contextStringValue);

    @SuppressWarnings({"LeakingThisInConstructor"})
    DefaultCustomizerPanel(ProjectCustomizer.Category category, Lookup context) {
        model = new DefaultCustomizerPanelModel(context);
        conventionModel.addElement(null);
        CommonTargetProperties ctp = context.lookup(CommonTargetProperties.class);
        if (ctp != null) {
            Arrays.stream(ctp.getAssessmentConventions())
                    .forEach(conventionModel::addElement);
        }
        conventionModel.setSelectedItem(model.getAssessmentConvention());
        initComponents();
        preferredConventionBox.setRenderer(r);
        useDisplayNameCheckBox.setSelected(model.getIsShowDisplayNames());
        updateUnit();
        updateTarget();
        model.addPropertyChangeListener(this);
        category.setStoreListener(model);
    }

    private void updateUnit() {
        unitTextField.setText(model.getUnitDisplay());
    }

    private void updateTarget() {
        targetTextField.setText(model.getTargetDisplay());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (DefaultCustomizerPanelModel.SHOW_DISPLAY_NAMES.equals(evt.getPropertyName())) {
            updateUnit();
            updateTarget();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        unitTextFieldLabel = new javax.swing.JLabel();
        targetTextFieldLabel = new javax.swing.JLabel();
        unitTextField = new javax.swing.JTextField();
        targetTextField = new javax.swing.JTextField();
        preferredConventionBoxLabel = new javax.swing.JLabel();
        preferredConventionBox = new org.jdesktop.swingx.JXComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        useDisplayNameCheckBox = new javax.swing.JCheckBox();

        unitTextFieldLabel.setLabelFor(unitTextField);
        org.openide.awt.Mnemonics.setLocalizedText(unitTextFieldLabel, org.openide.util.NbBundle.getMessage(DefaultCustomizerPanel.class, "DefaultCustomizerPanel.unitTextFieldLabel.text")); // NOI18N

        targetTextFieldLabel.setLabelFor(targetTextField);
        org.openide.awt.Mnemonics.setLocalizedText(targetTextFieldLabel, org.openide.util.NbBundle.getMessage(DefaultCustomizerPanel.class, "DefaultCustomizerPanel.targetTextFieldLabel.text")); // NOI18N

        unitTextField.setEditable(false);
        unitTextField.setPreferredSize(new java.awt.Dimension(270, 25));

        targetTextField.setEditable(false);
        targetTextField.setPreferredSize(new java.awt.Dimension(270, 25));

        org.openide.awt.Mnemonics.setLocalizedText(preferredConventionBoxLabel, org.openide.util.NbBundle.getMessage(DefaultCustomizerPanel.class, "DefaultCustomizerPanel.preferredConventionBoxLabel.text")); // NOI18N

        preferredConventionBox.setModel(conventionModel);
        preferredConventionBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preferredTargetSelected(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(useDisplayNameCheckBox, org.openide.util.NbBundle.getMessage(DefaultCustomizerPanel.class, "DefaultCustomizerPanel.useDisplayNameCheckBox.text")); // NOI18N
        useDisplayNameCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setDisplayNameActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(preferredConventionBoxLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(preferredConventionBox, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(unitTextFieldLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                            .addComponent(targetTextFieldLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(unitTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(targetTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(useDisplayNameCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(unitTextFieldLabel)
                    .addComponent(unitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(targetTextFieldLabel)
                    .addComponent(targetTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(useDisplayNameCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(preferredConventionBoxLabel)
                    .addComponent(preferredConventionBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(133, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void setDisplayNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setDisplayNameActionPerformed
        model.setShowDisplayNames(useDisplayNameCheckBox.isSelected());
    }//GEN-LAST:event_setDisplayNameActionPerformed

    private void preferredTargetSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferredTargetSelected
        model.setSelectedConvention((AssessmentConvention) preferredConventionBox.getSelectedItem());
    }//GEN-LAST:event_preferredTargetSelected


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private org.jdesktop.swingx.JXComboBox preferredConventionBox;
    private javax.swing.JLabel preferredConventionBoxLabel;
    private javax.swing.JTextField targetTextField;
    private javax.swing.JLabel targetTextFieldLabel;
    private javax.swing.JTextField unitTextField;
    private javax.swing.JLabel unitTextFieldLabel;
    private javax.swing.JCheckBox useDisplayNameCheckBox;
    // End of variables declaration//GEN-END:variables

}
