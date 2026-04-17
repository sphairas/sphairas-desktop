/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex.ui;

import java.awt.EventQueue;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.schulconnex.Schulconnex;
import org.thespheres.betula.schulconnex.SchulconnexImportConfiguration;
import org.thespheres.betula.schulconnex.SchulconnexKlasseItem;
import org.thespheres.betula.schulconnex.SchulconnexImportData;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;
import org.thespheres.betula.xmlimport.uiutil.UnitColumn;

final class SchulconnexPrimaryUnitDocumentsTableModel extends ImportTableModel<SchulconnexKlasseItem, SchulconnexImportData<SchulconnexKlasseItem>> {

    private final RequestProcessor rp = new RequestProcessor(SchulconnexPrimaryUnitDocumentsTableModel.class);

    SchulconnexPrimaryUnitDocumentsTableModel() {
        super(createColumns());
    }

    private static Set<ImportTableColumn> createColumns() {
        final String product = Schulconnex.getProduct().getDisplay();
        final Set<ImportTableColumn> ret = new HashSet<>();
        ret.add(new SelectedColumn());
        ret.add(new DefaultColumns.NodeColumn(product));
        ret.add(new UnitColumn(product));
        ret.add(new DefaultColumns.UnitDisplayColumn(product));
        ret.add(new DefaultColumns.DeleteDateColumn());
        return ret;
    }

    @Override
    public void initialize(final SchulconnexImportData<SchulconnexKlasseItem> wiz) {
        @SuppressWarnings("unchecked")
        final ChangeSet<SchulconnexKlasseItem> s = (ChangeSet<SchulconnexKlasseItem>) wiz.getProperty(AbstractFileImportAction.SELECTED_NODES);
        final RequestProcessor.Task t = rp.create(() -> {
            EventQueue.invokeLater(() -> {
                selected.clear();
                s.stream()
                        //                .peek(il -> il.initialize(wiz.getConfiguration(), wiz))
                        //                .peek(il -> wiz.addPropertyChangeListener(createPCL(il, wiz)))
                        .forEach(selected::add);
                fireTableDataChanged();
            });
        });
        s.addChangeListener(e -> t.schedule(1000));
        t.schedule(1000);
    }

    private static final class SelectedColumn extends DefaultColumns.DefaultCheckBoxColumn<SchulconnexKlasseItem, SchulconnexImportConfiguration, SchulconnexImportData<SchulconnexKlasseItem>, SchulconnexPrimaryUnitDocumentsTableModel> {

        private SelectedColumn() {
            super("selected", 50);
        }

        @Override
        public Boolean getColumnValue(final SchulconnexKlasseItem item) {
            return item.isSelected();
        }

        @Override
        public boolean setColumnValue(final SchulconnexKlasseItem item, final Object value) {
            boolean before = item.isSelected();
            item.setSelected((boolean) value);
            return before = !((boolean) value);
        }

        @Override
        public String getDisplayName() {
            return "";
        }
    }
}
