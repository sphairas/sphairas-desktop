/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.ui;

import org.thespheres.betula.gpuntis.ui.impl.UntisDefaultColumns;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.gpuntis.ImportedLesson;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;
import org.thespheres.betula.gpuntis.UntisImportData;
import org.thespheres.betula.gpuntis.impl.UntisSourceUserOverrides;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.utilities.AbstractSourceOverrides;

/**
 *
 * @author boris.heithecker
 */
public class UntisCreateDocumentsTableModel extends ImportTableModel<ImportedLesson, UntisImportData> {

    private UntisSourceUserOverrides overrides;

    public UntisCreateDocumentsTableModel() {
        super(createColumns());
    }

    private static Set<ImportTableColumn> createColumns() {
        String product = NbBundle.getMessage(UntisDefaultColumns.class, "UntisDefaultColumns.product");
        Set<ImportTableColumn> s = ImportTableModel.createDefault(product);
        s.addAll(UntisDefaultColumns.create(product));
        Lookups.forPath("UntisCreateDocumentsVisualPanel/Columns").lookupAll(ImportTableColumn.Factory.class).stream()
                .map(ImportTableColumn.Factory::createInstance)
                .forEach(s::add);
        return s;
    }

    @Override
    public void initialize(final UntisImportData descriptor) {
        final UntisImportConfiguration config = (UntisImportConfiguration) descriptor.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        ChangeSet<ImportedLesson> s = (ChangeSet<ImportedLesson>) descriptor.getProperty(AbstractFileImportAction.SELECTED_NODES);
        selected.clear();
        s.stream()
                .peek(il -> il.initialize(config, descriptor))
                .peek(il -> descriptor.addPropertyChangeListener(createPCL(il, descriptor)))
                .forEach(selected::add);
        fireTableDataChanged();
    }

    private PropertyChangeListener createPCL(final ImportedLesson il, UntisImportData descriptor) {
        return (PropertyChangeEvent evt) -> {
            if (AbstractFileImportAction.IMPORT_TARGET.equals(evt.getPropertyName())) {
                UntisImportConfiguration cfg = (UntisImportConfiguration) evt.getNewValue();
                il.initialize(cfg, descriptor);
            }
        };
    }

    @Override
    public ColFactory createColumnFactory(final UntisImportData wizard) {
        UntisColFactory ret = new UntisColFactory();
        UntisImportConfiguration configuration = (UntisImportConfiguration) wizard.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        ret.initialize(configuration, wizard);
        //Init SourceUserOverrides here, not in UntisCreateDocumentsTableModel.initialize
        //Needs to be ready ealier for column configuration
        overrides = (UntisSourceUserOverrides) wizard.getProperty(AbstractSourceOverrides.USER_SOURCE_OVERRIDES);
        return ret;
    }

    private final class UntisColFactory extends ColFactory {

        private UntisColFactory() {
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
