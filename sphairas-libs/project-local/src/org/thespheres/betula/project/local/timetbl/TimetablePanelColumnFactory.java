/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.local.timetbl;

import java.awt.Color;
import java.beans.ConstructorProperties;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author boris.heithecker
 */
@Messages({"TimetablePanelColumnFactory.column.period=Std.",
    "TimetablePanelColumnFactory.column.period.start=Anfang",
    "TimetablePanelColumnFactory.column.period.end=Ende"})
public class TimetablePanelColumnFactory extends ColumnFactory {

    private final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("HH:mm");
    private final StringValue localDateStringValue = v -> v instanceof LocalTime ? DTF.format((LocalTime) v) : "";
    private final DateFormat df = new SimpleDateFormat("HH:mm");
    private final JFormattedTextField ftf = new JFormattedTextField(df);

    @Override
    public void configureColumnWidths(JXTable table, TableColumnExt col) {
        switch (col.getModelIndex()) {
            case 0:
                col.setPreferredWidth(30);
                break;
            case 1:
                col.setPreferredWidth(55);
                break;
            case 2:
                col.setPreferredWidth(75);
                break;
            default:
                col.setPreferredWidth(30);
                break;
        }
    }

    @Override
    public void configureTableColumn(TableModel model, TableColumnExt col) {
        int ci = col.getModelIndex();
        if (ci == 0) {
            String dn = NbBundle.getMessage(TimetablePanelColumnFactory.class, "TimetablePanelColumnFactory.column.period");
            col.setHeaderValue(dn);
        } else if (ci < 3) {
            String dns = NbBundle.getMessage(TimetablePanelColumnFactory.class, "TimetablePanelColumnFactory.column.period.start");
            String dne = NbBundle.getMessage(TimetablePanelColumnFactory.class, "TimetablePanelColumnFactory.column.period.end");
            if (ci == 1) {
                col.setHeaderValue(dns);
            } else {
                col.setHeaderValue(dne);
            }
            ftf.setBorder(new LineBorder(Color.BLACK, 2));
            col.setCellEditor(new DefaultCellEditorExt(ftf));
            col.setCellRenderer(new DefaultTableRenderer(localDateStringValue));
        } else {
            int d = ci - 2;
            DayOfWeek day = DayOfWeek.of(d);
            String dn = day.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault());
            col.setHeaderValue(dn);
            col.setCellEditor(new DefaultCellEditor(new JCheckBox()));
            col.setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
        }
    }

    public static class DefaultCellEditorExt extends DefaultCellEditor {

        @ConstructorProperties({"component"})
        public DefaultCellEditorExt(final JFormattedTextField textField) {
            super(textField);
            //added in super constructor
            textField.removeActionListener(delegate);
            delegate = new EditorDelegate() {
                @Override
                public void setValue(Object value) {
                    textField.setValue(value);
                }

                @Override
                public Object getCellEditorValue() {
                    return textField.getValue();
                }
            };
            textField.addActionListener(delegate);
        }

    }
}
