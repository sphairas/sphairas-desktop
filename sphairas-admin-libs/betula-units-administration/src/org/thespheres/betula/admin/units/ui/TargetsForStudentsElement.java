/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui;

import org.thespheres.betula.ui.swingx.AbstractTableElement;
import org.thespheres.betula.admin.units.MoveStudentsToTargetDropSupport;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultEditorKit;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.awt.Actions;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeListener;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.MultiUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.admin.units.TargetAssessmentSelectionProvider;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.ui.swingx.CellIconHighlighter;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.ui.swingx.CellIconHighlighterDelegate;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages("TargetsForStudentsElement.inititalization.error=Could not open TargetsForStudentsElement. Node display name is {0}")
@MultiViewElement.Registration(mimeType = "application/betula-unit-data", persistenceType = TopComponent.PERSISTENCE_ALWAYS, displayName = "Fächer", preferredID = "TargetsForStudentsME", position = 1000)
public class TargetsForStudentsElement extends AbstractTableElement implements PropertyChangeListener, Serializable {

    private final static AtomicInteger rc = new AtomicInteger(1);

    private final static Set<TargetsForStudentsElement> TC_TRACKER = new HashSet<>(2);
//    public static final String DEFAULTTARGETTYPE = "zeugnisnoten";
    private AbstractUnitOpenSupport.Env env;
    private final javax.swing.JScrollPane scrollPane;
    private final JXComboBox targetTypeBox;
    private final JXComboBox termBox;
    private final ColumnSearchComponent columnSearchField;
    private TargetsForStudentsModel currentModel;
    private final DefaultComboBoxModel targetTypeBoxModel = new DefaultComboBoxModel();
    private final DefaultComboBoxModel termBoxModel = new DefaultComboBoxModel();
    private TermId savedTermId;
    private String savedTargetType;
    private final StringValue termStringValue = (value) -> {
        if (value instanceof Term) {
            Term t = (Term) value;
            return t.getDisplayName();
        }
        return null;
    };
    private final StringValue targetTypeStringValue = (value) -> {
        if (value instanceof String) {
            String s = (String) value;
            return StringUtils.capitalize(s);
        }
        return null;
    };
    private AbstractUnitOpenSupport support;
    private final int allowedDropActions = DnDConstants.ACTION_COPY; // | DnDConstants.ACTION_REFERENCE;
//    private MoveStudentsToTargetDropSupport dropSupport;
    private final StudentValuesToolTipHighlighter cellHighlighter;
    private final HashMap<StudentId, AbstractNode> studNodes = new HashMap<>();
    private int savedSelectedRow;
    private final RemoteUnitsModel.INITIALISATION[] currentStage = new RemoteUnitsModel.INITIALISATION[]{null};
    private NodeListener nl;
    private String defaultSuffix;
    private MListener mListener;

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public TargetsForStudentsElement() {
        super(new TableExt());
//        LogManager.getLogManager().getLogger("org.netbeans.core.TimableEventQueue").setLevel(Level.FINEST);
//        String sysProp = System.getProperty("org.openide.loaders.FolderList.refresh.interval");
        cellHighlighter = new StudentValuesToolTipHighlighter(HighlightPredicate.ALWAYS);
        scrollPane = new javax.swing.JScrollPane();
        targetTypeBox = new JXComboBox();
        targetTypeBox.setModel(targetTypeBoxModel);
        targetTypeBox.setEditable(false);
        targetTypeBox.setRenderer(new DefaultListRenderer(targetTypeStringValue));
        termBox = new JXComboBox();
        termBox.setModel(termBoxModel);
        termBox.setEditable(false);
        termBox.setRenderer(new DefaultListRenderer(termStringValue));
        columnSearchField = new ColumnSearchComponent(table);
//        toolbar.add(Box.createHorizontalGlue()); // After this every component will be added to the right 
        //If no Layout set ==> problem in ubuntu with ui.., jtextfield
        toolbar.setLayout(new FlowLayout(FlowLayout.RIGHT));
//        toolbar.add(columnSearchField.getLabel());
        toolbar.add(columnSearchField);
        toolbar.add(targetTypeBox);
        toolbar.add(termBox);
        setLayout(new BorderLayout());
        setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        table.getActionMap().put(DefaultEditorKit.pasteAction, Actions.forID("System", "org.openide.actions.PasteAction"));
    }

    private JComponent quietInit() {
        final JLabel loadingLbl = new JLabel(NbBundle.getMessage(TargetsForStudentsElement.class, "TargetsforStudentsElement.quietInit.label")); // NOI18N
        loadingLbl.setOpaque(true);
        loadingLbl.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLbl.setBorder(new EmptyBorder(new Insets(11, 11, 11, 11)));
        return loadingLbl;
    }

