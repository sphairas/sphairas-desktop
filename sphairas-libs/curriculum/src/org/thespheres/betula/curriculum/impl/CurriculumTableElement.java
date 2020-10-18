/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.impl;

import org.thespheres.betula.curriculum.util.CurriculumTableActions;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.dnd.DropTarget;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.text.CloneableEditor;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.ui.swingx.treetable.NbSwingXTreeTableElement;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.ui.util.UIUtilities;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@MultiViewElement.Registration(
        displayName = "#CurriculumTableElement.displayname",
        //        iconBase = "org/thespheres/betula/termreport/resources/betulatrep2_16.png",
        mimeType = CurriculumDataObject.CURRICULUM_FILE_MIME,
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "CurriculumTableElement",
        position = 1000)
@NbBundle.Messages({"CurriculumTableElement.displayname=Übersicht"})
public final class CurriculumTableElement extends NbSwingXTreeTableElement implements MultiViewElement, Serializable {

    private final static Set<CurriculumTableElement> TC_TRACKER = new HashSet<>(2);
    protected CurriculumDataObject obj;
    protected CurriculumSupport support;
    private Lookup.Result<CurriculumTableActions> lkpResult;
    private final Listener listener = new Listener();
    private final CurriculumTableModel model = new CurriculumTableModel();

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public CurriculumTableElement() {
        super();
        this.addNodeDelegateToActivatedNodes = true;
        setDropTarget(true);
        treeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        toolbar.setLayout(new FlowLayout(FlowLayout.LEADING));
        Utilities.actionsForPath("Loaders/" + CurriculumDataObject.CURRICULUM_FILE_MIME + "/Toolbar").stream()
                .forEach(toolbar::add);
    }

    public CurriculumTableElement(Lookup lkp) throws IOException {
        this();
        obj = lkp.lookup(CurriculumDataObject.class);
        initializeComponent();
        activatedNodes(Collections.EMPTY_LIST);
    }

    @Override
    protected Node getNodeDelegate() {
        return obj != null ? obj.getNodeDelegate() : null;
    }

    @Override
    protected void initializeComponent() throws IOException {
        super.initializeComponent();
        obj.addPropertyChangeListener(listener);
        support = obj.getLookup().lookup(CurriculumSupport.class);
        lkpResult = obj.getLookup().lookupResult(CurriculumTableActions.class);
        lkpResult.addLookupListener(listener);

//        treeTable.setColumnFactory(support.getModel().createColumnFactory());
        final Lookup hlkp = Lookups.forPath("Editors/" + CurriculumDataObject.CURRICULUM_FILE_MIME + "/Highlighter");
        hlkp.lookupAll(HighlighterInstanceFactory.class).stream()
                .map(hlf -> hlf.createHighlighter(treeTable, this))
                .filter(Objects::nonNull)
                .forEach(treeTable::addHighlighter);
        initTable();
    }

