/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;
import org.thespheres.betula.tableimport.impl.TargetItemsXmlCsvItem;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns.DefaultConventionColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.MultiSubjectColumn;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"XmlTargetDataDocumentsTableModel.product=Datenbank-Export",
    "XmlTargetDataDocumentsTableModel.columnName.kursart=Kursart"})
class XmlTargetDataDocumentsTableModel extends ImportTableModel<TargetItemsXmlCsvItem, XmlCsvImportSettings<TargetItemsXmlCsvItem>> {

    XmlTargetDataDocumentsTableModel() {
        super(createColumns());
    }

    private static Set<ImportTableColumn> createColumns() {
        final String product = NbBundle.getMessage(XmlTargetDataDocumentsTableModel.class, "XmlTargetDataDocumentsTableModel.product");
        final Set<ImportTableColumn> s = ImportTableModel.createDefault(product);
        s.add(new SelectedColumn());
//        s.add(new NodeColumn(product));
        s.add(new XmlSubjectColumn());
        s.add(new XmlKursartColumn());
        s.add(new XmlConventionColumn());
//        s.add(new UnitColumn(product));
//        s.add(new DeleteDateColumn());
        Lookups.forPath("XmlTargetDataDocumentsTableModel/Columns").lookupAll(ImportTableColumn.Factory.class).stream()
                .map(ImportTableColumn.Factory::createInstance)
                .forEach(s::add);
        return s;
    }

    @Override
    public void initialize(final XmlCsvImportSettings<TargetItemsXmlCsvItem> descriptor) {
        final ConfigurableImportTarget config = (ConfigurableImportTarget) descriptor.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        final ChangeSet<TargetItemsXmlCsvItem> s = (ChangeSet<TargetItemsXmlCsvItem>) descriptor.getProperty(AbstractFileImportAction.SELECTED_NODES);
        selected.clear();
        s.stream()
                .peek(il -> il.initialize(config, descriptor))
                .peek(il -> descriptor.addPropertyChangeListener(createPCL(il, descriptor)))
                .forEach(selected::add);
        fireTableDataChanged();
    }

    private PropertyChangeListener createPCL(final TargetItemsXmlCsvItem il, XmlCsvImportSettings<TargetItemsXmlCsvItem> descriptor) {
        return (PropertyChangeEvent evt) -> {
            if (AbstractFileImportAction.IMPORT_TARGET.equals(evt.getPropertyName())) {
                ConfigurableImportTarget cfg = (ConfigurableImportTarget) evt.getNewValue();
                il.initialize(cfg, descriptor);
            }
        };
    }

    final static class SelectedColumn extends DefaultColumns.DefaultCheckBoxColumn<TargetItemsXmlCsvItem, ConfigurableImportTarget, XmlCsvImportSettings<TargetItemsXmlCsvItem>, XmlTargetDataDocumentsTableModel> {

        SelectedColumn() {
            super("selected", 50);
        }

        @Override
        public Boolean getColumnValue(TargetItemsXmlCsvItem il) {
            return il.isSelected();
        }

        @Override
        public boolean setColumnValue(TargetItemsXmlCsvItem il, Object value) {
            il.setSelected((boolean) value);
            return false;
        }

        @Override
        public String getDisplayName() {
            return "";
        }

    }

    final static class XmlSubjectColumn extends MultiSubjectColumn<TargetItemsXmlCsvItem, ConfigurableImportTarget, XmlCsvImportSettings<TargetItemsXmlCsvItem>, XmlTargetDataDocumentsTableModel> {

        XmlSubjectColumn() {
            super(200, 125);
        }

        @Override
        protected MarkerConvention[] getMarkerConventions(ConfigurableImportTarget configuration) {
            return configuration.getSubjectMarkerConventions();
        }

    }

    final static class XmlKursartColumn extends DefaultColumns.DefaultMarkerColumn<TargetItemsXmlCsvItem, ConfigurableImportTarget, XmlCsvImportSettings<TargetItemsXmlCsvItem>, XmlTargetDataDocumentsTableModel> {

        XmlKursartColumn() {
            super("kursart", 250, 125);
        }

        @Override
        protected MarkerConvention[] getMarkerConventions(ConfigurableImportTarget configuration) {
            return configuration.getRealmMarkerConventions();
        }

        @Override
        public Marker getColumnValue(TargetItemsXmlCsvItem il) {
            return il.getRealm();
        }

        @Override
        public boolean setColumnValue(TargetItemsXmlCsvItem il, Object value) {
            il.getUniqueMarkerSet().add((Marker) value);
            return false;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(XmlTargetDataDocumentsTableModel.class, "XmlTargetDataDocumentsTableModel.columnName.kursart");
        }

    }

    final static class XmlConventionColumn extends DefaultConventionColumn<TargetItemsXmlCsvItem, ConfigurableImportTarget, XmlCsvImportSettings<TargetItemsXmlCsvItem>, XmlTargetDataDocumentsTableModel> {

        XmlConventionColumn() {
            super("convention", 750, 175);
        }

        @Override
        public AssessmentConvention getColumnValue(TargetItemsXmlCsvItem il) {
            return il.getAssessmentConvention();
        }

        @Override
        public boolean setColumnValue(TargetItemsXmlCsvItem il, Object value) {
            il.setAssessmentConvention((AssessmentConvention) value);
            return false;
        }

    }
}
