/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.NbBundle;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;
import org.thespheres.betula.tableimport.impl.PrimaryUnitsXmlCsvItem;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.UnitColumn;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"XmlPrimaryUnitDataDocumentsTableModel.product=Datenbank-Export",
    "XmlPrimaryUnitDataDocumentsTableModel.columnName.kursart=Kursart"})
class XmlPrimaryUnitDataDocumentsTableModel extends ImportTableModel<PrimaryUnitsXmlCsvItem, XmlCsvImportSettings<PrimaryUnitsXmlCsvItem>> {

    XmlPrimaryUnitDataDocumentsTableModel() {
        super(createColumns());
    }

    private static Set<ImportTableColumn> createColumns() {
        final String product = NbBundle.getMessage(XmlPrimaryUnitDataDocumentsTableModel.class, "XmlPrimaryUnitDataDocumentsTableModel.product");
        final HashSet<ImportTableColumn> ret = new HashSet<>();
        ret.add(new DefaultColumns.NodeColumn(product));
        ret.add(new UnitColumn(product));
        ret.add(new DefaultColumns.UnitDisplayColumn(product));
        ret.add(new DefaultColumns.DeleteDateColumn());
        ret.add(new SelectedColumn());
        return ret;
    }

    @Override
    public void initialize(final XmlCsvImportSettings<PrimaryUnitsXmlCsvItem> descriptor) {
        final ConfigurableImportTarget config = (ConfigurableImportTarget) descriptor.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        final ChangeSet<PrimaryUnitsXmlCsvItem> s = (ChangeSet<PrimaryUnitsXmlCsvItem>) descriptor.getProperty(AbstractFileImportAction.SELECTED_NODES);
        selected.clear();
        s.stream()
                .peek(il -> il.initialize(config, descriptor))
                .peek(il -> descriptor.addPropertyChangeListener(createPCL(il, descriptor)))
                .forEach(selected::add);
        fireTableDataChanged();
    }

    private PropertyChangeListener createPCL(final PrimaryUnitsXmlCsvItem il, XmlCsvImportSettings<PrimaryUnitsXmlCsvItem> descriptor) {
        return (PropertyChangeEvent evt) -> {
            if (AbstractFileImportAction.IMPORT_TARGET.equals(evt.getPropertyName())) {
                final ConfigurableImportTarget cfg = (ConfigurableImportTarget) evt.getNewValue();
                il.initialize(cfg, descriptor);
            }
        };
    }

    final static class SelectedColumn extends DefaultColumns.DefaultCheckBoxColumn<PrimaryUnitsXmlCsvItem, ConfigurableImportTarget, XmlCsvImportSettings<PrimaryUnitsXmlCsvItem>, XmlPrimaryUnitDataDocumentsTableModel> {

        SelectedColumn() {
            super("selected", 50);
        }

        @Override
        public Boolean getColumnValue(PrimaryUnitsXmlCsvItem il) {
            return il.isSelected();
        }

        @Override
        public boolean setColumnValue(PrimaryUnitsXmlCsvItem il, Object value) {
            il.setSelected((boolean) value);
            return false;
        }

        @Override
        public String getDisplayName() {
            return "";
        }

    }
}
