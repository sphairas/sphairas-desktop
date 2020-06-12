/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.swingx;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.jdesktop.swingx.JXTable;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.MouseUtils;
import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Utilities;
import org.openide.windows.CloneableTopComponent;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractTableElement extends CloneableTopComponent implements MultiViewElement {

    protected final JXTable table;
    protected final JToolBar toolbar;
    protected MultiViewElementCallback callback;
    private final PopupAdapter popupListener;
    private final DefaultAction defaultAction;
    protected final UndoRedo.Manager undoRedo = new UndoRedo.Manager();
    protected final SelectionListener selectionListener;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    protected AbstractTableElement() {
        this(new org.jdesktop.swingx.JXTable());
    }

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    protected AbstractTableElement(final org.jdesktop.swingx.JXTable tblCmp) {
        table = tblCmp;
        toolbar = new javax.swing.JToolBar();
        popupListener = new PopupAdapter();
        defaultAction = new DefaultAction();
        toolbar.setRollover(true);
        toolbar.setFloatable(false);
        table.addMouseListener(popupListener);
        table.addMouseListener(defaultAction);
        addMouseListener(defaultAction);
        table.setHorizontalScrollEnabled(true);
        table.setColumnControlVisible(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionListener = new SelectionListener();
        table.getSelectionModel().addListSelectionListener(selectionListener);
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolbar;
    }

    public JXTable getTable() {
        return table;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
        updateName();
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public UndoRedo.Manager getUndoRedo() {
        return undoRedo;
    }

    protected abstract Node getNodeForRow(int rowIndex);

    protected abstract void activatedNodes(List<Node> selected);

    protected abstract JPopupMenu createPopup(int modelCol, int modelRow, Point p, MouseEvent e);

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
//        EventQueue.invokeLater(() -> WindowManager.getDefault().findTopComponent("navigatorTC").open());
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
    }

    protected void updateName() {
    }

    private void showPopup(int x, int y, final JPopupMenu popup) {
        if (popup != null && popup.getSubElements().length > 0) {
            final PopupMenuListener p = new PopupMenuListener() {

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    popup.removePopupMenuListener(this);
                    table.requestFocus();
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            };
            popup.addPopupMenuListener(p);
            popup.show(table, x, y);
        }
    }

    private class DefaultAction extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            int selRow = table.rowAtPoint(e.getPoint());

            if ((selRow != -1) && SwingUtilities.isLeftMouseButton(e) && MouseUtils.isDoubleClick(e)) {
                if (!table.getSelectionModel().isSelectedIndex(selRow)) {
                    table.getSelectionModel().clearSelection();
                    table.getSelectionModel().setSelectionInterval(selRow, selRow);
                }
                int row = table.convertRowIndexToModel(selRow);
                final Node node = getNodeForRow(row);
                if (node == null) {
                    return;
                }
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
                }
            }
        }

    }

    private class PopupAdapter extends MouseUtils.PopupMouseAdapter {

        @Override
        protected void showPopup(MouseEvent e) {
            int selRow = table.rowAtPoint(e.getPoint());

            if (selRow != -1) {
                if (!table.getSelectionModel().isSelectedIndex(selRow)) {
                    table.getSelectionModel().clearSelection();
                    table.getSelectionModel().setSelectionInterval(selRow, selRow);
                }
            } else {
                table.getSelectionModel().clearSelection();
            }
            Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), table);
            int column = table.convertColumnIndexToModel(table.columnAtPoint(p));
            final int rap = table.rowAtPoint(p);
            JPopupMenu pop = null;
            if (rap != -1) {
                int row = table.convertRowIndexToModel(rap);
                pop = AbstractTableElement.this.createPopup(column, row, p, e);
            }
            if (pop != null) {
                AbstractTableElement.this.showPopup(p.x, p.y, pop);
                e.consume();
            }
        }

    }

//    public static Action createPasteAction(Transferable t, Node n) {
//        final NodeTransfer.Paste paste = NodeTransfer.findPaste(t);
//        if (paste != null) {
//            final PasteType[] types = paste.types(n);
//            if (types.length == 1) {
//                class SingleAction extends AbstractAction {
//
//                    private SingleAction() {
//                        super("paste-single");
//                    }
//
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        try {
//                            types[0].paste();
//                        } catch (IOException ex) {
//                        }
//                    }
//
//                }
//            } else if (types.length > 1) {
//                class MenuPasteAction extends AbstractAction implements Presenter.Popup {
//
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        throw new UnsupportedOperationException("Should never happen.");
//                    }
//
//                    @Override
//                    public JMenuItem getPopupPresenter() {
//                        return new JMenuItem(SystemAction.get(PasteAction.class).) {
//
//                        };
//                    }
//
//                }
//                class PasteActions extends AbstractAction {
//
//                    final PasteType paste;
//
//                    PasteActions(PasteType paste) {
//                        super(paste.getName());
//                        this.paste = paste;
//                    }
//
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        try {
//                            paste.paste();
//                        } catch (IOException ex) {
//                        }
//                    }
//
//                }
//                Action[] ac = Arrays.stream(types)
//                        .map(pt -> new PasteActions(pt))
//                        .toArray(Action[]::new);
//                Point loc = SwingUtilities.convertPoint(cmp, dtde.getLocation(), table);
//                JPopupMenu popup = Utilities.actionsToPopup(ac, table);
//                final PopupMenuListener pl = new PopupMenuListener() {
//
//                    @Override
//                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
//                    }
//
//                    @Override
//                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
//                        popup.removePopupMenuListener(this);
//                        table.requestFocus();
//                    }
//
//                    @Override
//                    public void popupMenuCanceled(PopupMenuEvent e) {
//                    }
//                };
//                popup.addPopupMenuListener(pl);
//                popup.show(table, loc.x, loc.y);
//            }
//        }
//    }
    protected class SelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            final int sel[] = table.getSelectedRows();
            final List<Node> nodes = Arrays.stream(sel)
                    .map(table::convertRowIndexToModel)
                    .mapToObj(AbstractTableElement.this::getNodeForRow)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            activatedNodes(nodes);
        }

        public List<Node> currentSelection() {
            final int sel[] = table.getSelectedRows();
            return Arrays.stream(sel)
                    .mapToObj(AbstractTableElement.this::getNodeForRow)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

    }
}