    protected void initTable() {
        Mutex.EVENT.writeAccess(() -> {
            final CurriculumTableActionsImpl ac;
            if ((ac = getCurrentActions()) != null) {
                DropTarget dt = new DropTarget();
                try {
                    dt.addDropTargetListener(ac);
                    treeTable.setDropTarget(dt);
//                    scrollPane.setDropTarget(dt);
                } catch (TooManyListenersException ex) {
                    PlatformUtil.getCodeNameBaseLogger(CurriculumTableElement.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                }
//                scrollPane.setViewportView(treeTable);
                model.setEnv(obj);
                setModel(model);
                setName(obj.getNodeDelegate().getDisplayName());
//                ac.addUndoableEditListener(undoRedo);
            } else {
//                scrollPane.setViewportView(initLoading());
            }
        });
    }

    protected CurriculumTableActionsImpl getCurrentActions() {
        return lkpResult.allInstances().stream()
                .map(CurriculumTableActionsImpl.class::cast)
                .collect(CollectionUtil.singleOrNull());
    }

    protected JLabel initLoading() {
        JLabel loadingLbl = new JLabel(NbBundle.getMessage(CloneableEditor.class, "LBL_EditorLoading")); // NOI18N
        loadingLbl.setOpaque(true);
        loadingLbl.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLbl.setBorder(new EmptyBorder(new Insets(11, 11, 11, 11)));
        loadingLbl.setVisible(false);
        return loadingLbl;
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        EventQueue.invokeLater(() -> {
            synchronized (TC_TRACKER) {
                final boolean open = TC_TRACKER.isEmpty();
                TC_TRACKER.add(this);
                if (open) {
//                    WindowManager.getDefault().findTopComponentGroup("TermReportGroup").open();
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
//                    WindowManager.getDefault().findTopComponentGroup("TermReportGroup").close();
                }
            }
        });
    }

    @Override
    protected void updateName(final Node n) {
        if (callback != null && obj != null && obj.isValid()) {
            Mutex.EVENT.writeAccess(() -> {
                final TopComponent tc = callback.getTopComponent();
                boolean modif = obj.isModified();
                boolean readOnly = !obj.getPrimaryFile().canWrite();
                final String displayName = UIUtilities.findDisplayName(obj);
                tc.setDisplayName(UIUtilities.annotateName(displayName, false, modif, readOnly));
                tc.setHtmlDisplayName(UIUtilities.annotateName(displayName, true, modif, readOnly));
            });
        }
    }

    @Override
    protected void activatedNodes(final List<Node> sel) {
        //MoveUpAction, MoveDownAction accept only one activated node
        addNodeDelegateToActivatedNodes = sel == null || sel.isEmpty();
        super.activatedNodes(sel);
    }

    @Override
    public void componentClosed() {
        if (lkpResult != null) {
            lkpResult.removeLookupListener(listener);
        }
        if (obj != null) {
            obj.removePropertyChangeListener(listener);
        }
    }

////    @Override
//    protected JPopupMenu createPopup(int modelCol, int modelRow, Point p, MouseEvent e) {
//        final CurriculumTableActionsImpl cta;
//        if ((cta = getCurrentActions()) != null) {
//            final Course item = support.getModel().getItemAt(modelRow);
//            final Node cn = cta.getNode(item.getId());
//            int section = support.getModel().getColumnIndex(modelCol);
//            final Section sec = support.getModel().getItemsModel().getSections().get(section);
//            final Node sn = cta.getNode(sec);
//            final Action[] ca = support.getDataObject().getNodeDelegate().getActions(true);
//            final Lookup context = new ProxyLookup(cn.getLookup(), sn.getLookup(), cta.getLookup());
//            final Action[] ac = Stream.of(
//                    Arrays.stream(cn.getActions(true)),
//                    Arrays.stream(sn.getActions(true)),
//                    Arrays.stream(ca))
//                    /* .parallel() if you want*/
//                    .reduce(Stream::concat)
//                    .orElseGet(Stream::empty)
//                    .toArray(Action[]::new);
//            return Utilities.actionsToPopup(ac, context);
//        }
//        return null;
//    }
//    @Override
//    protected Node getNodeForRow(int rowIndex) {
//        if (support != null) {
//            final int row = table.convertRowIndexToModel(rowIndex);
//            final CurriculumTableActionsImpl ac = getCurrentActions();
//            if (ac != null) {
//                final Course item = support.getModel().getItemAt(row);
//                return ac.getNode(item.getId());
//            }
//        }
//        return null;
//    }
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        if (obj != null && obj.isValid()) {
            out.writeObject(obj);
            out.writeInt(treeTable.getSelectedRow());
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        final Object oi = in.readObject();
        if (oi instanceof CurriculumDataObject) {
            obj = (CurriculumDataObject) oi;
            int sel = in.readInt();
            if (sel >= 0) {
                treeTable.getSelectionModel().setSelectionInterval(sel, sel);
            }
        }
        initializeComponent();
        activatedNodes(Collections.EMPTY_LIST);
    }

    private final class Listener extends NodeAdapter implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            initTable();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateName(getNodeDelegate());
        }

    }
}