    public TargetsForStudentsElement(Lookup context) throws IOException {
        this();
        env = context.lookup(AbstractUnitOpenSupport.Env.class);
        if (env == null) {
            throw new IOException();
        }
        initializeComponent();
        activatedNodes(Collections.EMPTY_LIST);
    }

    private void initializeComponent() throws IOException {
        if (!EventQueue.isDispatchThread()) {
            throw new IOException();
        }
        support = (AbstractUnitOpenSupport) env.findCloneableOpenSupport();
        defaultSuffix = support.findDocumentsModel().getModelPrimarySuffix();
        env.addPropertyChangeListener(this);
        //init table
        table.setColumnFactory(new TargetsForStudentsColumnFactory(table));
        table.addHighlighter(cellHighlighter);
        CellIconHighlighter ih = new CellIconHighlighter();
        MimeLookup.getLookup("application/betula-unit-context").lookupAll(HighlighterInstanceFactory.class).forEach(hlf -> {
            final Highlighter hl = hlf.createHighlighter(table, this);
            if (hl instanceof CellIconHighlighterDelegate) {
                ih.addIconHighlighterDelegate((CellIconHighlighterDelegate) hl);
            } else {
                table.addHighlighter(hl);
            }
        });
        table.addHighlighter(ih);
        final MoveStudentsToTargetDropSupport dropSupport = support.findDropSupport();
        if (dropSupport != null) {
            final DropTarget dropTarget = new DropTarget(table, allowedDropActions, dropSupport, true);
            table.setDropTarget(dropTarget);
        }
        //load
        final String prio = savedTargetType != null ? savedTargetType : defaultSuffix;
        final TermId prioTerm = savedTermId != null ? savedTermId : support.findTermSchedule().getCurrentTerm().getScheduledItemId();
        final RemoteUnitsModel rm;
        if (support instanceof PrimaryUnitOpenSupport) {
            rm = ((PrimaryUnitOpenSupport) support).getRemoteUnitsModel(RemoteUnitsModel.INITIALISATION.MAXIMUM, prio, prioTerm);
        } else if (support instanceof MultiUnitOpenSupport) {
            rm = ((MultiUnitOpenSupport) support).getRemoteUnitsModel(RemoteUnitsModel.INITIALISATION.MAXIMUM, prio, prioTerm);
        } else {
            rm = support.getRemoteUnitsModel();
        }
        final boolean init = rm.getInitialization().satisfies(RemoteUnitsModel.INITIALISATION.PRIORITY);
        if (init) {
            doInit();
        }
        rm.addPropertyChangeListener(this);
        if (!init) {
            scrollPane.setViewportView(quietInit());
        }
    }

    //Should be called only once
    private void doInit() {
        final TargetsForStudentsModel m;
        Exception initEx = null;
        try {
            m = new TargetsForStudentsModel(support, support.getRemoteUnitsModel(), this);
            mListener = new MListener();
            EventQueue.invokeLater(() -> {
                try {
                    initTable(m);
                } catch (IOException ex) {
                    handleInitializationException(ex);
                }
            });
        } catch (IOException ex) {
            initEx = ex;
        } finally {
            if (initEx != null) {
                final Exception e = initEx;
                Mutex.EVENT.writeAccess(() -> handleInitializationException(e));
            }
        }
    }

