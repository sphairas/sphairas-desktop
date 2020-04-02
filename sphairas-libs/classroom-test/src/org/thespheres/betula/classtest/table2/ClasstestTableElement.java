/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.table2;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultEditorKit;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.decorator.Highlighter;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.actions.PasteAction;
import org.openide.awt.Actions;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeTransfer;
import org.openide.text.CloneableEditor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.classtest.model.ClassroomTestEditor2;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.model.EditableStudent;
import org.thespheres.betula.classtest.model.LineItem;
import org.thespheres.betula.classtest.module2.ClasstestDataObject;
import org.thespheres.betula.ui.swingx.AbstractTableElement;
import org.thespheres.betula.ui.swingx.ExternalizableHighlighter;
import org.thespheres.betula.ui.swingx.ExternalizableHighlighter.ExternalizableHighlighterInstanceFactory;
import org.thespheres.betula.ui.util.UIUtilities;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.util.CollectionUtil;

@MultiViewElement.Registration(
        displayName = "#table2.multiview.displayname",
        iconBase = "org/thespheres/betula/classtest/resources/betulact16.png",
        mimeType = "text/betula-classtest-file+xml",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "ClasstestVisual",
        position = 2000
)
@Messages({"table2.multiview.displayname=Tabelle"})
public final class ClasstestTableElement extends AbstractTableElement implements MultiViewElement, Externalizable {

    private final static Set<ClasstestTableElement> TC_TRACKER = new HashSet<>(2);
    private ClasstestDataObject obj;
    private ClasstestTableSupport support;
    private Lookup.Result<ClassroomTestEditor2> ctEditorResult;
    private final Listener listener = new Listener();
    private final Box.Filler filler = new Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
    private final JXDatePicker datePicker = new JXDatePicker();
    private final javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane();
    private final PasteNodes paste = new PasteNodes();
    private final CopyCutSelection copy = new CopyCutSelection(false);
    private final CopyCutSelection cut = new CopyCutSelection(true);

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public ClasstestTableElement() {
        toolbar.add(Actions.forID("Betula", "org.thespheres.betula.classtest.actions.AddBasketAction"));
        toolbar.add(filler);
        toolbar.add(datePicker);
        setLayout(new BorderLayout());
        setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        getActionMap().put("paste", paste);
        getActionMap().put("paste-from-clipboard", paste);
        getActionMap().put(DefaultEditorKit.copyAction, copy);
        getActionMap().put("copy", copy);
        getActionMap().put(DefaultEditorKit.cutAction, copy);
        getActionMap().put("cut", cut);
        
        NbPreferences.root().node("/org/netbeans/modules/projectui").putBoolean("enable.actualselectionproject", true);
    }

    public ClasstestTableElement(Lookup lkp) {
        this();
        obj = lkp.lookup(ClasstestDataObject.class);
        initializeComponent(null);
        activatedNodes(Collections.EMPTY_LIST);
    }

