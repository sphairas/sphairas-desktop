/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import java.util.Collections;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.model.ImportSigneeItem;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;

/**
 *
 * @author boris.heithecker
 */
public class SigneeXmlCsvImportTableModel extends ImportTableModel<SigneeXmlCsvItem, SigneeXmlCsvSettings> implements ChangeSet.Listener<SigneeXmlCsvItem> {

    private SigneeXmlCsvSettings wizard;
    final ImportTableModel.ColFactory columnFactory = new ImportTableModel.ColFactory();
    private ChangeSet<SigneeXmlCsvItem> set;
    private final ChangeSupport cSupport = new ChangeSupport(this);
    private boolean valid;

    public SigneeXmlCsvImportTableModel() {
        super(createColumns());
    }

    private static Set<ImportTableColumn> createColumns() {
//        final String product = NbBundle.getMessage(DataImportSettings.class, "TableImport.product");
        final Set<ImportTableColumn> s = SigneeXmlCsvImportTableModelDefaultColumns.create(Product.NO.getId());
        Lookups.forPath("SigneeXmlCsvImportTableModel/Columns").lookupAll(ImportTableColumn.Factory.class).stream()
                .map(ImportTableColumn.Factory::createInstance)
                .forEach(s::add);
        return s;
    }

    @Override
    public void initialize(final SigneeXmlCsvSettings wiz) {
        this.wizard = wiz;
        set = (ChangeSet<SigneeXmlCsvItem>) wizard.getProperty(AbstractFileImportAction.SELECTED_NODES);
        initSignees();
        ImportTarget configuration = wiz.getImportTargetProperty();
        columnFactory.initialize(configuration, wiz);
        set.addChangeListener(this);
        setChanged(null);
    }

    private void initSignees() {
        selected.clear();
        selected.addAll(set);
        selected.stream()
                .forEach(i -> i.addVetoableChangeListener(this));
        Collections.sort(selected);
        fireTableDataChanged();
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        super.vetoableChange(evt);
        if (ImportSigneeItem.PROP_SELECTED.equals(evt.getPropertyName())) {
            setChanged(null);
        }
    }

    @Override
    public void setChanged(ChangeSet.SetChangeEvent<SigneeXmlCsvItem> e) {
        boolean before = valid;
        valid = !set.stream()
                .filter(ImportSigneeItem::isSelected)
                .anyMatch(uisi -> !uisi.isValid());
        if (before != valid) {
            cSupport.fireChange();
        }
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
}
