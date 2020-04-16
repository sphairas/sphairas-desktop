/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.unitsui;

import org.thespheres.betula.ui.swingx.AbstractTableElement;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeListener;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.MultiUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages("UnitsUIElement.inititalization.error=Could not open TargetsForStudentsElement. Node display name is {0}")
@MultiViewElement.Registration(mimeType = "application/betula-unit-data", persistenceType = TopComponent.PERSISTENCE_ALWAYS, displayName = "Gruppen", preferredID = "UnitsUIElementME", position = 5000)
public class UnitsUIElement extends AbstractTableElement implements PropertyChangeListener, Serializable {

    private final static Set<UnitsUIElement> TC_TRACKER = new HashSet<>(2);
//    public static final String DEFAULTTARGETTYPE = "zeugnisnoten";
    private AbstractUnitOpenSupport.Env env;
    private final javax.swing.JScrollPane scrollPane;
    private final UnitsUITableModel currentModel = new UnitsUITableModel();
    private AbstractUnitOpenSupport support;
//    private int savedSelectedRow;
    private final RemoteUnitsModel.INITIALISATION[] currentStage = new RemoteUnitsModel.INITIALISATION[]{null};
    private NodeListener nl;
    private final RequestProcessor rp = new RequestProcessor(UnitsUIElement.class);

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public UnitsUIElement() {
        scrollPane = new javax.swing.JScrollPane();
        toolbar.setLayout(new FlowLayout(FlowLayout.RIGHT));
        setLayout(new BorderLayout());
        setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    public UnitsUIElement(Lookup context) throws IOException {
        this();
        env = context.lookup(AbstractUnitOpenSupport.Env.class);
        if (env == null) {
            throw new IOException();
        }
        initializeComponent();
        activatedNodes(Collections.EMPTY_LIST);
    }

    @Messages("UnitsUIElement.quietInit.label=Lädt...")
    private JComponent quietInit() {
        final JLabel loadingLbl = new JLabel(NbBundle.getMessage(UnitsUIElement.class, "UnitsUIElement.quietInit.label")); // NOI18N
        loadingLbl.setOpaque(true);
        loadingLbl.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLbl.setBorder(new EmptyBorder(new Insets(11, 11, 11, 11)));
        return loadingLbl;
    }

    private void initializeComponent() throws IOException {
        if (!EventQueue.isDispatchThread()) {
            throw new IOException();
        }
        support = (AbstractUnitOpenSupport) env.findCloneableOpenSupport();
        env.addPropertyChangeListener(this);
        //init table
        table.setColumnFactory(new UnitsUIColumnFactory(this));
//        CellIconHighlighter ih = new CellIconHighlighter();
//        MimeLookup.getLookup("application/betula-unit-context").lookupAll(HighlighterInstanceFactory.class).forEach(hlf -> {
//            final Highlighter hl = hlf.createHighlighter(table, this);
//            if (hl instanceof CellIconHighlighterDelegate) {
//                ih.addIconHighlighterDelegate((CellIconHighlighterDelegate) hl);
//            } else {
//                table.addHighlighter(hl);
//            }
//        });
//        table.addHighlighter(ih);
        //load
        final RemoteUnitsModel rm;
        if (support instanceof PrimaryUnitOpenSupport) {
            rm = ((PrimaryUnitOpenSupport) support).getRemoteUnitsModel(RemoteUnitsModel.INITIALISATION.STUDENTS);
        } else if (support instanceof MultiUnitOpenSupport) {
            rm = ((MultiUnitOpenSupport) support).getRemoteUnitsModel(RemoteUnitsModel.INITIALISATION.STUDENTS);
        } else {
            rm = support.getRemoteUnitsModel();
        }
        final boolean init = rm.getInitialization().satisfies(RemoteUnitsModel.INITIALISATION.STUDENTS);
        if (init) {
            doInit(rm);
        }
        rm.addPropertyChangeListener(this);
        scrollPane.setViewportView(quietInit());
    }

    //Should be called only once
    private void doInit(final RemoteUnitsModel rm) {
        final RequestProcessor.Task initTask = rp.post(() -> {
            try {
                currentModel.initialize(rm);
            } catch (IOException ex) {
                handleInitializationException(ex);
            }
        });
        initTask.addTaskListener(t -> {
            EventQueue.invokeLater(() -> {
                try {
                    initTable();
                } catch (IOException ex) {
                    handleInitializationException(ex);
                }
            });
        });
    }

    void initTable() throws IOException {
//        cellHighlighter.setModel(currentModel);
        scrollPane.setViewportView(table);
//        initTermAndTargetBoxes();
        table.setModel(currentModel);
//        if (savedSelectedRow != -1 && savedSelectedRow < table.getRowCount()) {
//            table.getSelectionModel().setSelectionInterval(0, savedSelectedRow);
//        }
//        savedSelectedRow = -1;
//        support.getRemoteUnitsModel().addPropertyChangeListener(currentModel);

        //TODO: remove undoRedo wenn, componend close
        //TODO: update when targets added /removed
        support.getRemoteUnitsModel().getTargets().stream()
                .forEach(rtad -> rtad.addUndoableEditListener(undoRedo));
        support.addUndoableEditListener(undoRedo);

        activatedNodes(Collections.EMPTY_LIST);
    }

    @Override
    public void componentClosed() {
        if (support != null) {
            try {
                support.getRemoteUnitsModel().getTargets().stream()
                        .forEach(rtad -> rtad.removeUndoableEditListener(undoRedo));
                env.removePropertyChangeListener(this);
            } catch (IOException ex) {
            }
        }
    }

    protected CloneableOpenSupport.Env getEnv() {
        return env;
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        EventQueue.invokeLater(() -> {
            synchronized (TC_TRACKER) {
                final boolean open = TC_TRACKER.isEmpty();
                TC_TRACKER.add(this);
                if (open) {
                    WindowManager.getDefault().findTopComponentGroup("TargetsForStudentsGroup").open();
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
                    WindowManager.getDefault().findTopComponentGroup("TargetsForStudentsGroup").close();
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final String prop = evt.getPropertyName();
        if (evt.getSource() instanceof RemoteUnitsModel && prop.equals(RemoteUnitsModel.PROP_INITIALISATION)) {
            final RemoteUnitsModel.INITIALISATION stage = (RemoteUnitsModel.INITIALISATION) evt.getNewValue();
            if (stage.isError()) {
                synchronized (currentStage) {
                    currentStage[0] = stage;
                }
                closeTC();
            } else if (!stage.satisfies(RemoteUnitsModel.INITIALISATION.PRIORITY)) {
                return;
            }
            synchronized (currentStage) {
                RemoteUnitsModel.INITIALISATION current;
                if ((current = currentStage[0]) != null && current.satisfies(RemoteUnitsModel.INITIALISATION.PRIORITY)) {
//                    updateComboBoxes();
                } else if (stage.satisfies(RemoteUnitsModel.INITIALISATION.PRIORITY)) {
                    final RemoteUnitsModel rm = (RemoteUnitsModel) evt.getSource();
                    doInit(rm);
                }
                currentStage[0] = stage;
            }
        } else if (evt.getSource() instanceof AbstractUnitOpenSupport.Env && AbstractUnitOpenSupport.Env.PROP_VALID.equals(evt.getNewValue())) {
            boolean valid = env != null && env.isValid();
            if (!valid) {
                closeTC();
            }
        } else if (evt.getSource() instanceof RemoteUnitsModel && (prop.equals(RemoteUnitsModel.PROP_TERMS) || prop.equals(RemoteUnitsModel.PROP_TARGETS))) {
//            updateComboBoxes();
        }
    }

    private void closeTC() {
        if (callback != null) {
            Mutex.EVENT.writeAccess(() -> {
                if (callback.getTopComponent().close()) {
                    if (currentModel != null) {
//                        targetTypeBox.removeItemListener(currentModel);
//                        termBox.removeItemListener(currentModel);
                        try {
//                            support.getRemoteUnitsModel().removePropertyChangeListener(currentModel);
                            support.getRemoteUnitsModel().removePropertyChangeListener(this);
                        } catch (IOException ex) {
                        }
                    }
//                    currentModel = null;
                }
            });
        }
    }

    private void handleInitializationException(Exception initEx) {
        final String msg = NbBundle.getMessage(UnitsUIElement.class, "UnitsUIElement.inititalization.error", support.getNodeDelegate().getDisplayName());
        PlatformUtil.getCodeNameBaseLogger(UnitsUIElement.class).log(Level.SEVERE, msg, initEx);
        if (callback != null) {
            callback.getTopComponent().close();
        } else {
            close();
        }
    }

    @Override
    protected Node getNodeForRow(int rowIndex) {
        int row = table.convertRowIndexToModel(rowIndex);
        if (currentModel != null) {
//            final RemoteStudent rs = currentModel.getStudentAt(row);
//            return studNodes.computeIfAbsent(rs.getStudentId(), (si) -> new AbstractNode(Children.LEAF, Lookups.singleton(rs)));
        }
        return null;
    }

    @Override
    protected final void activatedNodes(List<Node> sel) {
        if (env.isValid()) {
            final List<Node> selection = new ArrayList<>(sel);
            if (currentModel != null) {
//                selection.add(currentModel.getNodeDelegate());
            }
            final Node ourNode = ((AbstractUnitOpenSupport) env.findCloneableOpenSupport()).getNodeDelegate();
            selection.add(ourNode);
            setActivatedNodes(selection.toArray(new Node[selection.size()]));
            setIcon(ourNode.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16));
            updateName();
        }
    }

    @Override
    protected void updateName() {
        if (callback != null) {
            Mutex.EVENT.writeAccess(() -> {
                TopComponent tc = callback.getTopComponent();
                if (getEnv() != null && getEnv().isValid()) {
                    final Node n = ((AbstractUnitOpenSupport) getEnv().findCloneableOpenSupport()).getNodeDelegate();
                    if (nl == null) {
                        class NL extends NodeAdapter {

                            @Override
                            public void propertyChange(PropertyChangeEvent ev) {
                                if (Node.PROP_DISPLAY_NAME.equals(ev.getPropertyName())) {
                                    updateName();
                                }
                            }

                        }
                        n.addNodeListener(org.openide.nodes.NodeOp.weakNodeListener(nl = new NL(), n));
                    }
                    tc.setDisplayName(n.getDisplayName());
                    tc.setHtmlDisplayName(n.getHtmlDisplayName());
                    tc.setIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
                }
            });
        }
    }

    @Override
    protected JPopupMenu createPopup(int modelCol, int modelRow, Point p, MouseEvent e) {
//        if (currentModel != null && modelCol > 0) { //isPopupAllowed()
//            RemoteTargetAssessmentDocument rtad = currentModel.getRemoteTargetAssessmentDocumentAt(modelCol - 1);
//            RemoteStudent rs = currentModel.getStudentAt(modelRow);
//            Term term = currentModel.getCurrentIndentity();
//            String type = currentModel != null ? currentModel.getCurrentTargetType() : null;
//            TargetAssessmentSelectionProvider sp = new TargetAssessmentSelectionProvider(rtad, rs, support, term, type);
//            if (rtad != null) {
//                Action[] a = Utilities.actionsForPath("Loaders/application/betula-unit-context/Actions").stream()
//                        .toArray(Action[]::new);
//                return Utilities.actionsToPopup(a, sp.getLookup());
//            }
//        }
        return null;
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
        super.readExternal(oi);
        Object o = oi.readObject();
        if (o instanceof AbstractUnitOpenSupport.Env) {
            AbstractUnitOpenSupport.Env r = (AbstractUnitOpenSupport.Env) o;
            if (r.isValid()) {
                env = r;
            } else {
                throw new IOException();
            }
//            o = oi.readObject();
//            if (o != null && o instanceof TermId) {
//                this.savedTermId = (TermId) o;
//            }
//            o = oi.readObject();
//            if (o != null && o instanceof String) {
//                savedTargetType = (String) o;
//            }
            initializeComponent();
            activatedNodes(Collections.EMPTY_LIST);
        } else {
            throw new IOException();
        }
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException {
        super.writeExternal(oo);
        if (env != null && env.isValid()) {
//            int srows = table.getSelectedRow();
//            if (srows == -1 && savedSelectedRow != -1) {
//                srows = savedSelectedRow;
//            }
            oo.writeObject(env);
//            oo.writeInt(srows);
        }
    }

}
