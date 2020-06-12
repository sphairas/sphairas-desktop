/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.swingx.treetable;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.TreePath;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.JXTreeTable;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.MouseUtils;
import org.openide.explorer.view.NodePopupFactory;
import org.openide.explorer.view.NodeRenderer;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
public abstract class NbSwingXTreeTableElement extends CloneableTopComponent implements Serializable {

    protected MultiViewElementCallback callback;
    protected final JToolBar toolbar;
    protected final JXTreeTable treeTable;
    private final JScrollPane scrollPane;
    private final PopupAdapter popupListener;
    private final DefaultAction defaultAction;
    private final NodePopupFactory nodePopupFactory;
    private final SelectionListener selectionListener;
    private NbSwingXTreeTableRestore restore;
    private boolean dropActive = true;
    private TreeViewDropSupport dropSupport;
    private boolean dropTargetPopupAllowed = true;
    private int allowedDropActions = DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE;
    private Object jxTree;
    NbSwingXTreeTableModel model;
    protected final PasteDelegate pasteDelegate = new PasteDelegate();
    protected final CutCopyDelegate copyDelegate = new CutCopyDelegate(DefaultEditorKit.copyAction, false);
    protected final CutCopyDelegate cutDelegate = new CutCopyDelegate("cut", true);
    protected final DeleteDelegate deleteDelegate = new DeleteDelegate();
    protected boolean addNodeDelegateToActivatedNodes = false; //Should be false; otherwise some node action will not work properly, like MoveUp, MoveDown

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    protected NbSwingXTreeTableElement() {
        treeTable = new JXTreeTable();
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        scrollPane = new JScrollPane();
        nodePopupFactory = new NodePopupFactory();
//        toolbar.setRollover(true);
//        toolbar.add(Box.createHorizontalGlue()); // After this every component will be added to the right 
        setLayout(new BorderLayout());
        setBorder(null);
        treeTable.setTreeCellRenderer(new NodeRenderer());
        treeTable.setHorizontalScrollEnabled(true);
        popupListener = new PopupAdapter();
        treeTable.addMouseListener(popupListener);
        addMouseListener(popupListener);
        defaultAction = new DefaultAction();
        treeTable.addMouseListener(defaultAction);
        addMouseListener(defaultAction);
        selectionListener = new SelectionListener();
        treeTable.getSelectionModel().addListSelectionListener(selectionListener);
        treeTable.getActionMap().put("org.openide.actions.PopupAction", new PopupAction());
        treeTable.getActionMap().put("delete", deleteDelegate);
        getActionMap().put("cut", cutDelegate);
        getActionMap().put(DefaultEditorKit.copyAction, copyDelegate);
        getActionMap().put("paste", pasteDelegate);
        scrollPane.setViewportView(treeTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    protected void setModel(final NbSwingXTreeTableModel root) {
        model = root;
        treeTable.setColumnFactory(model.createColumnFactory());
        treeTable.setTreeTableModel(model);
    }

    private void checkModel() {
        if (model == null) {
            throw new IllegalStateException("No NbSwingXTreeTableModel set on " + toString());
        }
    }

    protected void initializeComponent() throws IOException {

        if (restore != null) {
            final class PCL implements PropertyChangeListener, Runnable {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
//                    if (RemoteMessagesModel.PROP_INIT.equals(evt.getPropertyName()) && restore != null) {
//                        //Need to post in rl queue to ensure called after tree children initialization done.
////                        remoteModel.getRemoteLookup().getRequestProcessor().post(this);
////                        remoteModel.removePropertyChangeListener(this);
//                    }
                }

                @Override
                public void run() {
                    restore.restoreView();
                    restore = null;
                }

            }
//            remoteModel.addPropertyChangeListener(new PCL());
        }
    }

    JXTree getTree() {
        if (jxTree == null) {
            try {
                final Field f = JXTreeTable.class.getDeclaredField("renderer");
                f.setAccessible(true);
                jxTree = (JXTree) f.get(treeTable);
            } catch (ClassCastException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                jxTree = new IllegalStateException(ex);
            }
        }
        if (jxTree instanceof IllegalStateException) {
            throw (IllegalStateException) jxTree;
        }
        return (JXTree) jxTree;
    }

    public boolean isExpanded(Node n) {
        checkModel();
        return treeTable.isExpanded(model.getTreePath(n));
    }

    public void expandNode(final Node n) {
        checkModel();
        EventQueue.invokeLater(() -> {
            final TreePath p = model.getTreePath(n);
            treeTable.expandPath(p);
        });
    }

    public void setDropTarget(boolean state) {
        if (dropSupport == null) {
            dropSupport = new TreeViewDropSupport(this, treeTable, dropTargetPopupAllowed);
        }
        dropActive = state;
        if (dropSupport != null) {
            dropSupport.activate(dropActive);
        }
    }

    public boolean isDropActive() {
        return dropActive;
    }

    /**
     * Actions constants comes from {@link java.awt.dnd.DnDConstants}. All
     * actions are allowed by default.
     *
     * @return int representing set of actions which are allowed when dropping
     * into the asociated component.
     */
    public int getAllowedDropActions() {
        return allowedDropActions;
    }

    /**
     * Sets allowed actions for dropping.
     *
     * @param actions new allowed drop actions, using
     * {@link java.awt.dnd.DnDConstants}
     */
    public void setAllowedDropActions(int actions) {
        // PENDING: check parameters
        allowedDropActions = actions;
    }

    protected void activatedNodes(final List<Node> sel) {
        final List<Node> selection = new ArrayList<>(sel);
        final Node n = getNodeDelegate();
        if (n != null && addNodeDelegateToActivatedNodes) {
            selection.add(n);
            setIcon(n.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16));
        }
        setActivatedNodes(selection.toArray(new Node[selection.size()]));
        updateName(n);
        deleteDelegate.updateEnabled(sel);
        cutDelegate.updateEnabled(sel);
        copyDelegate.updateEnabled(sel);
    }

