/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.thespheres.betula.services.scheme.spi.ClassSchedule;
import org.thespheres.betula.services.scheme.spi.Period;

/**
 *
 * @author boris.heithecker
 */
class InsertRecordVisualPanel extends javax.swing.JPanel {

    private final DefaultComboBoxModel model = new DefaultComboBoxModel();
    private final StringValue periodStringValue = v -> v instanceof Period ? ((Period) v).getDisplayName() : "---";

    InsertRecordVisualPanel() {
        model.addElement(null);
        initComponents();
        periodComboBox.setRenderer(new DefaultListRenderer(periodStringValue));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dateLabel = new javax.swing.JLabel();
        dateTextField = new org.jdesktop.swingx.JXFormattedTextField();
        timeLabel = new javax.swing.JLabel();
        timeTextField = new org.jdesktop.swingx.JXFormattedTextField();
        periodLabel = new javax.swing.JLabel();
        periodComboBox = new org.jdesktop.swingx.JXComboBox();

        dateLabel.setLabelFor(dateTextField);
        org.openide.awt.Mnemonics.setLocalizedText(dateLabel, org.openide.util.NbBundle.getMessage(InsertRecordVisualPanel.class, "InsertRecordVisualPanel.dateLabel.text")); // NOI18N

        dateTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("d.M.yyyy"))));

        timeLabel.setLabelFor(timeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(timeLabel, org.openide.util.NbBundle.getMessage(InsertRecordVisualPanel.class, "InsertRecordVisualPanel.timeLabel.text")); // NOI18N

        timeTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("h:mm"))));

        periodLabel.setLabelFor(periodComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(periodLabel, org.openide.util.NbBundle.getMessage(InsertRecordVisualPanel.class, "InsertRecordVisualPanel.periodLabel.text")); // NOI18N

        periodComboBox.setModel(model);
        periodComboBox.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(dateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(periodLabel)
                                .addGap(18, 18, 18)
                                .addComponent(periodComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(timeLabel)
                                .addGap(31, 31, 31)
                                .addComponent(timeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dateLabel)
                    .addComponent(dateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeLabel)
                    .addComponent(timeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(periodLabel)
                    .addComponent(periodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel dateLabel;
    private org.jdesktop.swingx.JXFormattedTextField dateTextField;
    private org.jdesktop.swingx.JXComboBox periodComboBox;
    private javax.swing.JLabel periodLabel;
    private javax.swing.JLabel timeLabel;
    private org.jdesktop.swingx.JXFormattedTextField timeTextField;
    // End of variables declaration//GEN-END:variables

    static class InsertRecordPanel implements WizardDescriptor.Panel<WizardDescriptor>, ActionListener {

        static final String CLASS_SCHEDULE = "class-schedule";
        static final String DATE_TIME = "date-time";
        private InsertRecordVisualPanel cmp;
        private final ChangeSupport cSupport = new ChangeSupport(this);
        private ClassSchedule cs;

        @Override
        public InsertRecordVisualPanel getComponent() {
            if (cmp == null) {
                cmp = new InsertRecordVisualPanel();
            }
            return cmp;
        }

        @Override
        public boolean isValid() {
            final Date d = (Date) getComponent().dateTextField.getValue();
            final Date t = (Date) getComponent().timeTextField.getValue();
            return d != null && (t != null || getComponent().periodComboBox.getSelectedItem() != null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final Date d = (Date) getComponent().dateTextField.getValue();
            getComponent().model.removeAllElements();
            getComponent().model.addElement(null);
            if (d != null && cs != null) {
                final LocalDateTime ldt = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
                final LocalDateTime nd = ldt.plusDays(1l);
                final List<Period> pp = cs.inflate(ldt, nd, -1);
                pp.forEach(getComponent().model::addElement);
//                getComponent().setEnabled(true);
            } else {
//                getComponent().setEnabled(false);
            }
            cSupport.fireChange();
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            final LocalDateTime date = (LocalDateTime) settings.getProperty(DATE_TIME);
            final Date d = Date.from(date.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            getComponent().dateTextField.setValue(d);
            final Date t = Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
            getComponent().timeTextField.setValue(t);
            cs = (ClassSchedule) settings.getProperty(CLASS_SCHEDULE);
            getComponent().dateTextField.addActionListener(this);
            getComponent().timeTextField.addActionListener(this);
            if (cs != null) {
                getComponent().periodComboBox.setEnabled(true);
                getComponent().periodComboBox.addActionListener(this);
            } else {
                getComponent().periodComboBox.setEnabled(false);
            }
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            getComponent().dateTextField.removeActionListener(this);
            getComponent().timeTextField.removeActionListener(this);
            getComponent().periodComboBox.removeActionListener(this);
            final Date d = (Date) getComponent().dateTextField.getValue();
            final Date t = (Date) getComponent().timeTextField.getValue();
            final Period p = (Period) getComponent().periodComboBox.getSelectedItem();
            LocalDateTime ldt = null;
            if (p != null) {
                ldt = p.resolveStart();
            } else if (d != null && t != null) {
                final LocalDate ld = LocalDate.from(d.toInstant().atZone(ZoneId.systemDefault()));
                final LocalTime lt = LocalTime.from(t.toInstant().atZone(ZoneId.systemDefault()));
                ldt = LocalDateTime.of(ld, lt);
            }
            settings.putProperty(DATE_TIME, ldt);
        }

        @Override
        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            cSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cSupport.removeChangeListener(l);
        }

    }
}
