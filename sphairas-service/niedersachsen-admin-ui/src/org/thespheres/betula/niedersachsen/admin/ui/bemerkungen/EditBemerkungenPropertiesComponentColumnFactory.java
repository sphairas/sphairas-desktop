/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import java.awt.Component;
import java.awt.Font;
import java.text.MessageFormat;
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
import org.openide.util.NbBundle;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"EditBemerkungenPropertiesComponent.columnHeader.values=Text",
    "EditBemerkungenPropertiesComponent.columnHeader.key=Datum (Nr.)"})
class EditBemerkungenPropertiesComponentColumnFactory extends ColumnFactory {

    EditBemerkungenPropertiesComponentColumnFactory(EditBemerkungenPropertiesComponent element) {
    }

    @Override
    public void configureColumnWidths(JXTable table, TableColumnExt col) {
        super.configureColumnWidths(table, col);
        int index = col.getModelIndex();
        switch (index) {
            case 0:
                col.setPreferredWidth(110);
                break;
            default:
                col.setPreferredWidth(900);
                break;
        }
    }

    @Override
    public void configureTableColumn(TableModel model, TableColumnExt col) {
        super.configureTableColumn(model, col);
        int index = col.getModelIndex();
        if (index == 0) {
            configureKeyColumn(model, col);
        } else if (index == 1) {
            configureValueColumn(model, col);
        }
    }

    private void configureKeyColumn(TableModel model, TableColumnExt col) {
        final String h = NbBundle.getMessage(EditBemerkungenPropertiesComponentColumnFactory.class, "EditBemerkungenPropertiesComponent.columnHeader.key");
        col.setHeaderValue(h);
    }

    private void configureValueColumn(final TableModel model, final TableColumnExt col) {
        final String h = NbBundle.getMessage(EditBemerkungenPropertiesComponentColumnFactory.class, "EditBemerkungenPropertiesComponent.columnHeader.values");
        col.setHeaderValue(h);
        class Format implements StringValue {

            @Override
            public String getString(Object value) {
                if (value instanceof String) {
                    final Object[] args = ReportContextListener.getDefault().getCurrentFormatArgs();
                    try {
                        return MessageFormat.format((String) value, args);
                    } catch (Exception e) {
                    }
                }
                return value != null ? value.toString() : "null";
            }

        }
        col.setCellRenderer(new DefaultTableRenderer(new Format()));
        class DirtyHighlighter extends FontHighlighter implements HighlightPredicate {

            @SuppressWarnings({"LeakingThisInConstructor",
                "OverridableMethodCallInConstructor"})
            DirtyHighlighter() {
                setHighlightPredicate(this);
            }

            @Override
            protected boolean canHighlight(Component component, ComponentAdapter adapter) {
                if (getFont() == null) {
                    setFont(adapter.getComponent().getFont().deriveFont(Font.BOLD));
                }
                return super.canHighlight(component, adapter); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                final int r = adapter.convertRowIndexToModel(adapter.row);
                final EditBemerkungenPropertiesComponentTableModel m = (EditBemerkungenPropertiesComponentTableModel) ((JTable) adapter.getComponent()).getModel();
                return m.nodeForRow(r).isDirty();
            }
        }
        col.addHighlighter(new DirtyHighlighter());
    }

}
