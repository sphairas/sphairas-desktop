/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.impl;

import java.util.List;
import javax.swing.DefaultComboBoxModel;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.ui.util.WideJXComboBox;
import org.thespheres.betula.xmlimport.utilities.ImportStudentKey;

/**
 *
 * @author boris.heithecker
 */
@Deprecated //May be removed
class SyncNames2AssociationsDialog extends javax.swing.JPanel implements StringValue {

    final DefaultComboBoxModel<StudentId> model = new DefaultComboBoxModel<>();
    private final ImportStudentKey key;

    SyncNames2AssociationsDialog(final ImportStudentKey key, final List<VCardStudent> students) {
        this.key = key;
        model.addElement(null);
        students.stream()
                .map(VCardStudent::getStudentId)
                .forEach(model::addElement);
        initComponents();
    }

    private String createMessage() {
        return NbBundle.getMessage(SyncNames2Associations.class, "SyncNames2Associations.message", key.getSourceName(), key.getSourceDateOfBirth());
    }

    @Override
    public String getString(Object value) {
        if (value instanceof StudentId) {
            final StudentId sid = (StudentId) value;
            return Long.toString(sid.getId()) + " [" + sid.getAuthority() + "]";
        }
        return "---";
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selectComboBox = new WideJXComboBox();
        saveCheckBox = new javax.swing.JCheckBox();
        messageLabel = new org.jdesktop.swingx.JXLabel();

        selectComboBox.setModel(model);
        selectComboBox.setRenderer(new DefaultListRenderer(this));

        org.openide.awt.Mnemonics.setLocalizedText(saveCheckBox, org.openide.util.NbBundle.getMessage(SyncNames2AssociationsDialog.class, "SyncNames2AssociationsDialog.saveCheckBox.text")); // NOI18N
        saveCheckBox.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, createMessage());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(saveCheckBox)
                            .addComponent(messageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 289, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(messageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(selectComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(saveCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXLabel messageLabel;
    javax.swing.JCheckBox saveCheckBox;
    private javax.swing.JComboBox<StudentId> selectComboBox;
    // End of variables declaration//GEN-END:variables
}
