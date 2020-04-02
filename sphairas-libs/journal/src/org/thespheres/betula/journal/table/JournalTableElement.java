/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box.Filler;
import javax.swing.JPopupMenu;
import javax.swing.text.DefaultEditorKit;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.actions.PasteAction;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.journal.module.JournalDataObject;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;

@MultiViewElement.Registration(
        displayName = "#JournalTableElement.displayname",
        iconBase = "org/thespheres/betula/journal/resources/betulacal16.png",
        mimeType = "text/betula-journal-file+xml",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "JournalTableElement",
        position = 2000)
@Messages({"JournalTableElement.displayname=Übersicht"})
public final class JournalTableElement extends AbstractJounalElement implements Serializable {

    private final static Set<JournalTableElement> TC_TRACKER = new HashSet<>(2);
    private final Filler filler = new Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
    private final JXDatePicker datePicker = new JXDatePicker();
    protected final PasteNodes paste = new PasteNodes();

    public JournalTableElement() {
    }

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public JournalTableElement(Lookup lkp) {
        super(lkp);
        initializeComponent();
        activatedNodes(Collections.EMPTY_LIST);
        getActionMap().put("paste", paste);
        getActionMap().put("paste-from-clipboard", paste);
    }

    @Override
    protected void initializeComponent() {
        super.initializeComponent();
        toolbar.add(filler);
        toolbar.add(datePicker);
        table.setRowHeight(table.getRowHeight() * 3);
        table.setColumnFactory(support.getModel().createColumnFactory());
        Lookup hlkp = Lookups.forPath("Editors/" + JournalDataObject.JOURNAL_MIME + "/Highlighter");
        //
        //Need to add this special highlight because of textareaprovider
//        table.addHighlighter(new ColorHighlighter(HighlightPredicate.ALWAYS, Color.CYAN, Color.ORANGE, Color.BLUE, Color.RED));
        table.addHighlighter(new ColorHighlighter(HighlightPredicate.ALWAYS, table.getBackground(), table.getForeground(), table.getSelectionBackground(), table.getSelectionForeground()));
        //
        hlkp.lookupAll(HighlighterInstanceFactory.class).stream()
                .map(hlf -> hlf.createHighlighter(table, this))
                .filter(Objects::nonNull)
                .forEach(table::addHighlighter);

        final ActionMap am = table.getActionMap();
        final ActionMap tcam = getActionMap();
        am.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Mutex.EVENT.writeAccess(JournalTableElement.this::removeSelection);
            }

        });
        am.put("paste", paste);
        am.put(DefaultEditorKit.pasteAction, paste);
        tcam.put(DefaultEditorKit.pasteAction, paste);

        initTable();
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        EventQueue.invokeLater(() -> {
            synchronized (TC_TRACKER) {
                final boolean open = TC_TRACKER.isEmpty();
                TC_TRACKER.add(this);
                if (open) {
                    WindowManager.getDefault().findTopComponentGroup("JournalGroup").open();
                }
            }
        });
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
        EventQueue.invokeLater(() -> {
            synchronized (TC_TRACKER) {
                TC_TRACKER.remove(this);
                if (TC_TRACKER.isEmpty()) {
                    WindowManager.getDefault().findTopComponentGroup("JournalGroup").close();
                }
            }
        });
    }

    private void removeSelection() {
        JournalEditor je = getLookup().lookup(JournalEditor.class);
        if (je != null) {
            List<EditableRecord> l = Arrays.stream(table.getSelectedRows())
                    .map(table::convertRowIndexToModel)
                    .mapToObj(index -> je.getEditableJournal().getEditableRecords().get(index))
                    .collect(Collectors.toList());
            l.stream()
                    .map(r -> je.getEditableJournal().getEditableRecords().indexOf(r))
                    .filter(ci -> ci != -1)
                    .forEach(je::removeRecord);
        }
    }

    @Override
    protected void initTable() {
        Mutex.EVENT.writeAccess(() -> {
            final JournalEditor ed = getCurrentEditor();
            if (ed != null) {
                //TODO: setClasstest table
//            colFactory.initialize(editor);
                scrollPane.setViewportView(table);
                table.setModel(support.getModel());
                ed.addUndoableEditListener(undoRedo);
            } else {
                scrollPane.setViewportView(initLoading());
            }
        });
    }

    @Override
    protected Node getNodeForRow(int rowIndex) {
        if (support != null) {
            int row = table.convertRowIndexToModel(rowIndex);
            EditableRecord er = support.getModel().getItemsModel().getEditableRecords().get(row);
            return er.getNodeDelegate();
        }
        return null;
    }

    @Override
    protected JPopupMenu createPopup(int modelCol, int modelRow, Point p, MouseEvent e) {
//        JournalEditor je = getLookup().lookup(JournalEditor.class);
//        final Node ern = je.getEditableJournal().getEditableRecords().get(modelCol - 1).getNodeDelegate();
//        final Node epn = je.getEditableJournal().getEditableParticipants().get(modelRow).getNodeDelegate();
//        final Lookup context = new ProxyLookup(ern.getLookup(), epn.getLookup(), je.getLookup());
//        Utilities.actionsForPath("Loaders/text/betula-journal-record-context/Actions").stream();
        final List<Action> l = Stream.concat(Utilities.actionsForPath("Loaders/text/betula-journal-participant-context/Actions").stream(),
                Utilities.actionsForPath("Loaders/text/betula-journal-file+xml/Actions").stream())
                .collect(Collectors.toList());
        l.add(0, SystemAction.get(PasteAction.class));
//        l.add(1, null);
//        return Utilities.actionsToPopup(new Action[]{paste}, table);
        return Utilities.actionsToPopup(l.stream().toArray(Action[]::new), this);
    }

    private class PasteNodes extends AbstractAction {

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        private PasteNodes() {
            super("paste-nodes");
        }

        @Override
        public Object getValue(String key) {
            if ("delegates".equals(key)) {
                Transferable t = null;
                try {
                    t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                } catch (IllegalStateException illse) {
                }
                if (t != null) {
                    final NodeTransfer.Paste paste = NodeTransfer.findPaste(t);
                    if (paste != null) {
                        return paste.types(obj.getNodeDelegate());
                    }
                }

            }
            return super.getValue(key);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
//            throw new UnsupportedOperationException("Should never be called.");
        }
    }
}
