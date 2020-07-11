/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl;

import com.google.common.eventbus.Subscribe;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import javax.swing.table.AbstractTableModel;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.niedersachsen.admin.ui.RemoteReportsModel2;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2.ReportNote;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
class ZeugnisBemerkungModel extends AbstractTableModel {

    ReportData2 currentStudent;
    private final NumberFormat nf = NumberFormat.getIntegerInstance(Locale.GERMANY);
    private final ZeugnisMarkerTopComponent outer;

    ZeugnisBemerkungModel(final ZeugnisMarkerTopComponent outer) {
        this.outer = outer;
        nf.setGroupingUsed(false);
    }

    Optional<ReportData2> getCurrentStudent() {
        return Optional.ofNullable(currentStudent);
    }

    synchronized void setCurrentStudent(ReportData2 sd) {
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
        outer.cbm.setStudent(currentStudent);
        EventQueue.invokeLater(this::fireTableDataChanged);
    }

    @Override
    public synchronized int getRowCount() {
        return getCurrentStudent()
                .map(s -> s.getBemerkungen().size())
                .orElse(0);
    }

    public synchronized Optional<ReportNote> getCurrentReportNoteAt(final int row) {
        return getCurrentStudent()
                .map(s -> s.getBemerkungen().size() > row ? s.getBemerkungen().get(row) : null);
    }

    @Override
    public synchronized int getColumnCount() {
        return 3;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    @Override
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
        final ReportData2 s = getCurrentStudent().orElse(null);
        if (s != null && rowIndex < s.getBemerkungen().size()) {
            final ReportData2.ReportNote rn = s.getBemerkungen().get(rowIndex);
            final int pos = rn.getPosition();
            final Object v = rn.getValue();
            if (v instanceof Marker) {
                final Marker m = (Marker) v;
                switch (columnIndex) {
                    case 0:
                        return (ActionListener) e -> removeMarker(m);
                    case 1:
                        return getFormattedBem(m);
                    case 2:
                        return nf.format(pos);
                }
            } else if (v instanceof String) {
                final String text = (String) v;
                switch (columnIndex) {
                    case 0:
                        return (ActionListener) e -> removeNote(pos, text);
                    case 1:
                        return text;
                    case 2:
                        return nf.format(pos);
                }
            }
        }
        return null;
    }

    private void removeMarker(final Marker m) {
        getCurrentStudent().ifPresent(s -> {
            EventQueue.invokeLater(() -> {
                try {
                    s.removeMarker(m);
                    //TODO: fireTableDataChanged ????
                } catch (IOException ex) {
                    PlatformUtil.getCodeNameBaseLogger(ZeugnisMarkerTopComponent.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    Toolkit.getDefaultToolkit().beep();
                }
            });
        });
    }

    void addMarker(Marker m) {
        getCurrentStudent().ifPresent(s -> {
            try {
                s.addMarker(m);
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(ZeugnisMarkerTopComponent.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                Toolkit.getDefaultToolkit().beep();
            }
        });
    }

    void addNote(final String text, final int position) {
        getCurrentStudent().ifPresent(s -> {
            try {
                s.addFreieBemerkung(position, text);
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(ZeugnisMarkerTopComponent.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                Toolkit.getDefaultToolkit().beep();
            }
        });
    }

    private void removeNote(final int pos, final String before) {
        getCurrentStudent().ifPresent(s -> {
            EventQueue.invokeLater(() -> {
                try {
                    s.removeFreieBemerkung(pos, before);
                } catch (IOException ex) {
                    PlatformUtil.getCodeNameBaseLogger(ZeugnisMarkerTopComponent.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    Toolkit.getDefaultToolkit().beep();
                }
            });
        });
    }

    private String getFormattedBem(Object o) {
        if (o instanceof Marker) {
            final Marker marker = (Marker) o;
            return getCurrentStudent().map(s -> marker.getLongLabel(s.getFormatArgs())).orElse(marker.getLongLabel());
        }
        return "";
    }

    @Subscribe
    public void propertyChange(PropertyChangeEvent evt) {
        if (ReportData2.PROP_REPORT_NOTES.equals(evt.getPropertyName()) && getCurrentStudent().map(evt.getSource()::equals).orElse(false)) {
            EventQueue.invokeLater(this::fireTableDataChanged);
        }
    }

}
