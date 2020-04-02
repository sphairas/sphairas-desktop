/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.ui2;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.thespheres.betula.sibank.SiBankImportData;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.SiBankKlasseItem;
import org.thespheres.betula.sibank.ui2.impl.SiBankDefaultColumns;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.UnitColumn;

/**
 *
 * @author boris.heithecker
 */
public class SiBankCreateKlasseDocumentsTableModel extends ImportTableModel<SiBankKlasseItem, SiBankImportData<SiBankKlasseItem>> {

//    private SiBankSourceOverrides overrides;
    public SiBankCreateKlasseDocumentsTableModel() {
        super(createColumns());
    }

    @NbBundle.Messages({"SiBankCreateKlasseDocumentsTableModel.unitColumn=Klasse"})
    private static Set<ImportTableColumn> createColumns() {
        String product = NbBundle.getMessage(SiBankDefaultColumns.class, "SiBankDefaultColumns.product");
        final HashSet<ImportTableColumn> ret = new HashSet<>();
        ret.add(new DefaultColumns.NodeColumn(product));
        ret.add(new UnitColumn(product) {
            @Override
            public String getDisplayName() {
                return NbBundle.getMessage(SiBankCreateKlasseDocumentsTableModel.class, "SiBankCreateKlasseDocumentsTableModel.unitColumn");
            }
        });
        ret.add(new DefaultColumns.UnitDisplayColumn(product));
//        ret.add(new DefaultColumns.DocumentbaseColumn(product));
//        ret.add(new DefaultColumns.SourceSigneeColumn(product));
//        ret.add(new DefaultColumns.SigneeColumn(product));
        ret.add(new DefaultColumns.DeleteDateColumn());
//        s.addAll(SiBankDefaultColumns.create(product));
//        Lookups.forPath("SiBankCreateDocumentsVisualPanel/Columns").lookupAll(ImportTableColumn.Factory.class).stream()
//                .map(ImportTableColumn.Factory::createInstance)
//                .forEach(s::add);
        return ret;
    }

    @Override
    public void initialize(final SiBankImportData<SiBankKlasseItem> descriptor) {
//        final SiBankImportTarget config = (SiBankImportTarget) descriptor.getProperty(AbstractFileImportAction.IMPORT_TARGET);
//        ChangeSet<SiBankKursItem> s = (ChangeSet<SiBankKursItem>) descriptor.getProperty(AbstractFileImportAction.SELECTED_NODES);
//        selected.clear();
//        s.stream()
//                .peek(il -> il.initialize(config, descriptor))//TODO: remove initializ 2* ?????
//                .peek(il -> descriptor.addPropertyChangeListener(createPCL(il, descriptor)))
//                .forEach(selected::add);
//        fireTableDataChanged();

//        configuration = (SiBankImportTarget) descriptor.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        final SiBankImportTarget config = descriptor.getImportTargetProperty();
        final ChangeSet<SiBankKlasseItem> nodes = descriptor.getSelectedNodesProperty();

        selected.clear();
        nodes.stream()
                .peek(il -> il.initialize(config, descriptor))//TODO: remove initializ 2* ?????
                .peek(il -> descriptor.addPropertyChangeListener(createPCL(il, descriptor)))
                .forEach(selected::add);

        Mutex.EVENT.writeAccess(this::fireTableDataChanged);

//        findRemoteLookup().getRequestProcessor().post(this);
    }

    private PropertyChangeListener createPCL(final SiBankKlasseItem il, final SiBankImportData wiz) {
        return (PropertyChangeEvent evt) -> {
            if (AbstractFileImportAction.IMPORT_TARGET.equals(evt.getPropertyName())) {
                SiBankImportTarget cfg = (SiBankImportTarget) evt.getNewValue();
                il.initialize(cfg, wiz);
            }
        };
    }

    @Override
    public ColFactory createColumnFactory(final SiBankImportData<SiBankKlasseItem> wizard) {
        SiBankColFactory ret = new SiBankColFactory();
        SiBankImportTarget configuration = (SiBankImportTarget) wizard.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        ret.initialize(configuration, wizard);
        //Init SourceUserOverrides here, not in UntisCreateDocumentsTableModel.initialize
        //Needs to be ready ealier for column configuration
//        overrides = (SiBankSourceOverrides) wizard.getProperty(AbstractSourceOverrides.USER_SOURCE_OVERRIDES);
        return ret;
    }

    private final class SiBankColFactory extends ColFactory {

        private SiBankColFactory() {
        }

//        @Override
//        public void configureTableColumn(TableModel model, TableColumnExt col) {
//            super.configureTableColumn(model, col);
//            if (overrides != null) {
//                col.addHighlighter(overrides.createHighlighter());
//            } else {
//                throw new IllegalStateException();
//            }
//        }
    }
}
