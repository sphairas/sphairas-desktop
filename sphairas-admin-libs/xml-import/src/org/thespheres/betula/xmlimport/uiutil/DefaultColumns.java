/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyVetoException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Stream;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.text.DateFormatter;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.thespheres.betula.Tag;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.ui.swingx.SigneeConverter;
import org.thespheres.betula.services.CommonTargetProperties;
import org.thespheres.betula.ui.util.WideJXComboBox;
import org.thespheres.betula.xmlimport.Constants;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportTargetsItem;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <T>
 * @param <W>
 * @param <M>
 */
@NbBundle.Messages({"DefaultColumns.defaultColumnName.node=Gruppe/Kurs ({0})",
    "DefaultColumns.defaultColumnName.unit=Gruppe",
    "DefaultColumns.defaultColumnName.unitDisplayName=Gruppe (Anzeigename)",
    "DefaultColumns.defaultColumnName.documentBase=Liste (Basis-Name)",
    "DefaultColumns.defaultColumnName.sourceSignee=Lehrer ({0})",
    "DefaultColumns.defaultColumnName.signee=Unterzeichner",
    "DefaultColumns.defaultColumnName.deleteDate=Löschdatum",
    "DefaultColumns.defaultColumnName.subject=Fach",
    "DefaultColumns.defaultColumnName.convention=Notensystem"})
public abstract class DefaultColumns<I extends ImportItem, T extends ImportTarget, W extends AbstractImportWizardSettings<T>, M extends ImportTableModel<I, W>> extends ImportTableColumn<I, T, W, M> {

    protected final String product;

    public DefaultColumns(String id, int position, boolean editable, int width, String product) {
        super(id, position, editable, width);
        this.product = product;
    }

    static Set<ImportTableColumn> createDefaultSet(String product) {
        final Set<ImportTableColumn> ret = new DefaultColumnSet();
        ret.add(new NodeColumn(product));
        ret.add(new UnitColumn(product));
        ret.add(new UnitDisplayColumn(product));
        ret.add(new DocumentBaseColumn(product));
        ret.add(new SourceSigneeColumn(product));
        ret.add(new SigneeColumn(product));
        ret.add(new DeleteDateColumn());
        return ret;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(DefaultColumns.class, "DefaultColumns.defaultColumnName." + columnId(), product);
    }

    public static class NodeColumn<I extends ImportItem, T extends ImportTarget, W extends AbstractImportWizardSettings<T>, M extends ImportTableModel<I, W>> extends DefaultColumns<I, T, W, M> {

        public static final String ID = "node";

        public NodeColumn(String product) {
            super(ID, 100, false, 125, product);
        }

        @Override
        public Object getColumnValue(ImportItem il) {
            return il;
        }

        @Override
        public void configureTableColumn(M model, TableColumnExt col) {
            col.setCellRenderer(new DefaultTableRenderer(v -> ((ImportItem) v).getSourceNodeLabel()));
            col.addHighlighter(new ColorHighlighter(Color.LIGHT_GRAY, null));
        }

    }

    public static class UnitDisplayColumn<I extends ImportTargetsItem, T extends ImportTarget, W extends AbstractImportWizardSettings<T>, M extends ImportTableModel<I, W>> extends DefaultColumns<I, T, W, M> {

        public static final String ID = "unitDisplayName";

        public UnitDisplayColumn(String product) {
            super(ID, 400, false, 125, product);
        }

        @Override
        public String getColumnValue(final ImportTargetsItem il) {
            return il.getUnitDisplayName();
        }

        @Override
        public boolean isCellEditable(I il) {
            if (il.importUnitDisplayName()) {
                return true;
            }
            return super.isCellEditable(il);

        }

        @Override
        public boolean setColumnValue(final I il, final Object v) {
            if (v instanceof String || v == null) {
                final String name = StringUtils.trimToNull((String) v);
                try {
                    il.setClientProperty(Constants.PROP_USER_UNIT_DISPLAYNAME, name);
                    return true;
                } catch (PropertyVetoException pvex) {
                    return false;
                }
            }
            return false;
        }

