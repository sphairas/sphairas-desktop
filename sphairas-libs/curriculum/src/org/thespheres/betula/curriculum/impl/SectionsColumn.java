/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.decorator.AlignmentHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.Mutex;
import org.thespheres.betula.Convention;
import org.thespheres.betula.curriculum.CourseEntry;
import org.thespheres.betula.curriculum.CourseSelection;
import org.thespheres.betula.curriculum.CourseSelectionValue;
import org.thespheres.betula.curriculum.Curriculum;
import org.thespheres.betula.curriculum.Section;
import org.thespheres.betula.curriculum.util.CurriculumTableActions;
import org.thespheres.betula.curriculum.util.CurriculumUtil;
import org.thespheres.betula.curriculum.DefaultCourseSelectionValue;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.ui.swingx.treetable.NbPluggableSwingXTreeTableModel;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.PluggableTableColumn;
import org.thespheres.betula.ui.util.WideJXComboBox;

/**
 *
 * @author boris.heithecker
 */
class SectionsColumn extends PluggableTableColumn.IndexedColumn<Curriculum, CourseEntry> {

    private final Highlighter centerAlignment = new AlignmentHighlighter(SwingConstants.CENTER);
    private final Formatter formatter = new Formatter();
    private final ConventionComboBoxModelExt optionsModel;
    private final WideJXComboBox combo;
    private final JFormattedTextField tfield;

    SectionsColumn() {
        super("sections", 100, true, 40);
        tfield = new JFormattedTextField(formatter);
        tfield.setBorder(new LineBorder(Color.black, 2));
        tfield.setHorizontalAlignment(JTextField.CENTER);
        optionsModel = new ConventionComboBoxModelExt();
//        optionsModel.setUseLongLabel(config);
        combo = new WideJXComboBox();
        combo.setModel(optionsModel);
        combo.setEditable(true);
        combo.setEditor(new ComboBoxEditorWrapper());
        combo.setRenderer(new DefaultListRenderer(optionsModel));
        optionsModel.initialize(combo);
    }

    protected CurriculumTableActions getActions() {
        return getContext().lookup(CurriculumTableActions.class);
    }

    @Override
    public String getDisplayName(int index) {
        final List<Section> sections = model.getSections();
        return CurriculumUtil.getDisplayName(sections.get(index));
    }

    @Override
    public int getColumnsSize() {
        return model.getSections().size();
    }

    @Override
    public Object getColumnValue(final CourseEntry course, final int index) {
        final List<Section> sections = model.getSections();
        return sections.get(index)
                .getSelection((CourseEntry) course)
                .map(CourseSelection::getCourseSelectionValue)
                .orElse(null);
    }

    @Override
    public boolean isCellEditable(final CourseEntry il, final int index) {
        return true;
    }

    @Override
    public boolean setColumnValue(final CourseEntry course, final int index, final Object value) {
//        final Section section = model.getSections().get(index);
//        final Optional<CourseSelection> selection = section.getSelection(course);
//        final CourseSelectionValue csv = selection
//                .map(CourseSelection::getCourseSelectionValue)
//                .orElse(null);
//        if (csv == null || csv instanceof DefaultCourseSelectionValue) {
//            final DefaultCourseSelectionValue dcsv;
//            if (csv != null) {
//                dcsv = (DefaultCourseSelectionValue) csv;
//            } else {
//                dcsv = new DefaultCourseSelectionValue();
//                selection.ifPresent(s -> s.setCourseSelectionValue(dcsv));
//            }
//            if (value instanceof Integer) {
//                return getActions().setNumLessons(course, section, dcsv, (Integer) value);
//            } else if (value instanceof Marker) {
//                return getActions().setOption(course, section, dcsv, (Marker) value);
//            }
//        }
        return false;
    }

    @Override
    public void configureTableColumn(final NbPluggableSwingXTreeTableModel<Curriculum, CourseEntry> tm, final TableColumnExt col) {
        super.configureTableColumn(tm, col);
        col.setToolTipText(getDisplayName((int) col.getClientProperty(AbstractPluggableTableModel.PROP_COLUMNS_INDEX)));
        col.addHighlighter(centerAlignment);
        final class CellEditor extends DefaultCellEditor {

            @SuppressWarnings({"OverridableMethodCallInConstructor"})
            private CellEditor(JXComboBox editor) {
                super(editor);
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                final DefaultCourseSelectionValue current = value instanceof DefaultCourseSelectionValue ? (DefaultCourseSelectionValue) value : null;
                updateModel(current);
                return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }

        }
        col.setCellRenderer(new DefaultTableRenderer(formatter));
        col.setCellEditor(new CellEditor(combo));
    }

    private void updateModel(DefaultCourseSelectionValue current) {
        final Convention[] cvns = current != null ? current.getOptionsConventions(getContext()) : null;
        if (cvns != null) {
            Mutex.EVENT.postWriteRequest(() -> {
                optionsModel.setConventions(cvns);
            });
        }
    }

    class ComboBoxEditorWrapper implements ComboBoxEditor {

        @Override
        public Component getEditorComponent() {
            return tfield;
        }

        @Override
        public void setItem(Object value) {
            tfield.setValue(value);
        }

        @Override
        public Object getItem() {
            return tfield.getValue();
        }

        @Override
        public void selectAll() {
            tfield.selectAll();
        }

        @Override
        public void addActionListener(ActionListener l) {
            tfield.addActionListener(l);
        }

        @Override
        public void removeActionListener(ActionListener l) {
            tfield.addActionListener(l);
        }

    }

    class Formatter extends PositivIntegerOrMarkerFormatter implements StringValue {

        @Override
        public String valueToString(Object value) {
            if (value instanceof Number) {
                return NF.format(value);
            } else if (value instanceof Marker) {
                return ((Marker) value).getLongLabel();
            } else if (value instanceof CourseSelectionValue) {
                return ((CourseSelectionValue) value).toString(getContext());
            }
            return value != null ? value.toString() : "null";
        }

        @Override
        public String getString(Object value) {
            return valueToString(value);
        }

    }
}
