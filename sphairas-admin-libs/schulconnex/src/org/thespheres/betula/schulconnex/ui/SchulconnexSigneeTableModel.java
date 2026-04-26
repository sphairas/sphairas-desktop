/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex.ui;

import java.awt.EventQueue;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.schulconnex.SchulconnexImportData;
import org.thespheres.betula.schulconnex.SchulconnexSigneeItem;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;

/**
 * Table model for Schulconnex signee import review.
 *
 * @author boris.heithecker
 */
final class SchulconnexSigneeTableModel extends ImportTableModel<SchulconnexSigneeItem, SchulconnexImportData<SchulconnexSigneeItem>> {

    private final RequestProcessor rp = new RequestProcessor(SchulconnexTargetsDocumentsTableModel.class);
//    final ImportTableModel.ColFactory columnFactory = new ImportTableModel.ColFactory();
    private final ChangeSupport cSupport = new ChangeSupport(this);
//    private boolean valid;

    SchulconnexSigneeTableModel() {
        super(createColumns());
    }

    private static Set<ImportTableColumn> createColumns() {
        final Set<ImportTableColumn> columns = SchulconnexSigneeTableColumns.create();
        Lookups.forPath("SchulconnexSigneeTableModel/Columns").lookupAll(ImportTableColumn.Factory.class).stream()
                .map(ImportTableColumn.Factory::createInstance)
                .forEach(columns::add);
        return columns;
    }

//    @Override
//    @SuppressWarnings("unchecked")
//    public void initialize(final SchulconnexImportData<SchulconnexSigneeItem> wiz) {
//        set = (ChangeSet<SchulconnexSigneeItem>) wiz.getProperty(AbstractFileImportAction.SELECTED_NODES);
//        selected.clear();
//        selected.addAll(set);
//        selected.forEach(item -> item.addVetoableChangeListener(this));
//        Collections.sort(selected);
//        columnFactory.initialize(wiz.getImportTargetProperty(), wiz);
//        set.addChangeListener(this);
//        setChanged(null);
//        fireTableDataChanged();
//    }
    @Override
    public void initialize(final SchulconnexImportData<SchulconnexSigneeItem> wiz) {
        @SuppressWarnings("unchecked")
        final ChangeSet<SchulconnexSigneeItem> s = (ChangeSet<SchulconnexSigneeItem>) wiz.getProperty(AbstractFileImportAction.SELECTED_NODES);
        final RequestProcessor.Task t = rp.create(() -> {
            EventQueue.invokeLater(() -> {
                selected.clear();
                s.stream()
                        .forEach(selected::add);
                fireTableDataChanged();
            });
        });
        s.addChangeListener(e -> t.schedule(1000));
        t.schedule(1000);
    }

//    @Override
//    public void vetoableChange(final PropertyChangeEvent evt) throws PropertyVetoException {
//        super.vetoableChange(evt);
//        if (ImportSigneeItem.PROP_SELECTED.equals(evt.getPropertyName())) {
//            setChanged(null);
//        }
//    }
//    @Override
//    public void setChanged(final ChangeSet.SetChangeEvent<SchulconnexSigneeItem> e) {
//        final boolean before = valid;
//        valid = !set.stream()
//                .filter(ImportSigneeItem::isSelected)
//                .anyMatch(item -> !item.isValid());
//        if (before != valid) {
//            cSupport.fireChange();
//        }
//    }
//    boolean isValid() {
//        return valid;
//    }
    void addChangeListener(final ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    void removeChangeListener(final ChangeListener listener) {
        cSupport.removeChangeListener(listener);
    }
}
