/*
 * NotenAssessToolPanel.java
 *
 * Created on 1. August 2008, 00:08
 */
package org.thespheres.betula.noten.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.Arrays;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.openide.util.Mutex;
import org.thespheres.betula.util.Int2;
import org.thespheres.betula.assess.Distribution;

/**
 *
 * @author boris.heithecker
 */
class NotenAssessToolPanel extends javax.swing.JPanel implements PropertyChangeListener, ActionListener {

    private final NotenAssessment context;
    private final DefaultComboBoxModel cbModel;
    private final StringValue distStringValue = v -> v instanceof Distribution ? ((Distribution) v).getDisplayName() : null;
    private boolean updatingComboBox;

    @SuppressWarnings({"LeakingThisInConstructor"})
    NotenAssessToolPanel(NotenAssessment context) {
        this.context = context;
        this.cbModel = new DefaultComboBoxModel(context.getDefaultDistributions());
        initComponents();
        updateComboBox();
        updateMargin();
        distChooserComboBox.setRenderer(new DefaultListRenderer(distStringValue));
        distChooserComboBox.addActionListener(this);
        context.addPropertyChangeListener(this);
    }

    private void updateComboBox() {
        Distribution current = context.getCurrentDistribution();
        final Distribution<Int2> dist = Arrays.stream(context.getDefaultDistributions())
                .filter(current::equals)
                .findAny()
                .orElse(null);
        updatingComboBox = true;
        distChooserComboBox.setSelectedItem(dist);
        updatingComboBox = false;
    }

    private void updateMargin() {
        this.customRadioButton2.setSelected(context.getMarginModel().getMarginModel() == MarginModel.Model.Margin);
        this.customFormattedTextField.setValue(context.getMarginModel().getMarginValue());
        customFormattedTextField.setEnabled(customRadioButton2.isSelected());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Mutex.EVENT.writeAccess(this::updateComboBox);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!updatingComboBox) {
            Distribution dist = (Distribution) distChooserComboBox.getSelectedItem();
            if (dist != null) {
                Distribution before = context.getCurrentDistribution();
                context.applyDistribution(dist);
                class Edit extends AbstractUndoableEdit {

                    @Override
                    public void redo() throws CannotRedoException {
                        context.applyDistribution(dist);
                    }

                    @Override
                    public void undo() throws CannotUndoException {
                        context.applyDistribution(before);
                    }

                }
                context.undoSupport.postEdit(new Edit());
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jSpinner1 = new Int2ValueSpinner(NotenOld.EINS, context);
        jSpinner2 = new Int2ValueSpinner(NotenOld.ZWEI, context);
        jSpinner3 = new Int2ValueSpinner(NotenOld.DREI, context);
        jSpinner4 = new Int2ValueSpinner(NotenOld.VIER, context);
        jSpinner5 = new Int2ValueSpinner(NotenOld.FUENF, context);
        jSpinner6 = new Int2ValueSpinner(NotenOld.SECHS, context);
        jSpinner7 = new Int2PercentageSpinner(NotenOld.EINS, context);
        jSpinner8 = new Int2PercentageSpinner(NotenOld.ZWEI, context);
        jSpinner9 = new Int2PercentageSpinner(NotenOld.DREI, context);
        jSpinner10 = new Int2PercentageSpinner(NotenOld.VIER, context);
        jSpinner11 = new Int2PercentageSpinner(NotenOld.FUENF, context);
        jSpinner12 = new Int2PercentageSpinner(NotenOld.SECHS, context);
        distPanel = new javax.swing.JPanel();
        distChooserComboBox = new org.jdesktop.swingx.JXComboBox();
        marginPanel = new javax.swing.JPanel();
        equalRadioButton = new javax.swing.JRadioButton();
        customRadioButton2 = new javax.swing.JRadioButton();
        customFormattedTextField = new javax.swing.JFormattedTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jSpinner1.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner1, gridBagConstraints);

        jSpinner2.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner2, gridBagConstraints);

