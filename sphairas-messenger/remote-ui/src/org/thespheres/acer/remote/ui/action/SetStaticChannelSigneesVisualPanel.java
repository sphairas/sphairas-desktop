/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.util.Signees;

@Messages({"SetStaticChannelSigneesVisualPanel.step=Unterzeichner auswählen",
    "SetStaticChannelSigneesVisualPanel.headerValue.signee=Unterzeichner"})
final class SetStaticChannelSigneesVisualPanel extends JPanel {

    private final Model model = new Model();
    private Signees signees;

    SetStaticChannelSigneesVisualPanel() {
        initComponents();
        signeesTable.setModel(model);
    }

    void setSignees(Signees sig) {
        this.signees = sig;
        model.init();
    }

    void setSelectedSignees(Signee[] list) {
        model.list.stream().filter(sv -> Arrays.stream(list).anyMatch(sv::equals)).forEach(sv -> sv.selected = true);
    }

    Signee[] getSelectedSignees() {
        return model.list.stream().filter(sv -> sv.selected).map(sv -> sv.signee).toArray(Signee[]::new);
    }

    boolean isIncludeAll() {
        return selectAllCheckBox.isSelected();
    }

    void setIncludeAll(boolean includeAll) {
        selectAllCheckBox.setSelected(includeAll);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SetStaticChannelSigneesVisualPanel.class, "SetStaticChannelSigneesVisualPanel.step");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        selectSigneesLabel = new javax.swing.JLabel();
        signeesScrollPane = new javax.swing.JScrollPane();
        signeesTable = new org.jdesktop.swingx.JXTable();
        selectAllCheckBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(selectSigneesLabel, org.openide.util.NbBundle.getMessage(SetStaticChannelSigneesVisualPanel.class, "SetStaticChannelSigneesVisualPanel.selectSigneesLabel.text")); // NOI18N

        signeesTable.setColumnFactory(new ColFactory());
        signeesTable.setHorizontalScrollEnabled(true);

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, selectAllCheckBox, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), signeesTable, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        signeesScrollPane.setViewportView(signeesTable);

        selectAllCheckBox.setLabel(org.openide.util.NbBundle.getMessage(SetStaticChannelSigneesVisualPanel.class, "CreateStaticChannelVisualPanel.selectAllCheckBox.text")); // NOI18N
        selectAllCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(signeesScrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(selectSigneesLabel)
                            .addComponent(selectAllCheckBox))
                        .addGap(0, 247, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectSigneesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(signeesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(selectAllCheckBox)
                .addContainerGap())
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void selectAllCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllCheckBoxActionPerformed
        model.fireTableDataChanged();
    }//GEN-LAST:event_selectAllCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox selectAllCheckBox;
    private javax.swing.JLabel selectSigneesLabel;
    private javax.swing.JScrollPane signeesScrollPane;
    private org.jdesktop.swingx.JXTable signeesTable;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    private final class ColFactory extends ColumnFactory {

        private final StringValue signeeStringValue = o -> signees.getSignee((Signee) o);

        @Override
        public void configureColumnWidths(JXTable table, TableColumnExt col) {
            int index = col.getModelIndex();
            switch (index) {
                case 0:
                    col.setMinWidth(16);
                    col.setMaxWidth(16);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void configureTableColumn(TableModel model, TableColumnExt col) {
            int index = col.getModelIndex();
            if (index == 0) {
                col.setHeaderValue("");
                col.setCellEditor(new DefaultCellEditor(new JCheckBox()));
                col.setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
            } else if (index == 1) {
                String header = NbBundle.getMessage(SetStaticChannelSigneesVisualPanel.class, "SetStaticChannelSigneesVisualPanel.headerValue.signee");
                col.setHeaderValue(header);
                col.setCellRenderer(new DefaultTableRenderer(signeeStringValue));
            }
        }
    }

    private final class Model extends AbstractTableModel {

        private final List<SigneeValue> list = new ArrayList<>();

        private void init() {
            list.clear();
            signees.getSigneeSet()
                    .forEach(s -> list.add(new SigneeValue(s)));
            fireTableStructureChanged();
        }

        @Override
        public int getRowCount() {
            return list.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return !isIncludeAll() && columnIndex == 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return isIncludeAll() ? true :  list.get(rowIndex).selected;
                case 1:
                    return list.get(rowIndex).signee;
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            list.get(rowIndex).selected = (boolean) value;
        }

        private final class SigneeValue {

            private final Signee signee;
            private boolean selected = false;

            private SigneeValue(Signee signee) {
                this.signee = signee;
            }

        }

    }
}
