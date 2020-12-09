/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.ui;

import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.Comparator;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.Tag;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.gpuntis.ImportUntisUtil;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;
import org.thespheres.betula.gpuntis.UntisImportData;
import org.thespheres.betula.gpuntis.ui.UntisSubjectMappingsTableModel.MappedSubjectImportItem;
import org.thespheres.betula.gpuntis.ui.impl.UntisDefaultColumns;
import org.thespheres.betula.gpuntis.xml.Document;
import org.thespheres.betula.gpuntis.xml.Subject;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;

/**
 *
 * @author boris.heithecker
 */
public class UntisSubjectMappingsTableModel extends ImportTableModel<MappedSubjectImportItem, UntisImportData> {

    private UntisImportData wizard;
    final ImportTableModel.ColFactory columnFactory = new ImportTableModel.ColFactory();
    private final ChangeSupport cSupport = new ChangeSupport(this);
    private boolean valid = true;

    public UntisSubjectMappingsTableModel() {
        super(createColumns());
    }

    private static Set<ImportTableColumn> createDefaultSet(String product) {
        final Set<ImportTableColumn> ret = new HashSet<>();
        ret.add(new DefaultColumns.NodeColumn(product));
        ret.add(new MappedSubjectColumn());
        return ret;
    }

    private static Set<ImportTableColumn> createColumns() {
        final String product = NbBundle.getMessage(UntisDefaultColumns.class, "UntisDefaultColumns.product");
        final Set<ImportTableColumn> s = createDefaultSet(product);
        Lookups.forPath("UntisSubjectMappingsVisualPanel/Columns").lookupAll(ImportTableColumn.Factory.class).stream()
                .map(ImportTableColumn.Factory::createInstance)
                .forEach(s::add);
        return s;
    }

    @Override
    public void initialize(final UntisImportData wiz) {
        this.wizard = wiz;
        initSubjects();
        final ImportTarget configuration = wiz.getImportTargetProperty();
        columnFactory.initialize(configuration, wiz);
    }

    private void initSubjects() {
        final UntisImportConfiguration config = (UntisImportConfiguration) wizard.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        final Document data = (Document) wizard.getProperty(AbstractFileImportAction.DATA);

        final List<MappedSubjectImportItem> l = data.getSubjects().stream()
                .map(MappedSubjectImportItem::new)
                .sorted(Comparator.comparing(ImportItem::getSourceNodeLabel, Collator.getInstance(Locale.getDefault())))
                .peek(i -> i.initialize(config))
                .collect(Collectors.toList());

        selected.clear();
        selected.addAll(l);

        fireTableDataChanged();
    }

    boolean isValid() {
        return valid;
    }

    void addChangeListener(ChangeListener l) {
        cSupport.addChangeListener(l);
    }

    void removeChangeListener(ChangeListener l) {
        cSupport.addChangeListener(l);
    }

    static class MappedSubjectImportItem extends ImportItem {

        private final Subject untisSubject;

        MappedSubjectImportItem(final Subject untisSubject) {
            super(untisSubject.getId());
            this.untisSubject = untisSubject;
        }

        void initialize(final UntisImportConfiguration configuration) {
            final Marker m = ImportUntisUtil.findSubjectMarker(configuration, untisSubject);
            if (!Marker.isNull(m)) {
                this.untisSubject.setMappedSubject(m);
            }
        }

        public Subject getUntisSubject() {
            return untisSubject;
        }

        @Override
        public boolean isFragment() {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        public Marker getSubject() {
            return untisSubject.getMappedSubject();
        }

        public void setSubject(final Marker subject) {
            untisSubject.setMappedSubject(subject);
        }

    }

    static class MappedSubjectColumn extends DefaultColumns.DefaultMarkerColumn<MappedSubjectImportItem, UntisImportConfiguration, UntisImportData, UntisSubjectMappingsTableModel> {

        public static final String ID = "subject";
        private WeakReference<MappedSubjectImportItem> current;

        MappedSubjectColumn() {
            super(ID, 200, true, 125, true);
        }

        @Override
        public Object getColumnValue(MappedSubjectImportItem il) {
            current = new WeakReference<>(il);
            return il.getSubject();
        }

        @Override
        public boolean setColumnValue(MappedSubjectImportItem il, Object value) {
            il.setSubject((Marker) value);
            return false;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(DefaultColumns.class, "DefaultColumns.defaultColumnName." + columnId());
        }

        @Override
        public String getString(final Object value) {
            return value instanceof Tag ? ((Tag) value).getLongLabel(current.get()) : " ";
        }

        @Override
        protected MarkerConvention[] getMarkerConventions(final UntisImportConfiguration configuration) {
            return configuration.getSubjectMarkerConventions();
        }
    }

    static class UntisSubjectLongNameColumn extends ImportTableColumn<MappedSubjectImportItem, UntisImportConfiguration, UntisImportData, UntisSubjectMappingsTableModel> {

        public static final String ID = "long-name";

        UntisSubjectLongNameColumn() {
            super(ID, 500, false, 375);
        }

        @Override
        public Object getColumnValue(final MappedSubjectImportItem il) {
            return il.getUntisSubject().getLongName();
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(UntisSubjectMappingsTableModel.class, "UntisSubjectMappingsTableModel.UntisSubjectLongNameColumn.name");
        }

    }
}
