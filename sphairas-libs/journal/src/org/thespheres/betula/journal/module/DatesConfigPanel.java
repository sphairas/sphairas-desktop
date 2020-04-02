package org.thespheres.betula.journal.module;

import org.thespheres.betula.journal.JournalConfiguration;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.openide.util.NbBundle;

/**
 *
 * @author boris.heithecker
 */
class DatesConfigPanel extends javax.swing.JPanel {

    private final DateFormat[] datePickerFormats;

    DatesConfigPanel() {
        String pattern = NbBundle.getMessage(JournalConfiguration.class, "JournalFileConfiguration.datePickerFormat");
        datePickerFormats = new DateFormat[]{new SimpleDateFormat(pattern)};
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        startDatePicker = new org.jdesktop.swingx.JXDatePicker();
        endDatePicker = new org.jdesktop.swingx.JXDatePicker();
        startDateLabel = new javax.swing.JLabel();
        endDateLabel = new javax.swing.JLabel();

        startDatePicker.setFormats(datePickerFormats);

        endDatePicker.setFormats(datePickerFormats);

        startDateLabel.setLabelFor(startDatePicker);
        org.openide.awt.Mnemonics.setLocalizedText(startDateLabel, org.openide.util.NbBundle.getMessage(DatesConfigPanel.class, "DatesConfigPanel.startDateLabel.text")); // NOI18N

        endDateLabel.setLabelFor(endDatePicker);
        org.openide.awt.Mnemonics.setLocalizedText(endDateLabel, org.openide.util.NbBundle.getMessage(DatesConfigPanel.class, "DatesConfigPanel.endDateLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(endDateLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(startDateLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(startDatePicker, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                    .addComponent(endDatePicker, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startDateLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(endDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(endDateLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel endDateLabel;
    org.jdesktop.swingx.JXDatePicker endDatePicker;
    private javax.swing.JLabel startDateLabel;
    org.jdesktop.swingx.JXDatePicker startDatePicker;
    // End of variables declaration//GEN-END:variables
}
