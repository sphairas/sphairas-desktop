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
import java.util.Arrays;
import javax.swing.DefaultComboBoxModel;
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
class NotenOSAssessToolPanel extends javax.swing.JPanel implements PropertyChangeListener, ActionListener {

    private final NotenOSAssessment context;
    private final DefaultComboBoxModel cbModel;
    private final StringValue distStringValue = v -> v instanceof Distribution ? ((Distribution) v).getDisplayName() : null;
    private boolean updatingComboBox;

    @SuppressWarnings({"LeakingThisInConstructor"})
    NotenOSAssessToolPanel(NotenOSAssessment context) {
        this.context = context;
        this.cbModel = new DefaultComboBoxModel(context.getDefaultDistributions());
        initComponents();
        updateComboBox();
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
        jSpinner1 = new Int2ValueSpinner(NotenOS.P15, context);
        jSpinner2 = new Int2ValueSpinner(NotenOS.P14, context);
        jSpinner3 = new Int2ValueSpinner(NotenOS.P13, context);
        jSpinner4 = new Int2ValueSpinner(NotenOS.P12, context);
        jSpinner5 = new Int2ValueSpinner(NotenOS.P11, context);
        jSpinner6 = new Int2ValueSpinner(NotenOS.P3, context);
        jSpinner7 = new Int2PercentageSpinner(NotenOS.P15, context);
        jSpinner8 = new Int2PercentageSpinner(NotenOS.P14, context);
        jSpinner9 = new Int2PercentageSpinner(NotenOS.P13, context);
        jSpinner10 = new Int2PercentageSpinner(NotenOS.P12, context);
        jSpinner11 = new Int2PercentageSpinner(NotenOS.P11, context);
        jSpinner12 = new Int2PercentageSpinner(NotenOS.P10, context);
        jPanel2 = new javax.swing.JPanel();
        distChooserComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jSpinner13 = new Int2ValueSpinner(NotenOS.P9, context);
        jSpinner14 = new Int2ValueSpinner(NotenOS.P8, context);
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jSpinner15 = new Int2ValueSpinner(NotenOS.P7, context);
        jSpinner16 = new Int2ValueSpinner(NotenOS.P4, context);
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jSpinner17 = new Int2ValueSpinner(NotenOS.P6, context);
        jLabel13 = new javax.swing.JLabel();
        jSpinner18 = new Int2ValueSpinner(NotenOS.P5, context);
        jSpinner19 = new Int2ValueSpinner(NotenOS.P10, context);
        jLabel14 = new javax.swing.JLabel();
        jSpinner20 = new Int2ValueSpinner(NotenOS.P2, context);
        jLabel15 = new javax.swing.JLabel();
        jSpinner21 = new Int2ValueSpinner(NotenOS.P1, context);
        jLabel16 = new javax.swing.JLabel();
        jSpinner22 = new Int2ValueSpinner(NotenOS.P0, context);
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jSpinner23 = new Int2PercentageSpinner(NotenOS.P9, context);
        jSpinner24 = new Int2PercentageSpinner(NotenOS.P8, context);
        jSpinner25 = new Int2PercentageSpinner(NotenOS.P7, context);
        jSpinner26 = new Int2PercentageSpinner(NotenOS.P6, context);
        jSpinner27 = new Int2PercentageSpinner(NotenOS.P5, context);
        jSpinner28 = new Int2PercentageSpinner(NotenOS.P4, context);
        jSpinner29 = new Int2PercentageSpinner(NotenOS.P3, context);
        jSpinner30 = new Int2PercentageSpinner(NotenOS.P2, context);
        jSpinner31 = new Int2PercentageSpinner(NotenOS.P1, context);
        jSpinner32 = new Int2PercentageSpinner(NotenOS.P0, context);

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
        gridBagConstraints.gridx = 13;
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
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("NotenOSAssessToolPanel.jPanel2.border.title_1"))); // NOI18N
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        distChooserComboBox.setModel(cbModel);
        distChooserComboBox.setPreferredSize(new java.awt.Dimension(167, 20));
        jPanel2.add(distChooserComboBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jPanel2, gridBagConstraints);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel1.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(jLabel1, gridBagConstraints);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel2.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(jLabel2, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel3.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel3.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(jLabel3, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel4.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel4.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        add(jLabel4, gridBagConstraints);

        jLabel5.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel5.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel5.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        add(jLabel5, gridBagConstraints);

        jLabel6.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel6.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel6.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        add(jLabel6, gridBagConstraints);

        jLabel7.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel7.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel7.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        add(jLabel7, gridBagConstraints);

        jLabel8.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel8.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel8.text_1")); // NOI18N
        add(jLabel8, new java.awt.GridBagConstraints());

        jSpinner13.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner13, gridBagConstraints);

        jSpinner14.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner14, gridBagConstraints);

        jLabel9.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel9.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel9.text")); // NOI18N
        add(jLabel9, new java.awt.GridBagConstraints());

        jLabel10.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel10.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel10.text")); // NOI18N
        add(jLabel10, new java.awt.GridBagConstraints());

        jSpinner15.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner15, gridBagConstraints);

        jSpinner16.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner16, gridBagConstraints);

        jLabel11.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel11.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel11.text")); // NOI18N
        add(jLabel11, new java.awt.GridBagConstraints());

        jLabel12.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel12.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel12.text")); // NOI18N
        add(jLabel12, new java.awt.GridBagConstraints());

        jSpinner17.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner17, gridBagConstraints);

        jLabel13.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel13.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel13.text")); // NOI18N
        add(jLabel13, new java.awt.GridBagConstraints());

        jSpinner18.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner18, gridBagConstraints);

        jSpinner19.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner19, gridBagConstraints);

        jLabel14.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel14.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel14.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 13;
        gridBagConstraints.gridy = 0;
        add(jLabel14, gridBagConstraints);

        jSpinner20.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner20, gridBagConstraints);

        jLabel15.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel15.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel15.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 0;
        add(jLabel15, gridBagConstraints);

        jSpinner21.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner21, gridBagConstraints);

        jLabel16.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel16.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel16.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 0;
        add(jLabel16, gridBagConstraints);

        jSpinner22.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 16;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 20);
        add(jSpinner22, gridBagConstraints);

        jLabel17.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel17.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel17.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 16;
        gridBagConstraints.gridy = 0;
        add(jLabel17, gridBagConstraints);

        jLabel18.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        jLabel18.setText(org.openide.util.NbBundle.getMessage(NotenOSAssessToolPanel.class, "NotenOSAssessToolPanel.jLabel18.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 0;
        add(jLabel18, gridBagConstraints);

        jSpinner23.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner23, gridBagConstraints);

        jSpinner24.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner24, gridBagConstraints);

        jSpinner25.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner25, gridBagConstraints);

        jSpinner26.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner26, gridBagConstraints);

        jSpinner27.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner27, gridBagConstraints);

        jSpinner28.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner28, gridBagConstraints);

        jSpinner29.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 13;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner29, gridBagConstraints);

        jSpinner30.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner30, gridBagConstraints);

        jSpinner31.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jSpinner31, gridBagConstraints);

        jSpinner32.setPreferredSize(new java.awt.Dimension(66, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 16;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 20);
        add(jSpinner32, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox distChooserComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinner10;
    private javax.swing.JSpinner jSpinner11;
    private javax.swing.JSpinner jSpinner12;
    private javax.swing.JSpinner jSpinner13;
    private javax.swing.JSpinner jSpinner14;
    private javax.swing.JSpinner jSpinner15;
    private javax.swing.JSpinner jSpinner16;
    private javax.swing.JSpinner jSpinner17;
    private javax.swing.JSpinner jSpinner18;
    private javax.swing.JSpinner jSpinner19;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JSpinner jSpinner20;
    private javax.swing.JSpinner jSpinner21;
    private javax.swing.JSpinner jSpinner22;
    private javax.swing.JSpinner jSpinner23;
    private javax.swing.JSpinner jSpinner24;
    private javax.swing.JSpinner jSpinner25;
    private javax.swing.JSpinner jSpinner26;
    private javax.swing.JSpinner jSpinner27;
    private javax.swing.JSpinner jSpinner28;
    private javax.swing.JSpinner jSpinner29;
    private javax.swing.JSpinner jSpinner3;
    private javax.swing.JSpinner jSpinner30;
    private javax.swing.JSpinner jSpinner31;
    private javax.swing.JSpinner jSpinner32;
    private javax.swing.JSpinner jSpinner4;
    private javax.swing.JSpinner jSpinner5;
    private javax.swing.JSpinner jSpinner6;
    private javax.swing.JSpinner jSpinner7;
    private javax.swing.JSpinner jSpinner8;
    private javax.swing.JSpinner jSpinner9;
    // End of variables declaration//GEN-END:variables

}