    void initTable(TargetsForStudentsModel m) throws IOException {
//        final long l = System.currentTimeMillis();
        if (currentModel != null) {
            targetTypeBox.removeItemListener(currentModel);
            termBox.removeItemListener(currentModel);
            support.getRemoteUnitsModel().removePropertyChangeListener(mListener);
        }
        currentModel = m;
        cellHighlighter.setModel(currentModel);
        scrollPane.setViewportView(table);
        initTermAndTargetBoxes();
        table.setModel(currentModel);
        if (savedSelectedRow != -1 && savedSelectedRow < table.getRowCount()) {
            table.getSelectionModel().setSelectionInterval(0, savedSelectedRow);
        }
        savedSelectedRow = -1;
        support.getRemoteUnitsModel().addPropertyChangeListener(mListener);

        //TODO: remove undoRedo wenn, componend close
        //TODO: update when targets added /removed
        support.getRemoteUnitsModel().getTargets().stream()
                .forEach(rtad -> rtad.addUndoableEditListener(undoRedo));
        support.addUndoableEditListener(undoRedo);

        activatedNodes(Collections.EMPTY_LIST);

//        int r = Integer.getInteger("org.netbeans.core.TimeableEventQueue.report", -1);//20000 in production, 3000 develop/debug
//        Logger.getLogger("---TargetsForStudentsElementDebug---").log(Level.INFO, "Time in initTable: " + Long.toString(System.currentTimeMillis() - l) + " data: " + support.getNodeDelegate().getDisplayName());
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
                    updateComboBoxes();
                } else if (stage.satisfies(RemoteUnitsModel.INITIALISATION.PRIORITY)) {
                    doInit();
                }
                currentStage[0] = stage;
            }
        } else if (evt.getSource() instanceof AbstractUnitOpenSupport.Env && AbstractUnitOpenSupport.Env.PROP_VALID.equals(evt.getNewValue())) {
            boolean valid = env != null && env.isValid();
            if (!valid) {
                closeTC();
            }
        } else if (evt.getSource() instanceof RemoteUnitsModel && (prop.equals(RemoteUnitsModel.PROP_TERMS) || prop.equals(RemoteUnitsModel.PROP_TARGETS))) {
            updateComboBoxes();
        }
    }

    private void closeTC() {
        if (callback != null) {
            Mutex.EVENT.writeAccess(() -> {
                if (callback.getTopComponent().close()) {
                    if (currentModel != null) {
                        targetTypeBox.removeItemListener(currentModel);
                        termBox.removeItemListener(currentModel);
                        try {
                            support.getRemoteUnitsModel().removePropertyChangeListener(mListener);
                            support.getRemoteUnitsModel().removePropertyChangeListener(this);
                        } catch (IOException ex) {
                        }
                    }
                    currentModel = null;
                }
            });
        }
    }

    //TODO dauert oft viel zu lange!
    private void updateComboBoxes() {
        if (currentModel != null) {
            final Map<String, List<RemoteTargetAssessmentDocument>> m = currentModel.getRemoteUnitsModel().getTargets().stream()
                    .collect(Collectors.groupingBy(RemoteTargetAssessmentDocument::getTargetType, Collectors.toList()));
            Mutex.EVENT.writeAccess(() -> {
                final long start = System.currentTimeMillis();
                ((ColumnControlButtonExt) table.getColumnControl()).quiet(true);
                currentModel.updateTargets(m); //Too much time!
                ((ColumnControlButtonExt) table.getColumnControl()).quiet(false);
                targetTypeBox.removeItemListener(currentModel);
                termBox.removeItemListener(currentModel);
                final long l2 = System.currentTimeMillis() - start;
                initTermAndTargetBoxes();
                final long l3 = System.currentTimeMillis() - start;
                int r = Integer.getInteger("org.netbeans.core.TimeableEventQueue.report", -1);//20000 in production, 3000 develop/debug
                Logger.getLogger("---TargetsForStudentsElementDebug---").log(Level.INFO, "Time in updateComboBoxes: " + Long.toString(l2) + "/" + Long.toString(l3) + " data: " + support.getNodeDelegate().getDisplayName());
            });
        }
    }

    private void handleInitializationException(Exception initEx) {
        final String msg = NbBundle.getMessage(TargetsForStudentsElement.class, "TargetsForStudentsElement.inititalization.error", support.getNodeDelegate().getDisplayName());
        PlatformUtil.getCodeNameBaseLogger(TargetsForStudentsElement.class).log(Level.SEVERE, msg, initEx);
        if (callback != null) {
            callback.getTopComponent().close();
        } else {
            close();
        }
    }

    private void initTermAndTargetBoxes() {
        targetTypeBoxModel.removeAllElements();
        termBoxModel.removeAllElements();
        if (currentModel != null) {
            for (String type : currentModel.getTargetTypes()) {
                targetTypeBoxModel.addElement(type);
            }
            if (savedTargetType != null) {
                boolean res = currentModel.restoreCurrentTargetType(savedTargetType);
                if (res || currentModel.getRemoteUnitsModel().getInitialization().equals(RemoteUnitsModel.INITIALISATION.MAXIMUM)) {
                    savedTargetType = null;
                }
            } else {
                currentModel.restoreCurrentTargetType(defaultSuffix);
            }
            targetTypeBoxModel.setSelectedItem(currentModel.getCurrentTargetType());
            currentModel.getTerms().stream()
                    .forEach(termBoxModel::addElement);
            if (savedTermId != null) {
                boolean res = currentModel.restoreCurrentIdentity(savedTermId);
                if (res || currentModel.getRemoteUnitsModel().getInitialization().equals(RemoteUnitsModel.INITIALISATION.MAXIMUM)) {
                    savedTermId = null;
                }
            } else {
                try {
                    TermId t = support.findTermSchedule().getCurrentTerm().getScheduledItemId();
                    currentModel.restoreCurrentIdentity(t);
                } catch (IOException ex) {
                }
            }
            termBoxModel.setSelectedItem(currentModel.getCurrentIndentity());
        }
        termBox.addItemListener(currentModel);
        targetTypeBox.addItemListener(currentModel);
    }

    @Override
    protected Node getNodeForRow(int rowIndex) {
        int row = table.convertRowIndexToModel(rowIndex);
        if (currentModel != null) {
            final RemoteStudent rs = currentModel.getStudentAt(row);
            return studNodes.computeIfAbsent(rs.getStudentId(), (si) -> new AbstractNode(Children.LEAF, Lookups.singleton(rs)));
        }
        return null;
    }

    @Override
    protected final void activatedNodes(List<Node> sel) {
        if (env.isValid()) {
            final List<Node> selection = new ArrayList<>(sel);
            if (currentModel != null) {
                selection.add(currentModel.getNodeDelegate());
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
        if (currentModel != null && modelCol > 0) { //isPopupAllowed()
            RemoteTargetAssessmentDocument rtad = currentModel.getRemoteTargetAssessmentDocumentAtColumnIndex(modelCol);
            RemoteStudent rs = currentModel.getStudentAt(modelRow);
            Term term = currentModel.getCurrentIndentity();
            String type = currentModel != null ? currentModel.getCurrentTargetType() : null;
            TargetAssessmentSelectionProvider sp = new TargetAssessmentSelectionProvider(rtad, rs, support, term, type);
            if (rtad != null) {
                Action[] a = Utilities.actionsForPath("Loaders/application/betula-unit-context/Actions").stream()
                        .toArray(Action[]::new);
                return Utilities.actionsToPopup(a, sp.getLookup());
            }
        }
        return null;
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
        final int c = rc.getAndIncrement();
//        Logger.getLogger("---TargetsForStudentsElementDebug---").log(Level.INFO, "Reading: " + c);
        super.readExternal(oi);
        Object o = oi.readObject();
        if (o instanceof AbstractUnitOpenSupport.Env) {
            AbstractUnitOpenSupport.Env r = (AbstractUnitOpenSupport.Env) o;
            if (r.isValid()) {
                env = r;
            } else {
                throw new IOException();
            }
            o = oi.readObject();
            if (o != null && o instanceof TermId) {
                this.savedTermId = (TermId) o;
            }
            o = oi.readObject();
            if (o != null && o instanceof String) {
                savedTargetType = (String) o;
            }
//            final String log = "Read: " + ((AbstractUnitOpenSupport) env.findCloneableOpenSupport()).getNodeDelegate().getDisplayName();
//            PlatformUtil.getCodeNameBaseLogger(TargetsForStudentsElement.class).log(Level.INFO, log);
            initializeComponent();
            activatedNodes(Collections.EMPTY_LIST);
//            Logger.getLogger("---TargetsForStudentsElementDebug---").log(Level.INFO, "Read: " + c);
        } else {
            throw new IOException();
        }
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException {
        super.writeExternal(oo);
        if (env != null && env.isValid()) {
            int srows = table.getSelectedRow();
            if (srows == -1 && savedSelectedRow != -1) {
                srows = savedSelectedRow;
            }
            Term t = (Term) termBoxModel.getSelectedItem();
            TermId termid;
            if (t != null) {
                termid = t.getScheduledItemId();
            } else {
                termid = savedTermId;
            }
            String target = (String) targetTypeBoxModel.getSelectedItem();
            if (target == null) {
                target = savedTargetType;
            }
            if (termid == null || target == null) {
                throw new IOException();
            }
            oo.writeObject(env);
            oo.writeObject(termid);
            oo.writeObject(target);
            oo.writeInt(srows);
        }
    }

    static class TableExt extends JXTable {

        @Override
        protected JComponent createDefaultColumnControl() {
            return new ColumnControlButtonExt(this);
        }

    }

    class MListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (currentModel != null) {
                if (RemoteUnitsModel.PROP_STUDENTS.equals(evt.getPropertyName())) {
                    Mutex.EVENT.writeAccess(() -> {
                        ((ColumnControlButtonExt) table.getColumnControl()).quiet(true);
                        currentModel.initStudentsOnEvent();
                        ((ColumnControlButtonExt) table.getColumnControl()).quiet(false);
                    });
                } else if (RemoteUnitsModel.PROP_TARGETS.equals(evt.getPropertyName())) {
                    Mutex.EVENT.writeAccess(() -> {
                        ((ColumnControlButtonExt) table.getColumnControl()).quiet(true);
                        currentModel.initOnEvent();
                        ((ColumnControlButtonExt) table.getColumnControl()).quiet(false);
                    });
                }
            }
        }
    }

}