        jSpinner3.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner3, gridBagConstraints);

        jSpinner4.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner4, gridBagConstraints);

        jSpinner5.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner5, gridBagConstraints);

        jSpinner6.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner6, gridBagConstraints);

        jSpinner7.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner7, gridBagConstraints);

        jSpinner8.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner8, gridBagConstraints);

        jSpinner9.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner9, gridBagConstraints);

        jSpinner10.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner10, gridBagConstraints);

        jSpinner11.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner11, gridBagConstraints);

        jSpinner12.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner12, gridBagConstraints);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thespheres/betula/noten/impl/Bundle"); // NOI18N
        distPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("NotenAssessToolPanel.distPanel.border.title"))); // NOI18N
        distPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        distChooserComboBox.setModel(cbModel);
        distChooserComboBox.setPreferredSize(new java.awt.Dimension(167, 20));
        distPanel.add(distChooserComboBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(distPanel, gridBagConstraints);

        marginPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("NotenAssessToolPanel.marginPanel.border.title"), javax.swing.border.TitledBorder.LEADING, javax.swing.border.TitledBorder.TOP)); // NOI18N
        marginPanel.setLayout(new java.awt.GridBagLayout());

        buttonGroup1.add(equalRadioButton);
        equalRadioButton.setSelected(true);
        equalRadioButton.setText(org.openide.util.NbBundle.getMessage(NotenAssessToolPanel.class, "NotenAssessToolPanel.equalRadioButton.text")); // NOI18N
        equalRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        equalRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                equalRadioButtonmarginModeSelected(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        marginPanel.add(equalRadioButton, gridBagConstraints);

        buttonGroup1.add(customRadioButton2);
        customRadioButton2.setText(org.openide.util.NbBundle.getMessage(NotenAssessToolPanel.class, "NotenAssessToolPanel.customRadioButton2.text")); // NOI18N
        customRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        customRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customRadioButton2marginModeSelected(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        marginPanel.add(customRadioButton2, gridBagConstraints);

        customFormattedTextField.setEnabled(false);
        customFormattedTextField.setPreferredSize(new java.awt.Dimension(40, 20));
        final JFormattedTextField.AbstractFormatter af = new JFormattedTextField.AbstractFormatter() {

            public Object stringToValue(String text) throws ParseException {
                return new Int2(text);
            }

            public String valueToString(Object value) throws ParseException {
                if(value == null || !(value instanceof Int2)) return "";
                else return ((Int2)value).toString();
            }
        };
        customFormattedTextField.setFormatterFactory(new AbstractFormatterFactory() {

            public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
                return af;
            }
        });
        customFormattedTextField.setValue(Int2.fromInternalValue(2));
        customFormattedTextField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                customFormattedTextFieldcustomTextFieldPropertyChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        marginPanel.add(customFormattedTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(marginPanel, gridBagConstraints);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(NotenAssessToolPanel.class, "NotenAssessToolPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(jLabel1, gridBagConstraints);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(NotenAssessToolPanel.class, "NotenAssessToolPanel.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        add(jLabel2, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel3.setText(org.openide.util.NbBundle.getMessage(NotenAssessToolPanel.class, "NotenAssessToolPanel.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(jLabel3, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel4.setText(org.openide.util.NbBundle.getMessage(NotenAssessToolPanel.class, "NotenAssessToolPanel.jLabel4.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        add(jLabel4, gridBagConstraints);

        jLabel5.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel5.setText(org.openide.util.NbBundle.getMessage(NotenAssessToolPanel.class, "NotenAssessToolPanel.jLabel5.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        add(jLabel5, gridBagConstraints);

        jLabel6.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel6.setText(org.openide.util.NbBundle.getMessage(NotenAssessToolPanel.class, "NotenAssessToolPanel.jLabel6.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        add(jLabel6, gridBagConstraints);

        jLabel7.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel7.setText(org.openide.util.NbBundle.getMessage(NotenAssessToolPanel.class, "NotenAssessToolPanel.jLabel7.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        add(jLabel7, gridBagConstraints);

        jLabel8.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel8.setText(org.openide.util.NbBundle.getMessage(NotenAssessToolPanel.class, "NotenAssessToolPanel.jLabel8.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        add(jLabel8, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void equalRadioButtonmarginModeSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_equalRadioButtonmarginModeSelected
    if (customRadioButton2.isSelected() == (context.getMarginModel().getMarginModel() == MarginModel.Model.Margin)) {
        return;
    }
    final MarginModel.Model value = customRadioButton2.isSelected() ? MarginModel.Model.Margin : MarginModel.Model.Equal;
    final MarginModel.Model before = context.getMarginModel().getMarginModel();
    context.getMarginModel().setMarginModel(value);
    class Edit extends AbstractUndoableEdit {

        @Override
        public void redo() throws CannotRedoException {
            context.getMarginModel().setMarginModel(value);
        }

        @Override
        public void undo() throws CannotUndoException {
            context.getMarginModel().setMarginModel(before);
        }

    }
    context.undoSupport.postEdit(new Edit());
    customFormattedTextField.setEnabled(customRadioButton2.isSelected());
}//GEN-LAST:event_equalRadioButtonmarginModeSelected

private void customRadioButton2marginModeSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customRadioButton2marginModeSelected
    if (customRadioButton2.isSelected() == (context.getMarginModel().getMarginModel() == MarginModel.Model.Margin)) {
        return;
    }
    final MarginModel.Model value = customRadioButton2.isSelected() ? MarginModel.Model.Margin : MarginModel.Model.Equal;
    final MarginModel.Model before = context.getMarginModel().getMarginModel();
    context.getMarginModel().setMarginModel(value);
    class Edit extends AbstractUndoableEdit {

        @Override
        public void redo() throws CannotRedoException {
            context.getMarginModel().setMarginModel(value);
        }

        @Override
        public void undo() throws CannotUndoException {
            context.getMarginModel().setMarginModel(before);
        }

    }
    context.undoSupport.postEdit(new Edit());
    customFormattedTextField.setEnabled(customRadioButton2.isSelected());
}//GEN-LAST:event_customRadioButton2marginModeSelected

private void customFormattedTextFieldcustomTextFieldPropertyChanged(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_customFormattedTextFieldcustomTextFieldPropertyChanged
    if ((evt.getSource() instanceof JFormattedTextField) && "value".equals(evt.getPropertyName())) {
        Int2 value = (Int2) customFormattedTextField.getValue();
        final Int2 before = context.getMarginModel().getMarginValue();
        context.getMarginModel().setMarginValue(value);
        class Edit extends AbstractUndoableEdit {

            @Override
            public void redo() throws CannotRedoException {
                context.getMarginModel().setMarginValue(value);
            }

            @Override
            public void undo() throws CannotUndoException {
                context.getMarginModel().setMarginValue(before);
            }

        }
        context.undoSupport.postEdit(new Edit());
    }
}//GEN-LAST:event_customFormattedTextFieldcustomTextFieldPropertyChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JFormattedTextField customFormattedTextField;
    private javax.swing.JRadioButton customRadioButton2;
    private org.jdesktop.swingx.JXComboBox distChooserComboBox;
    private javax.swing.JPanel distPanel;
    private javax.swing.JRadioButton equalRadioButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinner10;
    private javax.swing.JSpinner jSpinner11;
    private javax.swing.JSpinner jSpinner12;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JSpinner jSpinner3;
    private javax.swing.JSpinner jSpinner4;
    private javax.swing.JSpinner jSpinner5;
    private javax.swing.JSpinner jSpinner6;
    private javax.swing.JSpinner jSpinner7;
    private javax.swing.JSpinner jSpinner8;
    private javax.swing.JSpinner jSpinner9;
    private javax.swing.JPanel marginPanel;
    // End of variables declaration//GEN-END:variables

}
