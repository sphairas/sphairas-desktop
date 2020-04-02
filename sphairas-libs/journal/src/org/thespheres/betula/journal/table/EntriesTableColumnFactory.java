/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import java.util.Objects;
import javax.swing.DefaultCellEditor;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlignmentHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.journal.JournalConfiguration;
import org.thespheres.betula.services.util.NbUtilities;
import org.thespheres.betula.ui.GradeComboBoxModel;
import org.thespheres.betula.ui.util.WideJXComboBox;

/**
 *
 * @author boris.heithecker
 */
class EntriesTableColumnFactory extends ColumnFactory {

    private final GradeComboBoxModel journalCBModel = new GradeComboBoxModel(new String[0], false);
    private final JXComboBox assesComboBox = new WideJXComboBox(journalCBModel);
//    private ClassSchedule scheme;
//    private final DateFormat df = new SimpleDateFormat("E.', 'dd.MM.", Locale.getDefault());
//    private final DateFormat df2 = new SimpleDateFormat("d.M.", Locale.getDefault());
//    private final Calendar cal = Calendar.getInstance();
    private final Highlighter centerHl = new AlignmentHighlighter(SwingConstants.CENTER);
    private String convention;
    private Lookup context;

    EntriesTableColumnFactory() {
        assesComboBox.setEditable(false);
        journalCBModel.setUseLongLabel(false);
        assesComboBox.setRenderer(new DefaultListRenderer(journalCBModel));
        journalCBModel.initialize(assesComboBox);
    }

    void setEditableCalendar(EditableJournal ej, Lookup ctx) {
        String oc = convention;
        convention = ej != null ? JournalConfiguration.getInstance().getJournalEntryPreferredConvention() : null;
        context = ctx;
        if (!Objects.equals(convention, oc)) {
            if (convention != null) {
                journalCBModel.setConventions(new String[]{convention});
            } else {
                journalCBModel.setConventions(new String[0]);
            }
        }
    }

    @Override
    public void configureColumnWidths(JXTable table, TableColumnExt col) {
        super.configureColumnWidths(table, col);
        int index = col.getModelIndex();
        if (index == 0) {
            col.setPreferredWidth(140);
        } else {
            col.setPreferredWidth(40);
        }
    }

    @Messages({"EntriesTableColumnFactory.columns.name=Name"})
    @Override
    public void configureTableColumn(TableModel m, final TableColumnExt col) {
        super.configureTableColumn(m, col);
        int index = col.getModelIndex();
        if (index == 0) {
            final String msg = NbBundle.getMessage(EntriesTableColumnFactory.class, "EntriesTableColumnFactory.columns.name");
            col.setHeaderValue(msg);
//            col.setCellRenderer(renderer);
        } else {
            EditableRecord r = ((EntriesTableModel) m).getEditableCalendar().getEditableRecords().get(index - 1);
            NbUtilities.waitAndThen(context, JournalEditor.class, e -> {
                final String hv = e.formatLocalDate(r);//NPE;
                col.setHeaderValue(hv);
                final String tt = e.formatLocalDate(r, false);
                col.setToolTipText(tt);
            });
            col.setCellEditor(new DefaultCellEditor(assesComboBox));
            col.setCellRenderer(new DefaultTableRenderer(journalCBModel));
            col.addHighlighter(centerHl);
            col.putClientProperty("record", r);
        }
    }

//    private String date(Date d, Date dbefore) {
//        cal.setTime(d);
//        int doy = cal.get(Calendar.DAY_OF_YEAR);
//        if (dbefore != null) {
//            cal.setTime(dbefore);
//            if (cal.get(Calendar.DAY_OF_YEAR) == doy && scheme != null) {
//                cal.setTime(d);
//                Period p = scheme.getCurrentPeriod(cal);
//                return p.formatTime();
//            }
//        }
//        return df2.format(d);
//    }
//    private String formatDate(Date d, boolean day) {
//        String format = "";
//        if (scheme == null) {
//            format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault()).format(d);
//        } else {
//            cal.setTime(d);
//            Period p = scheme.getCurrentPeriod(cal);
//            if (day) {
//                format = df.format(p.getBegin().getTime()) + ", ";
//            }
//            format += p.formatTime();
//        }
//        return format;
//    }
}
