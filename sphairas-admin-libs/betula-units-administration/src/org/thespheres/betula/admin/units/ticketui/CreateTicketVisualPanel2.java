/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ticketui;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.NumberFormatter;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteUnitsModel;

@NbBundle.Messages("CreateTicketVisualPanel2.step=Gruppe oder Liste auswählen")
public final class CreateTicketVisualPanel2 extends JPanel {

    private final SigneeTypeTableModel signeeTypeTableModel = new SigneeTypeTableModel();
    private final SigneeTypeTableColumnFactory signeeTypeTableColumnFactory = new SigneeTypeTableColumnFactory();
    private final TargetTypesTableModel targetTypesTableModel = new TargetTypesTableModel();
    private final TargetTypesTableColumnFactory targetTypesTableColumnFactory = new TargetTypesTableColumnFactory();
    private final DefaultComboBoxModel<RemoteStudent> studentsModel = new DefaultComboBoxModel<>();
    private final StringValue converter = o -> o instanceof RemoteStudent ? ((RemoteStudent) o).getDirectoryName() : "";
    private final List<SigneeType> signeeTypes;
    private final List<TargetType> targetTypes = new ArrayList<>();

    CreateTicketVisualPanel2() throws IOException {
        signeeTypes = SigneeType.create();
        initComponents();
        selectSigneeTypeTable.setModel(signeeTypeTableModel);
        selectTargetTypesTable.setModel(targetTypesTableModel);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(CreateTicketComponentVisualPanel.class, "CreateTicketVisualPanel2.step");
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        setStudentComboBox = new javax.swing.JComboBox();
        deleteIntervalLabel = new javax.swing.JLabel();
        deleteIntervalTextField = new JFormattedTextField(new NumberFormatter(NumberFormat.getIntegerInstance(Locale.getDefault())));
        deleteIntervalLabelAfter = new javax.swing.JLabel();
        scopeTextField = new org.jdesktop.swingx.JXTextField();
        setStudentScopeCheckBox = new javax.swing.JCheckBox();
        setDeleteIntervalCheckBox = new javax.swing.JCheckBox();
        selectTargetTypesLabel = new org.jdesktop.swingx.JXLabel();
        selectTargetTypesScrollPane = new javax.swing.JScrollPane();
        selectTargetTypesTable = new org.jdesktop.swingx.JXTable();
        selectSigneeTypeLabel = new org.jdesktop.swingx.JXLabel();
        selectSigneeTypeScrollPane = new javax.swing.JScrollPane();
        selectSigneeTypeTable = new org.jdesktop.swingx.JXTable();

        setStudentComboBox.setModel(studentsModel);
        setStudentComboBox.setRenderer(new DefaultListRenderer(converter));

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, setStudentScopeCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), setStudentComboBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        org.openide.awt.Mnemonics.setLocalizedText(deleteIntervalLabel, org.openide.util.NbBundle.getMessage(CreateTicketVisualPanel2.class, "CreateTicketVisualPanel2.deleteIntervalLabel.text")); // NOI18N

        deleteIntervalTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, setDeleteIntervalCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), deleteIntervalTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        org.openide.awt.Mnemonics.setLocalizedText(deleteIntervalLabelAfter, org.openide.util.NbBundle.getMessage(CreateTicketVisualPanel2.class, "CreateTicketVisualPanel2.deleteIntervalLabelAfter.text")); // NOI18N

        scopeTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(setStudentScopeCheckBox, org.openide.util.NbBundle.getMessage(CreateTicketVisualPanel2.class, "CreateTicketVisualPanel2.setStudentScopeCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(setDeleteIntervalCheckBox, org.openide.util.NbBundle.getMessage(CreateTicketVisualPanel2.class, "CreateTicketVisualPanel2.setDeleteIntervalCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectTargetTypesLabel, org.openide.util.NbBundle.getMessage(CreateTicketVisualPanel2.class, "CreateTicketVisualPanel2.selectTargetTypesLabel.text")); // NOI18N

        selectTargetTypesTable.setColumnFactory(targetTypesTableColumnFactory);
        selectTargetTypesScrollPane.setViewportView(selectTargetTypesTable);

        org.openide.awt.Mnemonics.setLocalizedText(selectSigneeTypeLabel, org.openide.util.NbBundle.getMessage(CreateTicketVisualPanel2.class, "CreateTicketVisualPanel2.selectSigneeTypeLabel.text")); // NOI18N

        selectSigneeTypeTable.setColumnFactory(signeeTypeTableColumnFactory);
        selectSigneeTypeScrollPane.setViewportView(selectSigneeTypeTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(scopeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(setStudentScopeCheckBox)
                                    .addComponent(setDeleteIntervalCheckBox)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(setStudentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(deleteIntervalLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(deleteIntervalTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(deleteIntervalLabelAfter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                    .addComponent(selectTargetTypesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(selectTargetTypesScrollPane))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(selectSigneeTypeScrollPane))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(selectSigneeTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scopeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(setStudentScopeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(setStudentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(setDeleteIntervalCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deleteIntervalLabel)
                    .addComponent(deleteIntervalTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteIntervalLabelAfter))
                .addGap(18, 18, 18)
                .addComponent(selectTargetTypesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectTargetTypesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(selectSigneeTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectSigneeTypeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel deleteIntervalLabel;
    private javax.swing.JLabel deleteIntervalLabelAfter;
    javax.swing.JFormattedTextField deleteIntervalTextField;
    private org.jdesktop.swingx.JXTextField scopeTextField;
    private org.jdesktop.swingx.JXLabel selectSigneeTypeLabel;
    private javax.swing.JScrollPane selectSigneeTypeScrollPane;
    private org.jdesktop.swingx.JXTable selectSigneeTypeTable;
    private org.jdesktop.swingx.JXLabel selectTargetTypesLabel;
    private javax.swing.JScrollPane selectTargetTypesScrollPane;
    private org.jdesktop.swingx.JXTable selectTargetTypesTable;
    private javax.swing.JCheckBox setDeleteIntervalCheckBox;
    private javax.swing.JComboBox setStudentComboBox;
    private javax.swing.JCheckBox setStudentScopeCheckBox;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    @NbBundle.Messages({"SigneeTypeTableColumnFactory.selectedColumn.header=",
        "SigneeTypeTableColumnFactory.displayNameColumn.header=Typ"})
    private class SigneeTypeTableColumnFactory extends ColumnFactory {

        @Override
        public void configureColumnWidths(JXTable table, TableColumnExt col) {
            if (col.getModelIndex() == 0) {
                col.setMaxWidth(16);
                col.setMinWidth(16);
            }
        }

        @Override
        public void configureTableColumn(TableModel model, TableColumnExt col) {
            if (col.getModelIndex() == 0) {
                final String dn = NbBundle.getMessage(CreateTicketVisualPanel2.class, "SigneeTypeTableColumnFactory.selectedColumn.header");
                col.setHeaderValue(dn);
                col.setCellEditor(new DefaultCellEditor(new JCheckBox()));
                col.setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
            } else if (col.getModelIndex() == 1) {
                final String dn = NbBundle.getMessage(CreateTicketVisualPanel2.class, "SigneeTypeTableColumnFactory.displayNameColumn.header");
                col.setHeaderValue(dn);
            }
        }

    }

    private class SigneeTypeTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return signeeTypes.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0 && signeeTypes.get(rowIndex).isSelectable();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            final SigneeType row = signeeTypes.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return row.isSelected();
                case 1:
                    return row.getDisplayName();
            }
            return null;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            final SigneeType row = signeeTypes.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    row.setSelected((Boolean) value);
            }
        }
    }

    @NbBundle.Messages({"TargetTypesTableColumnFactory.selectedColumn.header=",
        "TargetTypesTableColumnFactory.displayNameColumn.header=Liste"})
    private class TargetTypesTableColumnFactory extends ColumnFactory {

        @Override
        public void configureColumnWidths(JXTable table, TableColumnExt col) {
            if (col.getModelIndex() == 0) {
                col.setMaxWidth(16);
                col.setMinWidth(16);
            }
        }

        @Override
        public void configureTableColumn(TableModel model, TableColumnExt col) {
            if (col.getModelIndex() == 0) {
                final String dn = NbBundle.getMessage(CreateTicketVisualPanel2.class, "TargetTypesTableColumnFactory.selectedColumn.header");
                col.setHeaderValue(dn);
                col.setCellEditor(new DefaultCellEditor(new JCheckBox()));
                col.setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
            } else if (col.getModelIndex() == 1) {
                final String dn = NbBundle.getMessage(CreateTicketVisualPanel2.class, "TargetTypesTableColumnFactory.displayNameColumn.header");
                col.setHeaderValue(dn);
            }
        }

    }

    private class TargetTypesTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return targetTypes.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            final TargetType row = targetTypes.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return row.isSelected();
                case 1:
                    return row.getDisplayName();
            }
            return null;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            final TargetType row = targetTypes.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    row.setSelected((Boolean) value);
            }
        }

        private void fireRowsChanged() {
            fireTableDataChanged();
        }
    }

    static class CreateTicketPanel2 implements WizardDescriptor.Panel<WizardDescriptor>, DocumentListener {

        private CreateTicketVisualPanel2 component;
        private final ChangeSupport cSupport = new ChangeSupport(this);
        private boolean valid;

        @Override
        public CreateTicketVisualPanel2 getComponent() {
            if (component == null) {
                try {
                    component = new CreateTicketVisualPanel2();
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return component;
        }

        @Override
        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        private void validate() {
            Long delInterval = getDeleteInterval();
            boolean before = valid;
            valid = delInterval != null && delInterval >= 0;
            if (before != valid) {
                cSupport.fireChange();
            }
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        private Long getDeleteInterval() {
            CreateTicketVisualPanel2 panel = getComponent();
            return (Long) panel.deleteIntervalTextField.getValue();
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
            final CreateTicketVisualPanel2 comp = getComponent();
            String scope = (String) wiz.getProperty(Iterators.PROP_SCOPE_MESSAGE);
            comp.scopeTextField.setText(scope);
            final AbstractUnitOpenSupport uos = (AbstractUnitOpenSupport) wiz.getProperty(Iterators.PROP_UNITOPENSUPPORT);
            comp.studentsModel.removeAllElements();
            comp.studentsModel.addElement(null);
            Boolean setStudentScopeEnabled = (Boolean) wiz.getProperty(Iterators.PROP_STUDENTID_ENABLED);
            comp.setStudentScopeCheckBox.setEnabled(Boolean.TRUE.equals(setStudentScopeEnabled) && uos != null);
            comp.setStudentScopeCheckBox.setSelected(false);
            try {
                if (uos != null) {
                    final RemoteUnitsModel rum = uos.getRemoteUnitsModel(RemoteUnitsModel.INITIALISATION.STUDENTS);
                    rum.getStudents().forEach(comp.studentsModel::addElement);
                }
                valid = true;
                wiz.setMessage(null);
            } catch (IOException ex) {
                valid = false;
                wiz.setMessage(ex.getLocalizedMessage());
                wiz.setMessageType(WizardDescriptor.ERROR_MESSAGE);
            }
            final String targetTypeWarning = (String) wiz.getProperty(Iterators.PROP_TARGETTYPE_WARNING);
            if (targetTypeWarning != null) {
                wiz.setMessage(targetTypeWarning);
            }
            final Boolean delIntervalEnabled = (Boolean) wiz.getProperty(Iterators.PROP_DELETE_INTERVAL_ENABLED);
            comp.setDeleteIntervalCheckBox.setEnabled(Boolean.TRUE.equals(delIntervalEnabled));
            comp.deleteIntervalLabel.setEnabled(delIntervalEnabled);
            comp.deleteIntervalLabelAfter.setEnabled(delIntervalEnabled);
            Long delInterval = (Long) wiz.getProperty(Iterators.PROP_DELETE_INTERVAL);
            if (delInterval == null) {
                delInterval = 300l;
                comp.setDeleteIntervalCheckBox.setSelected(false);
            } else {
                comp.setDeleteIntervalCheckBox.setSelected(true);
            }
            comp.deleteIntervalTextField.setValue(delInterval);
            comp.deleteIntervalTextField.getDocument().addDocumentListener(this);
            final String[] signeeTypes = (String[]) wiz.getProperty(Iterators.PROP_SIGNEETYPES);
            if (signeeTypes != null) {
                comp.signeeTypes.stream()
                        .filter(e -> Arrays.stream(signeeTypes).anyMatch(e.getEntitlement()::equals))
                        .forEach(e -> e.setSelected(true));
            }
            comp.targetTypes.clear();
            final Set<String> targetTypesAvailable = (Set<String>) wiz.getProperty(Iterators.PROP_TARGETTYPESAVAILABLE);
            if (targetTypesAvailable != null) {
                targetTypesAvailable.stream()
                        .map(TargetType::new)
                        .forEach(comp.targetTypes::add);
                comp.targetTypesTableModel.fireRowsChanged();
            }
            final String[] targetTypes = (String[]) wiz.getProperty(Iterators.PROP_TARGETTYPES);
            if (targetTypes != null) {
                comp.targetTypes.stream()
                        .filter(e -> Arrays.stream(targetTypes).anyMatch(e.getTargetType()::equals))
                        .forEach(e -> e.setSelected(true));
            }
        }

        @Override
        public void storeSettings(WizardDescriptor wiz) {
            CreateTicketVisualPanel2 comp = getComponent();
            comp.deleteIntervalTextField.getDocument().removeDocumentListener(this);
            RemoteStudent rs = (RemoteStudent) comp.studentsModel.getSelectedItem();
            if (comp.setStudentScopeCheckBox.isSelected() && rs != null) {
                wiz.putProperty(Iterators.PROP_STUDENTID, rs.getStudentId());
            }
            final String[] signeeTypes = comp.signeeTypes.stream()
                    .filter(SigneeType::isSelected)
                    .map(SigneeType::getEntitlement)
                    .toArray(String[]::new);
            wiz.putProperty(Iterators.PROP_SIGNEETYPES, signeeTypes);
            final String[] targetTypes = comp.targetTypes.stream()
                    .filter(TargetType::isSelected)
                    .map(TargetType::getTargetType)
                    .toArray(String[]::new);
            wiz.putProperty(Iterators.PROP_TARGETTYPES, targetTypes);
            final Long delInterval = comp.setDeleteIntervalCheckBox.isSelected() ? getDeleteInterval() : null;
            wiz.putProperty(Iterators.PROP_DELETE_INTERVAL, delInterval);
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
