/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.swing.DefaultCellEditor;
import javax.swing.SwingConstants;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.AlignmentHighlighter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.NumberEditorExt;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.journal.util.JournalTableColumn;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;

/**
 *
 * @author boris.heithecker
 */
@Messages({"DefaultColumns.columnName.date=Datum/Stunde",
    "DefaultColumns.columnName.text=Protokoll",
    "DefaultColumns.columnName.weight=Gewichtung"})
class DefaultColumns {
    
    private DefaultColumns() {
    }
    
    static Set<JournalTableColumn> create() {
        HashSet<JournalTableColumn> ret = new HashSet<>();
        ret.add(new DateColumn());
        ret.add(new TextColumn());
        ret.add(new WeightColumn());
        return ret;
    }
    
    private static String displayName(String colid) {
        final String key = "DefaultColumns.columnName." + colid;
        return NbBundle.getMessage(DefaultColumns.class, key);
    }
    
    static class DateColumn extends JournalTableColumn {
        
        DateColumn() {
            super("date", 100, false, 115);
        }
        
        @Override
        public String getDisplayName() {
            return displayName(columnId());
        }
        
        @Override
        public Object getColumnValue(EditableRecord ri) {
            return context.lookup(JournalEditor.class).formatLocalDate(ri);
        }
        
    }
    
    static class TextColumn extends JournalTableColumn {
        
        TextColumn() {
            super("text", 200, true, 325);
        }
        
        @Override
        public String getDisplayName() {
            return displayName(columnId());
        }
        
        @Override
        public Object getColumnValue(EditableRecord il) {
            return il.getListingText();
        }
        
        @Override
        public boolean setColumnValue(EditableRecord il, Object value) {
            if (!(value instanceof String)) {
                throw new IllegalArgumentException("Not a String value.");
            }
            final String text = StringUtils.trimToNull((String) value);
            if (!Objects.equals(il.getListing(), value)) {
                il.setListing(text);
            }
            return false;
        }
        
        @Override
        public void configureTableColumn(AbstractPluggableTableModel<EditableJournal<?, ?>, EditableRecord<?>, ?, ?> model, TableColumnExt col) {
//            JTextField tfield = new JTextField();
//            tfield.setBorder(new LineBorder(Color.black, 2));
//            col.setCellEditor(new DefaultCellEditor(tfield));
            final TextAreaProvider textAreaProvider = new TextAreaProvider();
            col.setCellEditor(new TextAreaCellEditor(textAreaProvider.positionsMap));
            col.setCellRenderer(new DefaultTableRenderer(textAreaProvider));
        }
        
    }
    
    static class WeightColumn extends JournalTableColumn {
        
        private final DefaultCellEditor int2editor = new NumberEditorExt(DecimalFormat.getNumberInstance());
        private final StringValue weightStringValue = o -> o instanceof Double ? FormatUtil.WEIGHT_NF.format((Double) o) : "";
        
        WeightColumn() {
            super("weight", 300, true, 35);
        }
        
        @Override
        public String getDisplayName() {
            return displayName(columnId());
        }
        
        @Override
        public Object getColumnValue(EditableRecord il) {
            return il.getWeight();
        }
        
        @Override
        public boolean setColumnValue(EditableRecord il, Object value) {
            Double weight = (Double) value;
            il.setWeight(weight);
            return false;
        }
        
        @Override
        public void configureTableColumn(AbstractPluggableTableModel<EditableJournal<?, ?>, EditableRecord<?>, ?, ?> model, TableColumnExt col) {
            col.addHighlighter(new AlignmentHighlighter(SwingConstants.CENTER));
            col.setCellRenderer(new DefaultTableRenderer(new TopLabelProvider(weightStringValue)));//TopLabelPro funktioniert nicht
            col.setCellEditor(int2editor);
        }
    }
    
}
