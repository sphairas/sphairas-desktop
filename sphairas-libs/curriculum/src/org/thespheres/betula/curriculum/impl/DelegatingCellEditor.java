/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.impl;

import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import org.thespheres.betula.curriculum.util.EditableCourseSelectionValue;

/**
 *
 * @author boris.heithecker
 */
public class DelegatingCellEditor extends DefaultCellEditor {

    private EditableCourseSelectionValue currentDelegate;

    DelegatingCellEditor(JComboBox comboBox) {
        super(comboBox);
    }

    @Override
    public Object getCellEditorValue() {
        if (currentDelegate != null) {
            return currentDelegate.getCellEditorValue();
        } else {
            return super.getCellEditorValue();
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof EditableCourseSelectionValue) {
            currentDelegate = (EditableCourseSelectionValue) value;
            return ((EditableCourseSelectionValue) value).getTableCellEditorComponent(table, value, isSelected, row, column);
        } else {
            currentDelegate = null;
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
    }
}
