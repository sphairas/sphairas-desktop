/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang3.StringUtils;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

@Messages("SetZeugniskonfDateVisualPanel.step=Zeugniskonferenz")
public final class SetZeugniskonfDateVisualPanel extends JPanel {

    public SetZeugniskonfDateVisualPanel() {
        initComponents();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SetZeugniskonfDateVisualPanel.class, "SetZeugniskonfDateVisualPanel.step");
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        timeLabel = new javax.swing.JLabel();
        summaryLabel = new javax.swing.JLabel();
        datePicker = new org.jdesktop.swingx.JXDatePicker();
        summaryTextField = new javax.swing.JTextField();
        dateLabel = new javax.swing.JLabel();
        durationSpinner = new javax.swing.JSpinner();
        durationLabel = new javax.swing.JLabel();
        timeSpinner = new javax.swing.JSpinner();
        locationLabel = new javax.swing.JLabel();
        locationTextField = new javax.swing.JTextField();

        timeLabel.setLabelFor(timeSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(timeLabel, org.openide.util.NbBundle.getMessage(SetZeugniskonfDateVisualPanel.class, "SetZeugniskonfDateVisualPanel.timeLabel.text")); // NOI18N

        summaryLabel.setLabelFor(summaryTextField);
        org.openide.awt.Mnemonics.setLocalizedText(summaryLabel, org.openide.util.NbBundle.getMessage(SetZeugniskonfDateVisualPanel.class, "SetZeugniskonfDateVisualPanel.summaryLabel.text")); // NOI18N

        datePicker.setDate(new Date());

        org.openide.awt.Mnemonics.setLocalizedText(dateLabel, org.openide.util.NbBundle.getMessage(SetZeugniskonfDateVisualPanel.class, "SetZeugniskonfDateVisualPanel.dateLabel.text")); // NOI18N

        durationSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(-2400000L), null, null, java.util.Calendar.MINUTE));
        durationSpinner.setEditor(new javax.swing.JSpinner.DateEditor(durationSpinner, "HH:mm"));

        durationLabel.setLabelFor(durationSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(durationLabel, org.openide.util.NbBundle.getMessage(SetZeugniskonfDateVisualPanel.class, "SetZeugniskonfDateVisualPanel.durationLabel.text")); // NOI18N

        timeSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.HOUR));
        timeSpinner.setEditor(new javax.swing.JSpinner.DateEditor(timeSpinner, "HH:mm"));

        locationLabel.setLabelFor(locationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(SetZeugniskonfDateVisualPanel.class, "SetZeugniskonfDateVisualPanel.locationLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(locationLabel)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(locationTextField, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(summaryTextField, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(summaryLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(dateLabel)
                                .addGap(18, 18, 18)
                                .addComponent(datePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(timeLabel)
                                    .addComponent(durationLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(durationSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                                    .addComponent(timeSpinner))))
                        .addGap(24, 24, 24))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(durationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(durationLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(summaryLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(summaryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(datePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dateLabel)
                            .addComponent(timeLabel)
                            .addComponent(timeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(31, 31, 31)))
                .addComponent(locationLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(locationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel dateLabel;
    org.jdesktop.swingx.JXDatePicker datePicker;
    private javax.swing.JLabel durationLabel;
    javax.swing.JSpinner durationSpinner;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextField locationTextField;
    private javax.swing.JLabel summaryLabel;
    javax.swing.JTextField summaryTextField;
    private javax.swing.JLabel timeLabel;
    javax.swing.JSpinner timeSpinner;
    // End of variables declaration//GEN-END:variables

    static class SetZeugniskonfDateWizardPanel implements WizardDescriptor.Panel<WizardDescriptor>, DocumentListener { //, ActionListener {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private SetZeugniskonfDateVisualPanel component;
//    private final DateFormat df = new SimpleDateFormat("EE, d.M. HH:mm");
        private final ChangeSupport cSupport = new ChangeSupport(this);
        private boolean valid = false;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public SetZeugniskonfDateVisualPanel getComponent() {
            if (component == null) {
                component = new SetZeugniskonfDateVisualPanel();
            }
            return component;
        }

        @Override
        public HelpCtx getHelp() {
            // Show no Help button for this panel:
            return HelpCtx.DEFAULT_HELP;
            // If you have context help:
            // return new HelpCtx("help.key.here");
        }

        private void validate() {
            boolean before = valid;
            String summary = getSummary();
            Date date = getDate();
            valid = !StringUtils.isBlank(summary) && date != null;
            if (before != valid) {
                cSupport.fireChange();
            }
        }

        private String getSummary() {
            SetZeugniskonfDateVisualPanel panel = getComponent();
            return StringUtils.trimToNull(panel.summaryTextField.getText());
        }

        private Date getDate() {
            return getComponent().datePicker.getDate();
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            cSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cSupport.removeChangeListener(l);
        }

        @Override
        public void readSettings(WizardDescriptor wiz) {
            SetZeugniskonfDateVisualPanel panel = getComponent();
            String sum = (String) wiz.getProperty(SetZeugniskonfDateAction.PROP_NEWSUMMARY);
            if (sum != null) {
                panel.summaryTextField.setText(sum);
            } 
            final LocalDateTime ldt = (LocalDateTime) wiz.getProperty(SetZeugniskonfDateAction.PROP_NEWDATETIME);
            if (ldt != null) {
                final Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                panel.datePicker.setDate(date);
                panel.timeSpinner.setValue(date);
            } else {
                panel.datePicker.setDate(new Date());
            }
            panel.summaryTextField.getDocument().addDocumentListener(this);
            validate();
        }

        @Override
        public void storeSettings(WizardDescriptor wiz) {
            SetZeugniskonfDateVisualPanel panel = getComponent();
            panel.summaryTextField.getDocument().removeDocumentListener(this);
            if (isValid()) {
                wiz.putProperty(SetZeugniskonfDateAction.PROP_NEWSUMMARY, getSummary());
                Date date = getDate();
                LocalDate ld = LocalDate.from(date.toInstant().atZone(ZoneId.systemDefault()));
                Date time = (Date) panel.timeSpinner.getValue();
                LocalTime lt = LocalTime.from(time.toInstant().atZone(ZoneId.systemDefault()));
//            date.setHours(time.getHours());
//            date.setMinutes(time.getMinutes());
                wiz.putProperty(SetZeugniskonfDateAction.PROP_NEWDATETIME, LocalDateTime.of(ld, lt));
                Date dur = (Date) panel.durationSpinner.getValue();
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            validate();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            validate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            validate();
        }

    }

}
