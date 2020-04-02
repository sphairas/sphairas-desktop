/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;

/**
 *
 * @author boris.heithecker
 */
@Messages("ColumnSearchComponent.label.text=Listen-Filter")
class ColumnSearchComponent extends JXTextField implements DocumentListener {

    private final JXTable table;
    private final Set<Integer> hidden = new HashSet<>();
    private final Listener listener = new Listener();

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    ColumnSearchComponent(JXTable table) {
        super(NbBundle.getMessage(ColumnSearchComponent.class, "ColumnSearchComponent.label.text"));
        this.table = table;
        setColumns(8);
//        setPreferredSize(new Dimension(150, getPreferredSize().height));
        this.table.addPropertyChangeListener("model", null);
        this.table.addPropertyChangeListener(listener);
        this.table.getColumnModel().addColumnModelListener(table);
        getDocument().addDocumentListener(this);
    }

    private void update() {
        final TargetsElementModel model = (TargetsElementModel) table.getModel();
        if (model == null) {
            return;
        }
        final String match = getText();
        if (StringUtils.isBlank(match)) {
            table.getColumns(true).stream()
                    .map(tc -> (TableColumnExt) tc)
                    .forEach(col -> {
                        int index = col.getModelIndex() - 1;
                        if (hidden.contains(index)) {
                            col.setVisible(true);
                            hidden.remove(index);
                        }
                    });
        } else {
            table.getColumns(true).stream()
                    .map(tc -> (TableColumnExt) tc)
                    .forEach(col -> {
                        int index = col.getModelIndex();
                        if (index > 0 && index < model.getRemoteTargetAssessmentDocumentsSize() - 1) {
                            RemoteTargetAssessmentDocument rtad = model.getRemoteTargetAssessmentDocumentAtColumnIndex(index);
                            String text = rtad.getName().getSearchableString(null); //TODO: use term? custon function?
                            if (!StringUtils.containsIgnoreCase(text, match)) {
                                if (col.isVisible()) {
                                    col.setVisible(false);
                                    hidden.add(index);
                                }
                            } else if (hidden.contains(index)) {
                                col.setVisible(true);
                                hidden.remove(index);
                            }
                        }
                    });
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        update();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        update();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        update();
    }

    private final class Listener implements PropertyChangeListener, TableModelListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("model".equals(evt.getPropertyName())) {
                TableModel old = (TableModel) evt.getOldValue();
                if (old != null) {
                    old.removeTableModelListener(this);
                }
                TableModel nModel = (TableModel) evt.getNewValue();
                if (nModel != null) {
                    nModel.addTableModelListener(this);
                }
            }
        }

        @Override
        public void tableChanged(TableModelEvent e) {
            if (e.getColumn() == TableModelEvent.ALL_COLUMNS) {
                hidden.clear();
                EventQueue.invokeLater(ColumnSearchComponent.this::update);
            }
        }

    }

}
