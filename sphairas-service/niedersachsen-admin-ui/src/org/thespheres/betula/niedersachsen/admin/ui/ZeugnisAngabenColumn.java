/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui;

import java.awt.Toolkit;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.DefaultCellEditor;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.decorator.AlignmentHighlighter;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.niedersachsen.Abschluesse;
import org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.DayColumn;
import org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.KopfnoteColumn;
import org.thespheres.betula.niedersachsen.zeugnis.ZeugnisArt;
import org.thespheres.betula.ui.MarkerComboBoxModel;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.ui.util.PluggableTableColumn;
import org.thespheres.betula.ui.util.WideJXComboBox;

/**
 *
 * @author boris.heithecker
 */
public abstract class ZeugnisAngabenColumn extends PluggableTableColumn<RemoteReportsModel2, ReportData2> {

    protected ZeugnisAngabenColumn(String id, int position, boolean editable, int width) {
        super(id, position, editable, width);
    }

    public static Set<PluggableTableColumn<RemoteReportsModel2, ReportData2>> createDefault() {
        Set<PluggableTableColumn<RemoteReportsModel2, ReportData2>> ret = new HashSet<>();
        ret.add(new StudentsColumn());
        ret.add(new FehltageColumn());
        ret.add(new UnentschuldigtColumn());
        ret.add(new AVColumn());
        ret.add(new SVColumn());
        ret.add(new ArtColumn());
//        ret.add(new SGLColumn());
        ret.add(new BDayColumn());
        ret.add(new BirthplaceColumn());
        return ret;
    }

    @Messages("ZeugnisAngabenColumn.StudentsColumn.displayLabel=Name")
    static class StudentsColumn extends ZeugnisAngabenColumn {

        private final StringValue nameStringValue = v -> v instanceof RemoteStudent ? ((RemoteStudent) v).getDirectoryName() : "";

        StudentsColumn() {
            super("students", 100, false, 150);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(StudentsColumn.class, "ZeugnisAngabenColumn.StudentsColumn.displayLabel");
        }

        @Override
        public Object getColumnValue(ReportData2 il) {
            return il.getRemoteStudent();
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<RemoteReportsModel2, ReportData2, ?, ?> model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            col.setCellRenderer(new DefaultTableRenderer(nameStringValue));
        }

    }

    @Messages("ZeugnisAngabenColumn.FehltageColumn.displayLabel=Fehltage")
    static class FehltageColumn extends DayColumn {

        FehltageColumn() {
            super("fehltage", 1000, true, 40);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(StudentsColumn.class, "ZeugnisAngabenColumn.FehltageColumn.displayLabel");
        }

        @Override
        public Object getColumnValue(ReportData2 il) {
            return il.getFehltage();
        }

        @Override
        public boolean setColumnValue(ReportData2 il, Object value) {
            try {
                il.setFehltage((Integer) value);
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(ZeugnisAngabenColumn.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                Toolkit.getDefaultToolkit().beep();
            }
            return false;
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<RemoteReportsModel2, ReportData2, ?, ?> model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            col.addHighlighter(new InvalidHighlighter(model, ReportData2.PROP_FEHLTAGE));
        }

    }

    @Messages("ZeugnisAngabenColumn.UnentschuldigtColumn.displayLabel=Unentschuldigt")
    static class UnentschuldigtColumn extends DayColumn {

        UnentschuldigtColumn() {
            super("unentschuldigt", 1100, true, 40);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(StudentsColumn.class, "ZeugnisAngabenColumn.UnentschuldigtColumn.displayLabel");
        }

        @Override
        public Object getColumnValue(ReportData2 il) {
            return il.getUnentschuldigt();
        }

        @Override
        public boolean setColumnValue(ReportData2 il, Object value) {
            try {
                il.setUnentschuldigt((Integer) value);
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(ZeugnisAngabenColumn.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                Toolkit.getDefaultToolkit().beep();
            }
            return false;
        }

    }

    @Messages("ZeugnisAngabenColumn.AVColumn.displayLabel=AV")
    static class AVColumn extends KopfnoteColumn {

        AVColumn() {
            super("av", 2000, true, 40, "niedersachsen.arbeitsverhalten");
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(StudentsColumn.class, "ZeugnisAngabenColumn.AVColumn.displayLabel");
        }

        @Override
        public Object getColumnValue(ReportData2 il) {
            return il.getArbeitsverhalten();
        }

