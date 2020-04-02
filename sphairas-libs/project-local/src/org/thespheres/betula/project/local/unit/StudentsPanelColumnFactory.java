/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.local.unit;

import java.awt.Component;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.FontHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author boris.heithecker
 */
class StudentsPanelColumnFactory extends ColumnFactory {

    private final NumberFormat NF = NumberFormat.getInstance(Locale.getDefault());
    private final StringValue idStringValue = v -> v instanceof Long ? NF.format(v) : "";

    @Override
    public void configureColumnWidths(JXTable table, TableColumnExt col) {
        switch (col.getModelIndex()) {
            case 0:
                col.setPreferredWidth(200);
                break;
            case 1:
                col.setPreferredWidth(150);
                break;
            case 2:
                col.setPreferredWidth(150);
                break;
            case 3:
                col.setPreferredWidth(100);
                break;
        }
    }

    @Override
    public void configureTableColumn(TableModel model, TableColumnExt col) {
        switch (col.getModelIndex()) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                col.setCellRenderer(new DefaultTableRenderer(idStringValue));
                break;
        }
    }

    static class ModifiedHighlighter extends FontHighlighter implements HighlightPredicate {

        @SuppressWarnings({"LeakingThisInConstructor",
            "OverridableMethodCallInConstructor"})
        ModifiedHighlighter() {
            setHighlightPredicate(this);
        }

        @Override
        protected boolean canHighlight(Component component, ComponentAdapter adapter) {
            if (getFont() == null) {
                setFont(adapter.getComponent().getFont().deriveFont(Font.BOLD));
            }
            return super.canHighlight(component, adapter);
        }

        @Override
        public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
            StudentsPanelModel m = (StudentsPanelModel) ((JTable) adapter.getComponent()).getModel();
            int r = adapter.convertRowIndexToModel(adapter.row);
            return m.isItemModified(r);
        }
    }
}
