/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.openide.util.ImageUtilities;

/**
 *
 * @author boris.heithecker
 */
public class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    public static final String PROP_TABLE_ROW = "table-row";
    public static final String PROP_TABLE_COLUMN = "table-column";
    private final JButton button = new JButton();
    private ActionListener listener;
    private final ComponentProvider<JButton> buttonProvider = new ComponentProvider<JButton>() {

        @Override
        protected void format(CellContext cc) {
        }

        @Override
        protected void configureState(CellContext cc) {
//            deleteButton.putClientProperty(cc, cc);
        }

        @Override
        protected JButton createRendererComponent() {
            return button;
        }
    };

    @SuppressWarnings(value = "LeakingThisInConstructor")
    public ButtonEditor() {
        ImageIcon icon = ImageUtilities.loadImageIcon("org/thespheres/betula/admin/units/resources/cross-button.png", true);
        button.setIcon(icon);
        button.addActionListener(this);
    }

    @Override
    public Object getCellEditorValue() {
        return listener;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        button.putClientProperty(PROP_TABLE_ROW, row);
        button.putClientProperty(PROP_TABLE_COLUMN, column);
        if (value instanceof ActionListener) {
            listener = (ActionListener) value;
        } else {
            listener = null;
        }
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (listener != null) {
            listener.actionPerformed(e);
        }
    }

    public ComponentProvider<JButton> getDeleteButtonProvider() {
        return buttonProvider;
    }

    public TableCellRenderer createRenderer() {
        return new DefaultTableRenderer(buttonProvider);
    }

    public static void configureTableColumn(TableColumn column) {
        column.setMinWidth(16);
        column.setMaxWidth(16);
    }

}
