/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.ui.swingx.AbstractTableElement;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages(value = {"StudentsDBTopComponent.displayName=Schülerdatenbank ({0})"})
class StudentsDBTopComponent extends AbstractTableElement {

    private StudentsDBOpenSupport.Env env;
    private final StudentsDBTableModel model = StudentsDBTableModel.create();
    private final javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane();

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public StudentsDBTopComponent() {
        super();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setColumnFactory(model.createColumnFactory());
        toolbar.setLayout(new FlowLayout(FlowLayout.RIGHT));
        Utilities.actionsForPath("Loaders/" + StudentsDBOpenSupport.STUDENTSDB_MIME + "/Toolbar").stream()
                .forEach(toolbar::add);
        setLayout(new BorderLayout());
        setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    StudentsDBTopComponent(final StudentsDBOpenSupport.Env env) {
        this();
        this.env = env;
        initializeComponent();
        activatedNodes(Collections.EMPTY_LIST);
    }

    protected void initializeComponent() {
        final StudentsDBOpenSupport support = env.findCloneableOpenSupport();
        model.initialize(support.getVCardStudents(), Lookup.EMPTY);
//        obj.addPropertyChangeListener(listener);
//        support = obj.getLookup().lookup(CurriculumSupport.class);
//        lkpResult = obj.getLookup().lookupResult(CurriculumTableActions.class);
        //        LookupListener weakl = WeakListeners.create(LookupListener.class, listener, ctEditorResult);
        //        ctEditorResult.addLookupListener(weakl);
//        lkpResult.addLookupListener(listener);
        final Lookup hlkp = Lookups.forPath("Editors/" + StudentsDBOpenSupport.STUDENTSDB_MIME + "/Highlighter");
        hlkp.lookupAll(HighlighterInstanceFactory.class).stream()
                .map(hlf -> hlf.createHighlighter(table, this)).
                filter(Objects::nonNull)
                .forEach(table::addHighlighter);
        final ActionMap am1 = table.getActionMap();
        am1.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final List<VCardStudent> studs = Arrays.stream(table.getSelectedRows())
                        .map(table::convertRowIndexToModel)
                        .mapToObj(model::getItemAt)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                Mutex.EVENT.writeAccess(() -> {
                    final StudentsDBRemoveStudentAction ac = new StudentsDBRemoveStudentAction(studs, model.getItemsModel());
                    ac.actionPerformed(null);
                });
            }
        });
        initTable();
        updateName();
    }

    protected void initTable() {
        Mutex.EVENT.writeAccess(() -> {
//            final CurriculumTableActionsImpl ac;
//            if ((ac = getCurrentActions()) != null) {
//            DropTarget dt = new DropTarget();
//            try {
//                dt.addDropTargetListener(ac);
//                table.setDropTarget(dt);
//                scrollPane.setDropTarget(dt);
//            } catch (TooManyListenersException ex) {
//                PlatformUtil.getCodeNameBaseLogger(CurriculumTableElement.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
//            }
            scrollPane.setViewportView(table);
            table.setModel(model);
//            ac.addUndoableEditListener(undoRedo);
//            } else {
//                scrollPane.setViewportView(initLoading());
//            }
        });
    }

    @Override
    protected Node getNodeForRow(int rowIndex) {
        final VCardStudent item = model.getItemAt(rowIndex);
        return new StudentNode(item, model.getItemsModel());
    }

    @Override
    protected void activatedNodes(List<Node> selected) {
        setActivatedNodes(selected.toArray(new Node[selected.size()]));
    }

    @Override
    protected JPopupMenu createPopup(int modelCol, int modelRow, Point p, MouseEvent e) {
        final Node n = getNodeForRow(modelRow);
        return Utilities.actionsToPopup(n.getActions(true), this);
    }

    @Override
    protected void updateName() {
        if (env != null) {
            Mutex.EVENT.writeAccess(() -> {
                final String dn = NbBundle.getMessage(StudentsDBTopComponent.class, "StudentsDBTopComponent.displayName", env.getProviderInfo().getDisplayName());
                setDisplayName(dn);
//                Node n = obj.getNodeDelegate();
//                boolean modif = obj.isModified();
//                boolean readOnly = !obj.getPrimaryFile().canWrite();
//                String displayName = UIUtilities.findDisplayName(obj);
//                tc.setDisplayName(UIUtilities.annotateName(displayName, false, modif, readOnly));
//                tc.setHtmlDisplayName(UIUtilities.annotateName(displayName, true, modif, readOnly));
            });
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        if (env != null && env.isValid()) {
            out.writeObject(env);
            out.writeInt(table.getSelectedRow());
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object oi = in.readObject();
        if (oi instanceof StudentsDBOpenSupport.Env) {
            env = (StudentsDBOpenSupport.Env) oi;
            int sel = in.readInt();
            if (sel >= 0) {
                table.getSelectionModel().setSelectionInterval(sel, sel);
            }
        }
        initializeComponent();
        activatedNodes(Collections.EMPTY_LIST);
    }

    class StudentNode extends AbstractNode {

        StudentNode(final VCardStudent student, final VCardStudents coll) {
            super(Children.LEAF, Lookups.fixed(student, coll));
        }

        @Override
        public boolean canDestroy() {
            return true;
        }

        @Override
        public void destroy() throws IOException {
            System.out.println("Destroy");
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{DeleteAction.get(DeleteAction.class)};
        }

    }
}
