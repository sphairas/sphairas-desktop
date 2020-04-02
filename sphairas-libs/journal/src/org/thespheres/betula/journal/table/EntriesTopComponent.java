/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.ToolTipHighlighter;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.journal.module.Constants;
import org.thespheres.betula.journal.module.JournalDataObject;
import org.thespheres.betula.services.util.NbUtilities;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.ui.util.UIUtilities;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"EntriesTopComponent.displayName=Berichtsheft-Einträge",
    "EntriesTopComponent.current.displayName=Berichtsheft-Einträge ({0})",
    "EntriesTopComponent.openAction=Einträge (Berichtshefte)"})
@ConvertAsProperties(dtd = "-//org.thespheres.betula.journal.table//EntriesTopComponent//EN",
        autostore = false)
@TopComponent.Description(preferredID = "EntriesTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "org.thespheres.betula.calendar.journal.EntriesTopComponent")
@ActionReference(path = "Menu/Window/betula-project-local-windows", position = 100, separatorAfter = 1000)
@TopComponent.OpenActionRegistration(displayName = "#EntriesTopComponent.openAction",
        preferredID = "EntriesTopComponent")
public class EntriesTopComponent extends JournalOutputTable {

    private final EntriesTableColumnFactory columnFactory = new EntriesTableColumnFactory();
    private final Lookup.Result<EditableRecord> lookupResult;
    private final Listener listener = new Listener();
    private final EntriesTableModel model = new EntriesTableModel();
    private final SelectionHighlighter selectionHighlighter = new SelectionHighlighter();

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public EntriesTopComponent() {
        super();
        table.setColumnFactory(columnFactory);
        Lookup hlkp = Lookups.forPath("Editors/" + Constants.JOURNAL_ENTRIES_MIME + "/Highlighter");
        hlkp.lookupAll(HighlighterInstanceFactory.class).stream()
                .map(hlf -> hlf.createHighlighter(table, this))
                .filter(Objects::nonNull)
                .forEach(table::addHighlighter);
        table.addHighlighter(selectionHighlighter);
        table.addHighlighter(new ValuesHighlighter());
        table.setModel(model);
        lookupResult = Utilities.actionsGlobalContext().lookupResult(EditableRecord.class);
        onChange();
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        lookupResult.addLookupListener(listener);
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
        lookupResult.removeLookupListener(listener);
    }

    @Override
    protected synchronized void onChange() {
        super.onChange();
        selectionHighlighter.updateRecord();
    }

    @Override
    protected void setCurrentCalendar(JournalDataObject m) {
        if (m != null) {
            if (!Objects.equals(m, current)) {
                current = m;
                NbUtilities.waitAndThen(current.getLookup(), EditableJournal.class, ecal -> {
                    EventQueue.invokeLater(() -> {
                        //Important to set colFactory before model
                        columnFactory.setEditableCalendar(ecal, m.getLookup());
                        model.setEditableJounal(ecal);
                    });
                });
            }
        } else {
//            current = null;
//            model.setEditableJounal(null);
//            columnFactory.setEditableCalendar(null, null);
        }
        updateName();
    }

    @Override
    protected void updateName() {
        if (current == null) {
            setName(NbBundle.getMessage(EntriesTopComponent.class, "EntriesTopComponent.displayName"));
        } else {
            String n = UIUtilities.findDisplayName(current);
            setName(NbBundle.getMessage(EntriesTopComponent.class, "EntriesTopComponent.current.displayName", n));
        }
    }

    @Override
    protected Node getNodeForRow(int row) {
        if (current != null) {
            final EditableJournal<?, ?> j = current.getLookup().lookup(EditableJournal.class);
            final EditableParticipant er = j.getEditableParticipants().get(row);
            return er.getNodeDelegate();
        }
        return null;
    }

    @Override
    protected JPopupMenu createPopup(final int modelCol, final int modelRow, Point p, MouseEvent e) {
        final EditableJournal<?, ?> ej = model.getEditableCalendar();
        if (ej != null && modelCol > 0) { //isPopupAllowed()
//            final Grade undefined = GradeFactory.find("mitarbeit2", "undefined");
//            final Grade fill = GradeFactory.find("mitarbeit2", "x");
            final Node ern = ej.getEditableRecords().get(modelCol - 1).getNodeDelegate();
            final Node epn = ej.getEditableParticipants().get(modelRow).getNodeDelegate();
            final Lookup context = new ProxyLookup(ern.getLookup(), epn.getLookup());
            Action[] ac = Stream.concat(Utilities.actionsForPath("Loaders/text/betula-journal-record-context/Actions").stream(),
                    Utilities.actionsForPath("Loaders/text/betula-journal-participant-context/Actions").stream())
                    .toArray(Action[]::new);
            return Utilities.actionsToPopup(ac, context);
        }
        return null;
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private class SelectionHighlighter extends ColorHighlighter implements HighlightPredicate {

        private List<EditableRecord> last;

        @SuppressWarnings({"LeakingThisInConstructor",
            "OverridableMethodCallInConstructor"})
        private SelectionHighlighter() {
            super(table.getSelectionBackground(), table.getSelectionForeground());
            setHighlightPredicate(this);
        }

        private void updateRecord() {
            List<EditableRecord> current = lookupResult.allInstances().stream()
                    .map(EditableRecord.class::cast)
                    .collect(Collectors.toList());
            if (!current.isEmpty() || EntriesTopComponent.this.current == null) {
                last = current;
            }
            fireStateChanged();
        }

        @Override
        public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
            final int ci = ca.convertColumnIndexToModel(ca.column);
            return last != null ? last.stream().anyMatch(er -> er.getIndex() == ci - 1) : false;
        }
    }

    private class ValuesHighlighter extends ToolTipHighlighter {

        @SuppressWarnings({"LeakingThisInConstructor",
            "OverridableMethodCallInConstructor"})
        private ValuesHighlighter() {
            setHighlightPredicate(HighlightPredicate.ALWAYS);
        }

        @Override
        protected Component doHighlight(Component component, ComponentAdapter adapter) {
            if (adapter.getValue() == null) {
                return component;
            }
            final StringJoiner sj = new StringJoiner("<br>", "<html>", "</html>");
            int ci = adapter.convertColumnIndexToModel(adapter.column);
            int ri = adapter.convertRowIndexToModel(adapter.row);
            final EditableJournal<?, ?> ecal = model.getEditableCalendar();
            if (ecal != null && ci > 0) {
                final EditableRecord<?> er = ecal.getEditableRecords().get(ci - 1);
                final JournalEditor editor;
                if (current != null && (editor = current.getLookup().lookup(JournalEditor.class)) != null) {
                    sj.add(editor.formatLocalDate(er, false));
                }
                sj.add(ecal.getEditableParticipants().get(ri).getDirectoryName());
                final Grade g = er.getGradeAt(ri);
                if (g != null) {
                    sj.add(g.getLongLabel());
                }
            }
            ((JComponent) component).setToolTipText(sj.toString());
            return component;
        }
    }

    private final class Listener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            Mutex.EVENT.writeAccess(selectionHighlighter::updateRecord);
        }

    }
}
