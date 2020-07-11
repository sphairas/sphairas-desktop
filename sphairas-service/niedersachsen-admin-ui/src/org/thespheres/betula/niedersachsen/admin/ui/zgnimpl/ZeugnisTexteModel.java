/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl;

import com.google.common.eventbus.Subscribe;
import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.niedersachsen.admin.ui.RemoteReportsModel2;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2.TextNote;
import org.thespheres.betula.niedersachsen.zeugnis.ReportProvisionsUtil;

/**
 *
 * @author boris.heithecker
 */
class ZeugnisTexteModel extends AbstractTableModel {

    public static final String PROP_TEMPLATE = "template";
    ReportData2 currentStudent;
    private final NumberFormat nf = NumberFormat.getIntegerInstance(Locale.GERMANY);
    private TextNoteTemplate template = null;
    final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);

    ZeugnisTexteModel() {
        nf.setGroupingUsed(false);
    }

    Optional<ReportData2> getCurrentStudent() {
        return Optional.ofNullable(currentStudent);
    }

    synchronized void setCurrentStudent(final ReportData2 sd) {
        RemoteReportsModel2 before = null;
        if (currentStudent != null) {
            before = currentStudent.getHistory();
        }
        currentStudent = sd;
        RemoteReportsModel2 history = null;
        if (currentStudent != null) {
            history = currentStudent.getHistory();
        }
        if (!Objects.equals(before, history)) {
            if (before != null) {
                before.getEventBus().unregister(this);
            }
            if (history != null) {
                history.getEventBus().register(this);
            }
        }
        resetTemplate();
        EventQueue.invokeLater(this::fireTableDataChanged);
    }

    private void resetTemplate() {
        template = null;
        pSupport.firePropertyChange(PROP_TEMPLATE, template, null);
    }

    @Override
    public synchronized int getRowCount() {
        final int c = getCurrentStudent()
                .map(s -> s.getTextNotes().size())
                .orElse(0);
        return template == null ? c : c + 1;
    }

    public synchronized Optional<TextNote> getCurrentReportNoteAt(final int row) {
        if (isTemplateRow(row)) {
            return Optional.of(template);
        }
        return getCurrentStudent()
                .map(s -> s.getTextNotes().size() > row ? s.getTextNotes().get(row) : null);
    }

    private boolean isTemplateRow(final int row) {
        return template != null && row == getRowCount() - 1;
    }

    @Override
    public synchronized int getColumnCount() {
        return 3;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0
                || columnIndex == 1 && isTemplateRow(rowIndex)
                || columnIndex == 2;
    }

    @Override
    public synchronized Object getValueAt(final int rowIndex, final int columnIndex) {
        final ReportData2 s = getCurrentStudent().orElse(null);
        if (s != null && rowIndex < getRowCount()) {
            final ReportData2.TextNote rn = isTemplateRow(rowIndex) ? template : s.getTextNotes().get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return (ActionListener) e -> removeNote(rn.getKey(), rn.getValue());
                case 1:
                    return ReportProvisionsUtil.getTextFieldLabel(rn.getKey());
                case 2:
                    return rn.getValue();
            }
        }
        return null;
    }

    @Override
    public void setValueAt(final Object value, int rowIndex, int columnIndex) {
        final ReportData2 s = getCurrentStudent().orElse(null);
        if (s != null && rowIndex < getRowCount()) {
            switch (columnIndex) {
                case 0:
                    break;
                case 1:
                    final String text = StringUtils.stripToNull((String) value);
                    template.setKey(text);
                    break;
                case 2:
                    final String v = StringUtils.stripToNull((String) value);
                    if (isTemplateRow(rowIndex)) {
                        template.setValue(v);
                    } else {
                        final ReportData2.TextNote rn = s.getTextNotes().get(rowIndex);
                        s.putTextNote(rn.getKey(), v, rn.getValue());
                    }
                    break;
            }
        }
    }

    private void removeNote(final String key, final String before) {
        getCurrentStudent().ifPresent(s -> {
            EventQueue.invokeLater(() -> s.putTextNote(key, null, before));
        });
    }

    void createTemplate() {
        if (template == null) {
            template = new TextNoteTemplate();
            pSupport.firePropertyChange(PROP_TEMPLATE, null, template);
            EventQueue.invokeLater(() -> fireTableStructureChanged());
        }
    }

    boolean hasTemplate() {
        return template != null;
    }

    boolean isTemplateValid() {
        return hasTemplate() && template.isValid();
    }

    void saveTemplate() {
        getCurrentStudent().ifPresent(s -> {
            EventQueue.invokeLater(() -> {
                if (template != null) {
                    s.putTextNote(template.getKey(), template.getValue(), null);
                    resetTemplate();
                }
            });
        });

    }

    @Subscribe
    public void propertyChange(PropertyChangeEvent evt) {
        if (ReportData2.PROP_REPORT_NOTES.equals(evt.getPropertyName()) && getCurrentStudent().map(evt.getSource()::equals).orElse(false)) {
            EventQueue.invokeLater(this::fireTableDataChanged);
        }
    }

    class TextNoteTemplate extends TextNote {

        TextNoteTemplate() {
            super("key", "value");
        }

        TextNoteTemplate(final TextNoteTemplate orig) {
            super(orig.key, orig.value);
        }

        void setKey(final String key) {
            final TextNoteTemplate before = new TextNoteTemplate(this);
            this.key = key;
            pSupport.firePropertyChange(PROP_TEMPLATE, before, this);
        }

        void setValue(final String value) {
            final TextNoteTemplate before = new TextNoteTemplate(this);
            this.value = value;
            pSupport.firePropertyChange(PROP_TEMPLATE, before, this);
        }

        boolean isValid() {
            return StringUtils.isNotBlank(getKey())
                    && StringUtils.isNotBlank(getValue());
        }

    }

}
