/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.ui;

import org.thespheres.betula.gpuntis.UntisImportSigneeItem;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.gpuntis.ImportUntisUtil;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;
import org.thespheres.betula.gpuntis.UntisImportData;
import org.thespheres.betula.services.util.SigneeStatus;
import org.thespheres.betula.gpuntis.ui.impl.UntisDefaultColumns;
import org.thespheres.betula.gpuntis.ui.impl.UntisSigneesDefaultColumns;
import org.thespheres.betula.gpuntis.xml.Document;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;

/**
 *
 * @author boris.heithecker
 */
public class UntisSigneeImportTableModel extends ImportTableModel<UntisImportSigneeItem, UntisImportData> implements ChangeSet.Listener<UntisImportSigneeItem> {

    private UntisImportData wizard;
    final ImportTableModel.ColFactory columnFactory = new ImportTableModel.ColFactory();
    private final static Marker STATUS_ACTIVE = MarkerFactory.find(SigneeStatus.NAME, "active", null);
    private ChangeSet<UntisImportSigneeItem> set;
    private final ChangeSupport cSupport = new ChangeSupport(this);
    private boolean valid;

    public UntisSigneeImportTableModel() {
        super(createColumns());
    }

    private static Set<ImportTableColumn> createColumns() {
        String product = NbBundle.getMessage(UntisDefaultColumns.class, "UntisDefaultColumns.product");
        Set<ImportTableColumn> s = UntisSigneesDefaultColumns.create(product);
        Lookups.forPath("UntisSigneeImportVisualPanel/Columns").lookupAll(ImportTableColumn.Factory.class).stream()
                .map(ImportTableColumn.Factory::createInstance)
                .forEach(s::add);
        return s;
    }

    @Override
    public void initialize(UntisImportData wiz) {
        this.wizard = wiz;
        set = (ChangeSet<UntisImportSigneeItem>) wizard.getProperty(AbstractFileImportAction.SELECTED_NODES);
        initSignees();
        ImportTarget configuration = wiz.getImportTargetProperty();
        columnFactory.initialize(configuration, wiz);
        set.addChangeListener(this);
        setChanged(null);
    }

    private void initSignees() {
        final UntisImportConfiguration config = (UntisImportConfiguration) wizard.getProperty(AbstractFileImportAction.IMPORT_TARGET);

        final Document data = (Document) wizard.getProperty(AbstractFileImportAction.DATA);
        final Map<Signee, String> teachers = new HashMap<>();

        data.getTeachers().stream()
                .filter(t -> t.getEmail() != null)
                .forEach(t -> {
                    final String dirName = t.getSurname() + ", " + t.getForename();
                    final Signee sig = ImportUntisUtil.parseEmail(t.getEmail());
                    teachers.put(sig, dirName);
                });
        selected.clear();
        Signees.get(config.getWebServiceProvider().getInfo().getURL())
                .ifPresent(ss -> {
                    ss.getSigneeSet().forEach(sig -> {
                        final UntisImportSigneeItem adapter = new UntisImportSigneeItem(sig, ss.getSignee(sig, false), config, set, true);
                        final String name = teachers.get(sig);
                        adapter.setUserName(name);
                        teachers.remove(sig);
                        adapter.setMarkers((ss.getMarkers(sig)));
                        selected.add(adapter);
                    });
                });

        teachers.entrySet().stream()
                .map(e -> new UntisImportSigneeItem(e.getKey(), e.getValue(), config, set, false))
                .peek(usii -> usii.setStatus(STATUS_ACTIVE))
                .forEach(selected::add);

        Collections.sort(selected);
        fireTableDataChanged();
    }

    @Override
    public void setChanged(ChangeSet.SetChangeEvent<UntisImportSigneeItem> e) {
        boolean before = valid;
        valid = !set.stream()
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
