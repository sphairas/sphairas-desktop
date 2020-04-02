/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.target;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.journal.table.AbstractJounalElement;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.journal.module.Constants;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;

/**
 *
 * @author boris.heithecker
 */
//@MultiViewElement.Registration(displayName = "#assess.table.multiview.displayname", mimeType = "application/betula-calendar", persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED, preferredID = "calendar-assess-table", iconBase = "org/thespheres/betula/journal/resources/betulacal16.png")
@MultiViewElement.Registration(
        displayName = "#TargetAssessmentTableElement.displayname",
        iconBase = "org/thespheres/betula/journal/resources/betulacal16.png",
        mimeType = "text/betula-journal-file+xml",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "TargetAssessmentTableElement",
        position = 5000)
@NbBundle.Messages({"TargetAssessmentTableElement.displayname=Bewerten"})
public final class TargetAssessmentTableElement extends AbstractJounalElement implements Serializable {

//    private final TargetAssessmentColumnFactory colFactory = new TargetAssessmentColumnFactory();
    private TargetAssessmentSupport targetAssessmentSupport;
    private final static Set<TargetAssessmentTableElement> TC_TRACKER = new HashSet<>(2);

    public TargetAssessmentTableElement() {
    }

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public TargetAssessmentTableElement(Lookup context) {
        super(context);
        initializeComponent();
        activatedNodes(Collections.EMPTY_LIST);
    }

    @Override
    protected void initializeComponent() {
        super.initializeComponent();
        targetAssessmentSupport = obj.getLookup().lookup(TargetAssessmentSupport.class);

        Lookup hlkp = Lookups.forPath("Editors/" + Constants.JOURNAL_TARGETTABLE_MIME + "/Highlighter");
        hlkp.lookupAll(HighlighterInstanceFactory.class).stream()
                .map(hlf -> hlf.createHighlighter(table, this))
                .filter(Objects::nonNull)
                .forEach(table::addHighlighter);

        final ActionMap am1 = table.getActionMap();
        am1.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { //TODO: überarbeiten !!!
                final TargetAssessmentTableModel m = targetAssessmentSupport.getModel();
                final TargetAssessment ta = targetAssessmentSupport.getLookup().lookup(TargetAssessment.class);
                if (m != null && ta != null) {
                    final List<EditableParticipant> studs = Arrays.stream(table.getSelectedRows())
                            .map(table::convertRowIndexToModel)
                            .mapToObj(ri -> (EditableParticipant) m.getValueAt(ri, 0))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    Mutex.EVENT.writeAccess(() -> {
                        studs.stream()
                                //                                    .peek(ep -> ta.submit(ep.getStudentId(), null, null))
                                .forEach(ep -> ep.getEditableJournal().removeParticipant(ep.getStudentId()));
                    });
                }
            }
        });
        initTable();
    }

    @Override
    protected void initTable() {
        Mutex.EVENT.writeAccess(() -> {
            if (targetAssessmentSupport != null) {
                scrollPane.setViewportView(table);
                final TargetAssessmentTableModel model = targetAssessmentSupport.getModel();
                final JournalEditor currentEditor = getCurrentEditor();
                if (currentEditor != null) {
//                    colFactory.initialize(currentEditor);
                    table.setColumnFactory(model.createColumnFactory());
                    model.initialize(currentEditor.getEditableJournal(), currentEditor.getLookup());
//                    model.initialize(currentEditor);
                    currentEditor.addUndoableEditListener(undoRedo);
                }
                table.setModel(model);
            } else {
                scrollPane.setViewportView(initLoading());
            }
        });
    }

    @Override
    protected Node getNodeForRow(int row) {
        if (support != null) {
            final EditableParticipant er = getCurrentEditor().getEditableJournal().getEditableParticipants().get(row);
            return er.getNodeDelegate();
        }
        return null;
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        EventQueue.invokeLater(() -> {
            synchronized (TC_TRACKER) {
                final boolean open = TC_TRACKER.isEmpty();
                TC_TRACKER.add(this);
                if (open) {
                    WindowManager.getDefault().findTopComponentGroup("JournalTargetGroup").open();
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
                    WindowManager.getDefault().findTopComponentGroup("JournalTargetGroup").close();
                }
            }
        });
    }
}
