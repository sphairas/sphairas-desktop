/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.ui2;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.sibank.SiBankImportData;
import org.thespheres.betula.sibank.SiBankKursItem;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.impl.SiBankSourceOverrides;
import org.thespheres.betula.sibank.ui2.impl.SiBankDefaultColumns;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.utilities.AbstractSourceOverrides;

/**
 *
 * @author boris.heithecker
 */
public class SiBankCreateDocumentsTableModel extends ImportTableModel<SiBankKursItem, SiBankImportData<SiBankKursItem>> {

    private SiBankSourceOverrides overrides;

    public SiBankCreateDocumentsTableModel() {
        super(createColumns());
    }

    private static Set<ImportTableColumn> createColumns() {
        String product = NbBundle.getMessage(SiBankDefaultColumns.class, "SiBankDefaultColumns.product");
        Set<ImportTableColumn> s = ImportTableModel.createDefault(product);
        s.addAll(SiBankDefaultColumns.create(product));
        Lookups.forPath("SiBankCreateDocumentsVisualPanel/Columns").lookupAll(ImportTableColumn.Factory.class).stream()
                .map(ImportTableColumn.Factory::createInstance)
                .forEach(s::add);
        return s;
    }

    @Override
    public void initialize(final SiBankImportData descriptor) {
        final SiBankImportTarget config = (SiBankImportTarget) descriptor.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        ChangeSet<SiBankKursItem> s = (ChangeSet<SiBankKursItem>) descriptor.getProperty(AbstractFileImportAction.SELECTED_NODES);
        selected.clear();
        s.stream()
//                .peek(il -> il.initialize(config, descriptor))//TODO: remove initializ 2* ?????
                .peek(il -> descriptor.addPropertyChangeListener(createPCL(il, descriptor)))
                .forEach(selected::add);
        fireTableDataChanged();
    }

    private PropertyChangeListener createPCL(final SiBankKursItem il, final SiBankImportData wiz) {
        return (PropertyChangeEvent evt) -> {
            if (AbstractFileImportAction.IMPORT_TARGET.equals(evt.getPropertyName())) {
                SiBankImportTarget cfg = (SiBankImportTarget) evt.getNewValue();
                il.initialize(cfg, wiz);
            }
        };
    }

    @Override
    public ColFactory createColumnFactory(final SiBankImportData wizard) {
        SiBankColFactory ret = new SiBankColFactory();
        SiBankImportTarget configuration = (SiBankImportTarget) wizard.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        ret.initialize(configuration, wizard);
        //Init SourceUserOverrides here, not in UntisCreateDocumentsTableModel.initialize
        //Needs to be ready ealier for column configuration
        overrides = (SiBankSourceOverrides) wizard.getProperty(AbstractSourceOverrides.USER_SOURCE_OVERRIDES);
        return ret;
    }

    private final class SiBankColFactory extends ColFactory {

        private SiBankColFactory() {
        }

        @Override
        public void configureTableColumn(TableModel model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            if (overrides != null) {
                col.addHighlighter(overrides.createHighlighter());
            } else {
                throw new IllegalStateException();
            }
        }
    }
}
