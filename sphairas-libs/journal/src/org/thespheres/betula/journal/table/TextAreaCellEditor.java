/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.util.Map;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author boris.heithecker
 */
public class TextAreaCellEditor extends AbstractCellEditor implements TableCellEditor {

    protected JTextArea editorComponent;
    protected JScrollPane pane;
    private final Map<Integer, Point> positionsMap;
    private int currentRow;

    public TextAreaCellEditor(Map<Integer, Point> positionsMap) {
        this.positionsMap = positionsMap;
        pane = new JScrollPane();
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        editorComponent = new JTextArea();

        editorComponent.setName("Table.editor");
        editorComponent.setRows(3);
        editorComponent.setLineWrap(true);
        editorComponent.setWrapStyleWord(true);
        editorComponent.setOpaque(true);
//        editorComponent.ssetHorizontalAlignment(JTextField.RIGHT);
//        editorComponent.addActionListener(this);
//editorComponent.setInputVerifier(inputVerifier);
        pane.setViewportView(editorComponent);
        pane.setViewportBorder(BorderFactory.createEmptyBorder());
    }

    @Override
    public boolean stopCellEditing() {
        boolean ret = super.stopCellEditing();
        Point viewPosition = pane.getViewport().getViewPosition();
//        int row = (int) pane.getClientProperty("editing-row");
//        positionsMap.put(row, viewPosition);
        positionsMap.put(currentRow, viewPosition);
        return ret;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        editorComponent.setBorder(new LineBorder(Color.black));
        editorComponent.setText((String) value);
//        pane.putClientProperty("editing-row", row);
        currentRow = row;
        Point p = positionsMap.get(row);
        if (p != null) {
            pane.getViewport().setViewPosition(p);
        }
        return pane;
    }

    @Override
    public String getCellEditorValue() throws IllegalStateException {
        return editorComponent.getText();
    }

}