    protected void initializeComponent(ExternalizableHighlighter[] arr) {
        assert obj != null;
        obj.addPropertyChangeListener(listener);
        support = obj.getLookup().lookup(ClasstestTableSupport.class);
        ctEditorResult = obj.getLookup().lookupResult(ClassroomTestEditor2.class);

//        table.setColumnFactory(colFactory);
        Lookup hlkp = Lookups.forPath("Editors/" + ClasstestDataObject.CLASSTEST_MIME + "/Highlighter");
        hlkp.lookupAll(HighlighterInstanceFactory.class).stream()
                .map(hlf -> createOrRestore(hlf, arr))
                .filter(Objects::nonNull)
                .forEach(table::addHighlighter);

        ActionMap am1 = table.getActionMap();
        am1.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final ClasstestTableModel2 m = (ClasstestTableModel2) table.getModel();
                final ClassroomTestEditor2<?, ?> ed = TopComponent.getRegistry().getActivated().getLookup().lookup(ClassroomTestEditor2.class);
                if (m != null && ed != null) {
                    final List<StudentId> studs = Arrays.stream(table.getSelectedRows())
                            .map(table::convertRowIndexToModel)
                            .map(ri -> LineItem.toModelIndex(ri, ed.getEditableClassroomTest().getEditableStudents().size()))
                            .filter(mi -> mi >= 0)
                            .sorted()
                            .mapToObj(index -> ed.getEditableClassroomTest().getEditableStudents().get(index))
                            .map(EditableStudent::getStudentId)
                            .collect(Collectors.toList());
                    for (int i = studs.size() - 1; i > -1; i--) {
                        ed.removeStudent(studs.get(i));
                    }
                }
            }
        });

        initTable();
    }

    private Highlighter createOrRestore(HighlighterInstanceFactory hlf, ExternalizableHighlighter[] arr) {
        if (arr != null && hlf instanceof ExternalizableHighlighterInstanceFactory) {
            String id = ((ExternalizableHighlighterInstanceFactory) hlf).id();
            ExternalizableHighlighter found = Arrays.stream(arr)
                    .filter(eh -> eh.id().equals(id))
                    .collect(CollectionUtil.singleOrNull());
            if (found != null) {
                found.restore(table, this);
                return found;
            }
        }
        return hlf.createHighlighter(table, this);
    }

    private synchronized void initTable() {
        Mutex.EVENT.writeAccess(() -> {
            ClassroomTestEditor2 editor = ctEditorResult.allInstances().stream()
                    .findAny()
                    .orElse(null);
            if (editor != null) {
                ctEditorResult.removeLookupListener(listener);
//                colFactory.initialize(editor);
                table.setColumnFactory(support.getModel().createColumnFactory());
                scrollPane.setViewportView(table);
                //TODO: setModel only after it has been initialized?
                table.setModel(support.getModel());
                editor.addUndoableEditListener(undoRedo);
                DropTarget dt = new DropTarget();
                try {
                    //TODO: siehe auch OutlineView
                    dt.addDropTargetListener(editor);
                    table.setDropTarget(dt);
                    scrollPane.setDropTarget(dt);
                } catch (TooManyListenersException ex) {
                    Logger.getLogger(ClasstestTableElement.class.getName()).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                }
            } else {
                scrollPane.setViewportView(initLoading());
                ctEditorResult.addLookupListener(listener);
            }
        });
    }

    @Override
    protected Node getNodeForRow(int rowIndex) {
        if (support != null) {
            final int row = table.convertRowIndexToModel(rowIndex);
            if (row > 1) {
                int index = row - 2;
                final EditableClassroomTest<?, ?, ?> itemsModel = support.getModel().getItemsModel();
                final List l = itemsModel.getEditableStudents();
                if (l.size() > index) {
                    final EditableStudent es = (EditableStudent<?>) l.get(index);
                    return es.getNodeDelegate();
                }
            }
        }
        return null;
    }

    @Override
    protected void activatedNodes(List<Node> selected) {
        if (obj.isValid()) {
            Node ourNode = obj.getNodeDelegate();
            setActivatedNodes(new Node[]{ourNode});
            setIcon(ourNode.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16));
            updateName();
//            ourNode.addNodeListener(org.openide.nodes.NodeOp.weakNodeListener(listener, ourNode));
        }
    }

    @Override
    protected JPopupMenu createPopup(int modelCol, int modelRow, Point p, MouseEvent e) {
        final List<Action> l = Stream.concat(Utilities.actionsForPath("Loaders/application/betula-classroomtest-student-context/Actions").stream(),
                Utilities.actionsForPath("Loaders/text/betula-classtest-file+xml/Actions").stream())
                .collect(Collectors.toList());
        l.add(0, SystemAction.get(PasteAction.class));
        return Utilities.actionsToPopup(l.stream().toArray(Action[]::new), this);
    }

    private JLabel initLoading() {
        JLabel loadingLbl = new JLabel(NbBundle.getMessage(CloneableEditor.class, "LBL_EditorLoading")); // NOI18N
        loadingLbl.setOpaque(true);
        loadingLbl.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLbl.setBorder(new EmptyBorder(new Insets(11, 11, 11, 11)));
        loadingLbl.setVisible(false);
        return loadingLbl;
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
        final TopComponent tc = WindowManager.getDefault().findTopComponent("projectTabLogical_tc");
        if (tc != null && tc instanceof ExplorerManager.Provider && obj.isValid()) {
            try {
                ((ExplorerManager.Provider) tc).getExplorerManager().setSelectedNodes(new Node[]{obj.getNodeDelegate()});
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);CloneableEditor e;
            }
        }
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        EventQueue.invokeLater(() -> {
            synchronized (TC_TRACKER) {
                final boolean open = TC_TRACKER.isEmpty();
                TC_TRACKER.add(this);
                if (open) {
                    WindowManager.getDefault().findTopComponentGroup("ClasstestGroup").open();
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
                    WindowManager.getDefault().findTopComponentGroup("ClasstestGroup").close();
                }
            }
        });
    }

