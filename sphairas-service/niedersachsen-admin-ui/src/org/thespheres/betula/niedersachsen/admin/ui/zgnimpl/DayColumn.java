/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl;

import org.thespheres.betula.niedersachsen.admin.ui.ZeugnisAngabenColumn;
import javax.swing.JFormattedTextField;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.decorator.AlignmentHighlighter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.thespheres.betula.niedersachsen.admin.ui.RemoteReportsModel2;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.IntegerEditor;

/**
 *
 * @author boris.heithecker
 */
public abstract class DayColumn extends ZeugnisAngabenColumn {

    protected final IntegerEditor daysEditor = new IntegerEditor(0, 365);
    protected final AlignmentHighlighter centerHighlighter = new AlignmentHighlighter(SwingConstants.CENTER);
    private final StringValue intStringValue = v -> (v == null || (int) v == 0) ? "---" : v.toString();

    protected DayColumn(String id, int position, boolean editable, int width) {
        super(id, position, editable, width);
        ((JFormattedTextField) daysEditor.getComponent()).setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public void configureTableColumn(AbstractPluggableTableModel<RemoteReportsModel2, ReportData2, ?, ?> model, TableColumnExt col) {
        super.configureTableColumn(model, col);
        col.setCellRenderer(new DefaultTableRenderer(intStringValue));
        col.setCellEditor(daysEditor);
        col.addHighlighter(centerHighlighter);
    }

}
