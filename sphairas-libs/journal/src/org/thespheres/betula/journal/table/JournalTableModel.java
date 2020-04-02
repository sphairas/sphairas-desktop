/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import com.google.common.eventbus.Subscribe;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.util.Set;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.openide.util.Lookup;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.module.JournalDataObject;
import org.thespheres.betula.journal.table.JournalTableModel.JournalColFactory;
import org.thespheres.betula.journal.util.JournalTableColumn;
import org.thespheres.betula.listprint.builder.TableItem;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel.PluggableColumnFactory;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 */
public class JournalTableModel extends AbstractPluggableTableModel<EditableJournal<?, ?>, EditableRecord<?>, JournalTableColumn, JournalColFactory> implements TableItem {

    private JournalTableModel(Set<JournalTableColumn> s) {
        super("RecordsTableModel", s);
    }

    static JournalTableModel create() {
        Set<JournalTableColumn> s = DefaultColumns.create();
        MimeLookup.getLookup(JournalDataObject.JOURNAL_MIME)
                .lookupAll(JournalTableColumn.Factory.class).stream()
                .map(JournalTableColumn.Factory::createInstance)
                .forEach(s::add);
        return new JournalTableModel(s);
    }

    @Override
    public void initialize(EditableJournal journal, Lookup context) {
        if (this.model != null) {
//            this.model.removeCalendarListener(this);
            this.model.getEventBus().unregister(this);
        }
        super.initialize(journal, context);
        this.model.getEventBus().register(this);
    }

    @Override
    protected JournalColFactory createColumnFactory() {
        return new JournalColFactory();
    }

    @Override
    protected int getItemSize() {
        return model.getEditableRecords().size();
    }

    @Override
    protected EditableRecord<?> getItemAt(int row) {
        return model.getEditableRecords().get(row);
    }

    @Subscribe
    public void onModelChange(CollectionChangeEvent event) {
        if (EditableJournal.COLLECTION_RECORDS.equals(event.getCollectionName())) {
            event.getItemAs(EditableRecord.class).ifPresent(er -> {
                final int line = er.getIndex();
                switch (event.getType()) {
                    case ADD:
                        EventQueue.invokeLater(() -> fireTableRowsInserted(line, line));
                        break;
                    case REMOVE:
                        EventQueue.invokeLater(() -> {
                            try {
                                fireTableRowsDeleted(line, line);
                            } catch (IndexOutOfBoundsException e) {
                                //Bug in SwingX
                            }
                        });
                        break;
                    case REORDER:
                        EventQueue.invokeLater(() -> fireTableDataChanged());
                        break;
                }
            });
        }
    }

    @Subscribe
    public void onPropertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof EditableJournal && EditableJournal.PROP_JOURNAL_START.equals(evt.getPropertyName())) {
            EventQueue.invokeLater(() -> fireTableDataChanged());
        } else if (evt.getSource() instanceof EditableRecord) {
            final int index = ((EditableRecord) evt.getSource()).getIndex();
            EventQueue.invokeLater(() -> fireTableRowsUpdated(index, index));
        }
    }

    public class JournalColFactory extends PluggableColumnFactory {

//        protected JournalTableModel model(JXTable t) {
//            return model(t.getModel());
//        }
//
//        protected JournalTableModel model(TableModel t) {
//            return (JournalTableModel) t;
//        }
    }
}
