/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl;

import org.thespheres.betula.niedersachsen.admin.ui.ZeugnisAngabenColumn;
import javax.swing.DefaultCellEditor;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.decorator.AlignmentHighlighter;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;
import org.thespheres.betula.niedersachsen.admin.ui.RemoteReportsModel2;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.ui.GradeComboBoxModel;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;

/**
 *
 * @author boris.heithecker
 */
public abstract class KopfnoteColumn extends ZeugnisAngabenColumn {

    protected final String convention;
    protected final JXComboBox box;
    protected final GradeComboBoxModel gcbm;
    protected final AlignmentHighlighter centerHighlighter = new AlignmentHighlighter(SwingConstants.CENTER);

    protected KopfnoteColumn(String id, int position, boolean editable, int width, String cnv) {
        super(id, position, editable, width);
        convention = cnv;
        gcbm = new GradeComboBoxModel(new String[]{convention, "niedersachsen.ersatzeintrag"});
        gcbm.setUseLongLabel(false);
        box = new JXComboBox(gcbm);
        box.setEditable(false);
        box.setRenderer(new DefaultListRenderer(gcbm));
        gcbm.initialize(box);
    }

    @Override
    public void configureTableColumn(AbstractPluggableTableModel<RemoteReportsModel2, ReportData2, ?, ?> model, TableColumnExt col) {
        super.configureTableColumn(model, col);
        col.setCellEditor(new DefaultCellEditor(box));
        col.setCellRenderer(new DefaultTableRenderer(gcbm));
        col.addHighlighter(centerHighlighter);
    }

}