//    @Override
//    public void componentActivated() {
//        super.componentActivated();
////        EventQueue.invokeLater(() -> {
////            TopComponent etc = WindowManager.getDefault().findTopComponent("AssessTopComponent");
////            etc.open();
////            etc.requestVisible();
////        });
//    }
    @Override
    public void componentClosed() {
        if (ctEditorResult != null) {
            ctEditorResult.removeLookupListener(listener);
        }
        if (obj != null) {
            obj.removePropertyChangeListener(listener);
        }
    }

    @Override
    public void updateName() {
        if (callback != null) {
            Mutex.EVENT.writeAccess(() -> {
                TopComponent tc = callback.getTopComponent();
                if (obj != null && obj.isValid()) {
                    boolean modif = obj.isModified();
                    boolean readOnly = !obj.getPrimaryFile().canWrite();
                    String displayName = UIUtilities.findDisplayName(obj);
                    tc.setDisplayName(UIUtilities.annotateName(displayName, false, modif, readOnly));
                    tc.setHtmlDisplayName(UIUtilities.annotateName(displayName, true, modif, readOnly));
                }
            });
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(obj);
        out.writeInt(table.getSelectedRow());
        ExternalizableHighlighter[] arr = Arrays.stream(table.getHighlighters())
                .filter(ExternalizableHighlighter.class::isInstance)
                .toArray(ExternalizableHighlighter[]::new);
        out.writeObject(arr);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        obj = (ClasstestDataObject) in.readObject();
        if (obj == null) {
            throw new IOException();
        }
        int sel = in.readInt();
        if (sel >= 0) {
            table.getSelectionModel().setSelectionInterval(sel, sel);
        }
        ExternalizableHighlighter[] arr = (ExternalizableHighlighter[]) in.readObject();
        initializeComponent(arr);
        activatedNodes(Collections.EMPTY_LIST);
    }

    private class CopyCutSelection extends AbstractAction {

        private final boolean cut;

        private CopyCutSelection(boolean cut) {
            super("copy-cut-nodes");
            this.cut = cut;
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            final Node[] sel = getActivatedNodes();
            if (sel == null) {
                return;
            }
            final Transferable[] t = Arrays.stream(sel)
                    .map(n -> {
                        try {
                            return cut ? n.clipboardCut() : n.clipboardCopy();
                        } catch (IOException ex) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toArray(Transferable[]::new);
            final Transferable trans;
            if (t.length == 1) {
                trans = t[0];
            } else if (sel.length > 1) {
                trans = new ExTransferable.Multi(t);
            } else {
                trans = null;
            }
            if (trans != null) {
                final Clipboard c = Lookup.getDefault().lookup(Clipboard.class);
                if (c != null) {
                    c.setContents(trans, new StringSelection("")); // NOI18N
                }
            }
        }

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

    private final class Listener extends NodeAdapter implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            initTable();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateName();
        }

    }
}