        @Override
        public boolean setColumnValue(ReportData2 il, Object value) {
            try {
                il.setArbeitsverhalten((Grade) value);
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(ZeugnisAngabenColumn.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                Toolkit.getDefaultToolkit().beep();
            }
            return false;
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<RemoteReportsModel2, ReportData2, ?, ?> model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            col.addHighlighter(new InvalidHighlighter(model, ReportData2.PROP_ARBEITSVERHALTEN));
        }

    }

    @Messages("ZeugnisAngabenColumn.SVColumn.displayLabel=SV")
    static class SVColumn extends KopfnoteColumn {

        SVColumn() {
            super("av", 2100, true, 40, "niedersachsen.sozialverhalten");
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(StudentsColumn.class, "ZeugnisAngabenColumn.SVColumn.displayLabel");
        }

        @Override
        public Object getColumnValue(ReportData2 il) {
            return il.getSozialverhalten();
        }

        @Override
        public boolean setColumnValue(ReportData2 il, Object value) {
            try {
                il.setSozialverhalten((Grade) value);
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(ZeugnisAngabenColumn.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                Toolkit.getDefaultToolkit().beep();
            }
            return false;
        }

    }

    @Messages("ZeugnisAngabenColumn.ArtColumn.displayLabel=Zeugnisart")
    static class ArtColumn extends ZeugnisAngabenColumn {

        private final JXComboBox box;
        private final MarkerComboBoxModel gcbm;
        private final AlignmentHighlighter centerHighlighter = new AlignmentHighlighter(SwingConstants.CENTER);

        ArtColumn() {
            super("zeugnisart", 3000, true, 40);
            gcbm = new MarkerComboBoxModel(new String[]{ZeugnisArt.CONVENTION_NAME, Abschluesse.CONVENTION_NAME});
            gcbm.setUseLongLabel(false);
            box = new WideJXComboBox(gcbm);
            box.setEditable(false);
            box.setRenderer(new DefaultListRenderer(gcbm));
            gcbm.initialize(box);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(StudentsColumn.class, "ZeugnisAngabenColumn.ArtColumn.displayLabel");
        }

        @Override
        public Object getColumnValue(ReportData2 il) {
            return il.getZeugnisTyp();
        }

        @Override
        public boolean setColumnValue(ReportData2 il, Object value) {
            try {
                il.setZeugnisType((Marker) value);
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(ZeugnisAngabenColumn.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                Toolkit.getDefaultToolkit().beep();
            }
            return false;
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<RemoteReportsModel2, ReportData2, ?, ?> model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            col.setCellEditor(new DefaultCellEditor(box));
            col.setCellRenderer(new DefaultTableRenderer(gcbm));
            col.addHighlighter(centerHighlighter);
        }

    }

    @Messages("ZeugnisAngabenColumn.BDayColumn.displayLabel=Geburtsdatum")
    static class BDayColumn extends ZeugnisAngabenColumn {

        final static DateTimeFormatter DTF = DateTimeFormatter.ofPattern("d. MMM. yyyy");
        private final StringValue dateStringValue = v -> (v instanceof LocalDate) ? ((LocalDate) v).format(DTF) : null;

        BDayColumn() {
            super("birthday", 10000, false, 70);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(StudentsColumn.class, "ZeugnisAngabenColumn.BDayColumn.displayLabel");
        }

        @Override
        public Object getColumnValue(ReportData2 il) {
            return il.getRemoteStudent().getDateOfBirth();
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<RemoteReportsModel2, ReportData2, ?, ?> model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            col.setCellRenderer(new DefaultTableRenderer(dateStringValue));
        }

    }

    @Messages("ZeugnisAngabenColumn.BirthplaceColumn.displayLabel=Geburtsort")
    static class BirthplaceColumn extends ZeugnisAngabenColumn {

        BirthplaceColumn() {
            super("birthplace", 11000, false, 70);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(StudentsColumn.class, "ZeugnisAngabenColumn.BirthplaceColumn.displayLabel");
        }

        @Override
        public Object getColumnValue(ReportData2 il) {
            return il.getRemoteStudent().getBirthplace();
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<RemoteReportsModel2, ReportData2, ?, ?> model, TableColumnExt col) {
            super.configureTableColumn(model, col);
        }

    }

    public static abstract class Factory extends PluggableTableColumn.Factory<PluggableTableColumn<RemoteReportsModel2, ReportData2>> {
    }
}