        @Override
        public void configureTableColumn(M model, TableColumnExt col) {
            class UnresolvedNamePredicate implements HighlightPredicate {

                @Override
                public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                    M m = (M) ((JTable) adapter.getComponent()).getModel();
                    int r = adapter.convertRowIndexToModel(adapter.row);
                    if (r < m.selected.size()) {
                        final ImportTargetsItem kurs = m.selected.get(r);
                        final String dN = kurs.getUnitDisplayName();
                        final UnitId uid = kurs.getUnitId();
                        return dN != null && uid != null && dN.equals(uid.getId());
                    }
                    return false;
                }
            }
            col.addHighlighter(new ColorHighlighter(new UnresolvedNamePredicate(), null, Color.RED));
        }

    }

    public static class SourceSigneeColumn<I extends ImportTargetsItem, T extends ImportTarget, W extends AbstractImportWizardSettings<T>, M extends ImportTableModel<I, W>> extends DefaultColumns<I, T, W, M> {

        public static final String ID = "sourceSignee";

        public SourceSigneeColumn(String product) {
            super(ID, 600, false, 80, product);
        }

        @Override
        public Object getColumnValue(ImportTargetsItem il) {
            return il.getSourceSigneeName();
        }

        @Override
        public void configureTableColumn(M model, TableColumnExt col) {
            col.addHighlighter(new ColorHighlighter(Color.LIGHT_GRAY, null));
        }

    }

    public static class SigneeColumn<I extends ImportTargetsItem, T extends ImportTarget, W extends AbstractImportWizardSettings<T>, M extends ImportTableModel<I, W>> extends DefaultColumns<I, T, W, M> {

        public static final String ID = "signee";
        protected final JXComboBox signeeBox;
        protected SigneeConverter sigConv;

        public SigneeColumn(String product) {
            super(ID, 700, true, 170, product);
            signeeBox = new JXComboBox();
        }

        @Override
        public void initialize(T configuration, W wizard) {
            sigConv = new SigneeConverter(configuration.getWebServiceProvider().getInfo().getURL(), "---");
            Vector<Signee> sigV = new Vector();
            sigV.add(null);
            Signees.get(configuration.getWebServiceProvider().getInfo().getURL())
                    .map(Signees::getSigneeSet)
                    .ifPresent(sigV::addAll);
            AutoCompleteDecorator.decorate(signeeBox, sigConv);
            signeeBox.setRenderer(new DefaultListRenderer(sigConv));
            signeeBox.setEditable(false);
            signeeBox.setModel(new DefaultComboBoxModel(sigV));
        }

        @Override
        public Object getColumnValue(ImportTargetsItem il) {
            return il.getSignee();
        }

        @Override
        public boolean setColumnValue(ImportTargetsItem il, Object value) {
            il.setSignee((Signee) value);
            return true;
        }

        @Override
        public void configureTableColumn(M model, TableColumnExt col) {
            col.setCellEditor(new DefaultCellEditor(signeeBox));
            col.setCellRenderer(new DefaultTableRenderer(sigConv));
        }

    }

    public static class DeleteDateColumn<I extends ImportItem, T extends ImportTarget, W extends AbstractImportWizardSettings<T>, M extends ImportTableModel<I, W>> extends DefaultColumns<I, T, W, M> implements StringValue {

        public static final String ID = "deleteDate";
        protected final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMANY);
        protected final DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);

        public DeleteDateColumn() {
            super(ID, 800, true, 125, null);
        }

        @Override
        public LocalDate getColumnValue(final I il) {
            return il.getDeleteDate();
        }

        @Override
        public boolean setColumnValue(final I il, final Object value) {
            final Date d = (Date) value;
            if (d != null) {
                il.setDeleteDate(LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault()).toLocalDate());
            }
            return true;
        }

        @Override
        public void configureTableColumn(M model, TableColumnExt col) {
            final JFormattedTextField tfield = new JFormattedTextField(new DateFormatter(dateFormat)) {
                @Override
                public void setValue(final Object value) {
                    final Date d = Optional.ofNullable(value)
                            .filter(LocalDate.class::isInstance)
                            .map(LocalDate.class::cast)
                            .map(ld -> Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                            .orElse(null);
                    super.setValue(d);
                }            
            };
            tfield.setBorder(new LineBorder(Color.black, 2));
            class CellEditor extends DefaultCellEditor {

                private CellEditor() {
                    super(tfield);
                    tfield.removeActionListener(delegate);
                    delegate = new DefaultCellEditor.EditorDelegate() {
                        @Override
                        public void setValue(Object value) {
                            try {
                                tfield.setValue(value);
                            } catch (IllegalArgumentException e) {
                            }
                        }

                        @Override
                        public Object getCellEditorValue() {
                            return tfield.getValue();
                        }
                    };
                    tfield.addActionListener(delegate);
                }
            }
            col.setCellEditor(new CellEditor());
            col.setCellRenderer(new DefaultTableRenderer(this));
        }

        @Override
        public String getString(Object value) {
            if (value instanceof LocalDate) {
                return dateFormatter.format((LocalDate) value);
            }
            return "";
        }

    }

    public static abstract class DefaultCheckBoxColumn<I extends ImportItem, T extends ImportTarget, W extends AbstractImportWizardSettings<T>, M extends ImportTableModel<I, W>> extends ImportTableColumn<I, T, W, M> {

        protected final JCheckBox box;

        protected DefaultCheckBoxColumn(String id, int position) {
            super(id, position, true, 16);
            box = new JCheckBox();
        }

        @Override
        public void configureTableColumn(M model, TableColumnExt col) {
            col.setCellEditor(new DefaultCellEditor(box));
            col.setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
            col.setMinWidth(getPreferredWidth());
            col.setMaxWidth(getPreferredWidth());
        }

    }

    public static abstract class DefaultTagColumn<I extends ImportItem, T extends ImportTarget, W extends AbstractImportWizardSettings<T>, M extends ImportTableModel<I, W>, C extends Tag> extends ImportTableColumn<I, T, W, M> implements StringValue {

        protected final JXComboBox box;

        protected DefaultTagColumn(String id, int position, boolean editable, int width) {
            super(id, position, editable, width);
            box = new WideJXComboBox();
            box.setRenderer(new DefaultListRenderer(this));
            box.setEditable(false);
        }

        protected DefaultTagColumn(String id, int position, int width) {
            this(id, position, true, width);
        }

        @Override
        public void initialize(T configuration, W wizard) {
            final Tag[] elements = getTags(configuration);
            final DefaultComboBoxModel fcbm = new DefaultComboBoxModel(elements);
            box.setModel(fcbm);
        }

        protected abstract C[] getTags(T configuration);

        @Override
        public void configureTableColumn(M model, TableColumnExt col) {
            col.setCellEditor(new DefaultCellEditor(box));
            col.setCellRenderer(new DefaultTableRenderer(this));
        }

        @Override
        public String getString(Object value) {
            return value instanceof Tag ? ((Tag) value).getLongLabel() : " ";
        }

    }

    public static abstract class DefaultEnumColumn<I extends ImportItem, T extends ImportTarget, W extends ImportWizardSettings, M extends ImportTableModel<I, W>> extends ImportTableColumn<I, T, W, M> {

        protected final JXComboBox box;
        private final StringValue stringValue = (value) -> value instanceof Enum ? ((Enum) value).toString() : null;
        private final Enum[] enums;

        protected DefaultEnumColumn(Enum[] e, String id, int position, boolean editable, int width) {
            super(id, position, editable, width);
            this.enums = e;
            box = new JXComboBox();
            box.setRenderer(new DefaultListRenderer(stringValue));
            box.setEditable(false);
        }

        protected DefaultEnumColumn(Enum[] e, String id, int position, int width) {
            this(e, id, position, true, width);
        }

        @Override
        public void initialize(T configuration, W wizard) {
            DefaultComboBoxModel fcbm = new DefaultComboBoxModel(getEnum());
            box.setModel(fcbm);
        }

        protected Enum[] getEnum() {
            return this.enums;
        }

        @Override
        public void configureTableColumn(M model, TableColumnExt col) {
            col.setCellEditor(new DefaultCellEditor(box));
            col.setCellRenderer(new DefaultTableRenderer(stringValue));
        }

    }

    public static abstract class DefaultMarkerColumn<I extends ImportItem, T extends ImportTarget, W extends AbstractImportWizardSettings<T>, M extends ImportTableModel<I, W>> extends DefaultTagColumn<I, T, W, M, Marker> {

        protected boolean prependNull;

        protected DefaultMarkerColumn(String id, int position, int width) {
            super(id, position, width);
        }

        protected DefaultMarkerColumn(String id, int position, boolean editable, int width) {
            this(id, position, editable, width, false);
        }

        protected DefaultMarkerColumn(String id, int position, boolean editable, int width, boolean prependNull) {
            super(id, position, editable, width);
            this.prependNull = prependNull;
        }

        @Override
        protected Marker[] getTags(T configuration) {
            final MarkerConvention[] conventions = getMarkerConventions(configuration);
            final Stream<Marker> prepend = prependNull ? Stream.of((Marker) null) : Stream.empty();
            if (conventions != null) {
                final Stream<Marker> markers = Arrays.stream(conventions)
                        .flatMap(mc -> Arrays.stream(mc.getAllMarkers())
                        .sorted(Comparator.comparing(Marker::getLongLabel)));
                return Stream.concat(prepend, markers)
                        .toArray(Marker[]::new);

            } else {
                return new Marker[0];
            }
        }

        protected abstract MarkerConvention[] getMarkerConventions(T configuration);

    }

    public static abstract class DefaultSubjectColumn<I extends ImportTargetsItem, T extends ImportTarget, W extends AbstractImportWizardSettings<T>, M extends ImportTableModel<I, W>> extends DefaultMarkerColumn<I, T, W, M> {

        public static final String ID = "subject";
        private WeakReference<I> current;

        protected DefaultSubjectColumn(int position, int width) {
            super(ID, position, width);
        }

        @Override
        public Object getColumnValue(I il) {
            current = new WeakReference<>(il);
            return il.getSubjectMarker();
        }

        @Override
        public boolean setColumnValue(I il, Object value) {
            il.setSubjectMarker((Marker) value);
            return false;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(DefaultColumns.class, "DefaultColumns.defaultColumnName." + columnId());
        }

        @Override
        public String getString(Object value) {
            return value instanceof Tag ? ((Tag) value).getLongLabel(current.get()) : " ";
        }
    }

    public static abstract class DefaultConventionColumn<I extends ImportItem, T extends ImportTarget & CommonTargetProperties, W extends AbstractImportWizardSettings<T>, M extends ImportTableModel<I, W>> extends DefaultColumns<I, T, W, M> {

        protected final JXComboBox box;
        private final StringValue stringValue = (value) -> value instanceof AssessmentConvention ? ((AssessmentConvention) value).getDisplayName() : null;

        protected DefaultConventionColumn(String id, int position, int width) {
            super(id, position, true, width, null);
            box = new JXComboBox();
            box.setRenderer(new DefaultListRenderer(stringValue));
            box.setEditable(false);
        }

        @Override
        public void initialize(T configuration, W wizard) {
            final AssessmentConvention[] elements = getConventions(configuration);
            final DefaultComboBoxModel fcbm = new DefaultComboBoxModel(elements);
            box.setModel(fcbm);
        }

        protected AssessmentConvention[] getConventions(T configuration) {
            return configuration.getAssessmentConventions();
        }

        @Override
        public void configureTableColumn(M model, TableColumnExt col) {
            col.setCellEditor(new DefaultCellEditor(box));
            col.setCellRenderer(new DefaultTableRenderer(stringValue));
        }
    }
}