    protected void updateName(Node n) {
        if (callback != null && n != null) {
            Mutex.EVENT.writeAccess(() -> {
                final TopComponent tc = callback.getTopComponent();
                tc.setDisplayName(n.getDisplayName());
                tc.setHtmlDisplayName(n.getHtmlDisplayName());
                tc.setIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
            });
        }
    }

    protected abstract Node getNodeDelegate();

    public JComponent getVisualRepresentation() {
        return this;
    }

    public JComponent getToolbarRepresentation() {
        return toolbar;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
        updateName(getNodeDelegate());
    }

    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
//        model.initialize(callback);
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
    }

    private Node getNodeForRow(int rowIndex) {
        int row = treeTable.convertRowIndexToModel(rowIndex);
        TreePath tp = treeTable.getPathForRow(row);
        if (tp != null) {
            return Visualizer.findNode(tp.getLastPathComponent());
        }
        return null;
    }

    private JPopupMenu createPopup(Point p) {
        final int sel[] = treeTable.getSelectedRows();
        final Node[] nodes = Arrays.stream(sel)
                .mapToObj(NbSwingXTreeTableElement.this::getNodeForRow)
                .filter(Objects::nonNull)
                .toArray(Node[]::new);
        p = SwingUtilities.convertPoint(this, p, treeTable);
        int column = treeTable.columnAtPoint(p);
        int row = treeTable.rowAtPoint(p);
        return nodePopupFactory.createPopupMenu(row, column, nodes, treeTable);
    }

    private Point getPositionForPopup() {
        int i = treeTable.getSelectionModel().getLeadSelectionIndex();
        if (i < 0) {
            return null;
        }
        int j = treeTable.getColumnModel().getSelectionModel().getLeadSelectionIndex();
        if (j < 0) {
            j = 0;
        }
        Rectangle rect = treeTable.getCellRect(i, j, true);
        if (rect == null) {
            return null;
        }

        Point p = new Point(rect.x + rect.width / 3, rect.y + rect.height / 2);
        // bugfix #36984, convert point by TableView.this
        return SwingUtilities.convertPoint(treeTable, p, scrollPane);
    }

    void showPopup(int x, int y, JPopupMenu popup) {
        if ((popup != null) && (popup.getSubElements().length > 0)) {
            final PopupMenuListener p = new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    popup.removePopupMenuListener(this);
                    treeTable.requestFocus();
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            };
            popup.addPopupMenuListener(p);
            popup.show(this, x, y);
        }
    }

    private Point mouseEventToPoint(MouseEvent e) {
        int selRow = treeTable.rowAtPoint(e.getPoint());
        if (selRow != -1) {
            if (!treeTable.getSelectionModel().isSelectedIndex(selRow)) {
                treeTable.getSelectionModel().clearSelection();
                treeTable.getSelectionModel().setSelectionInterval(selRow, selRow);
            }
        } else {
            treeTable.getSelectionModel().clearSelection();
        }
        return SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), scrollPane);
    }

    private class DeleteDelegate extends AbstractAction {

        private void updateEnabled(final List<Node> sel) {
            final boolean ena = sel.stream()
                    .allMatch(Node::canDestroy);
            setEnabled(ena);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            selectionListener.currentSelection().stream()
                    .filter(Node::canDestroy)
                    .forEach(n -> {
                        try {
                            n.destroy();
                        } catch (IOException ex) {
                            PlatformUtil.getCodeNameBaseLogger(NbSwingXTreeTableElement.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                        }
                    });
        }

    }

    private class PasteDelegate extends AbstractAction {

        private void updateEnabled(final List<Node> sel) {
//            final boolean ena = sel.stream()
//                    .allMatch(Node::canDest);
//            setEnabled(ena);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
//            selectionListener.currentSelection().stream()
//                    .filter(Node::canDestroy)
//                    .forEach(n -> {
//                        try {
//                            n.destroy();
//                        } catch (IOException ex) {
//                            PlatformUtil.getCodeNameBaseLogger(getClass()).log(Level.FINE, ex.getLocalizedMessage(), ex);
//                        }
//                    });
        }
    }

    private class CutCopyDelegate extends AbstractAction {

        private final boolean cut;

        CutCopyDelegate(final String name, final boolean cut) {
            super(name);
            this.cut = cut;
        }

        private void updateEnabled(final List<Node> sel) {
            final boolean ena = sel.stream()
                    .allMatch(n -> cut ? n.canCut() : n.canCopy());
            setEnabled(ena);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            final List<Node> l = selectionListener.currentSelection();
            Transferable t = null;
            if (l.size() == 1) {
                final Node n = l.get(0);
                try {
                    t = cut ? n.clipboardCut() : n.clipboardCopy();
                } catch (IOException e) {
                    PlatformUtil.getCodeNameBaseLogger(NbSwingXTreeTableElement.class).log(Level.WARNING, null, e);
                }
            } else if (!l.isEmpty()) {
                final Transferable[] arr = l.stream()
                        .map(n -> {
                            try {
                                return cut ? n.clipboardCut() : n.clipboardCopy();
                            } catch (IOException e) {
                                PlatformUtil.getCodeNameBaseLogger(NbSwingXTreeTableElement.class).log(Level.WARNING, null, e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toArray(Transferable[]::new);
                if (arr.length != 0) {
                    t = new ExTransferable.Multi(arr);
                }
            }
            if (t != null) {
                Clipboard clipboard = getClipboard();
                if (clipboard != null) {
                    clipboard.setContents(t, new StringSelection(""));
                }
            }
        }
    }

    static Clipboard getClipboard() {
        Clipboard c = Lookup.getDefault().lookup(Clipboard.class);
        if (c == null) {
            c = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        return c;
    }

    private class SelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent evt) {
            if (evt.getValueIsAdjusting()) {
                return;
            }
            final List<Node> nodes = currentSelection();
            activatedNodes(nodes);
        }

        List<Node> currentSelection() {
            final int sel[] = treeTable.getSelectedRows();
            return Arrays.stream(sel)
                    .mapToObj(NbSwingXTreeTableElement.this::getNodeForRow)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    private class PopupAction extends AbstractAction implements Runnable {

        @Override
        public void actionPerformed(ActionEvent evt) {
            SwingUtilities.invokeLater(this);
        }

        @Override
        public void run() {
            Point p = getPositionForPopup();
            if (p == null) {
                return;
            }
            JPopupMenu pop = createPopup(p);
            showPopup(p.x, p.y, pop);
        }
    };

    private class DefaultAction extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            int selRow = treeTable.rowAtPoint(e.getPoint());

            if ((selRow != -1) && SwingUtilities.isLeftMouseButton(e) && MouseUtils.isDoubleClick(e)) {
                // Default action.
                TreePath selPath = treeTable.getPathForLocation(e.getX(), e.getY());
                Node node = Visualizer.findNode(selPath.getLastPathComponent());
                Action ac = node.getPreferredAction();
                if (ac instanceof ContextAwareAction) {
                    Action contextInstance = ((ContextAwareAction) ac).createContextAwareInstance(getLookup()); //node.getLookup());
                    ac = contextInstance;
                }
                if (ac != null) {
                    if (ac.isEnabled()) {
                        ac.actionPerformed(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                    } else {
                        Utilities.disabledActionBeep();
                    }
                    e.consume();
                    return;
                }

                if (treeTable.isExpanded(selRow)) {
                    treeTable.collapseRow(selRow);
                } else {
                    treeTable.expandRow(selRow);
                }
            }
        }

    }

    private class PopupAdapter extends MouseUtils.PopupMouseAdapter {

        @Override
        protected void showPopup(MouseEvent e) {
            Point p = mouseEventToPoint(e);
            JPopupMenu pop = createPopup(p);
            NbSwingXTreeTableElement.this.showPopup(p.x, p.y, pop);
            e.consume();
        }

    }
}
