/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui;

import org.thespheres.betula.admin.units.TargetsSelectionElementEnv2;
import org.thespheres.betula.ui.swingx.AbstractTableElement;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultEditorKit;
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
import org.openide.nodes.NodeListener;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.admin.units.TargetAssessmentSelectionProvider;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.ui.swingx.CellIconHighlighter;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.ui.swingx.CellIconHighlighterDelegate;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages("TargetsSelectionElement.initialization.error=Could not open TargetsSelectionElement {0}")
@MultiViewElement.Registration(mimeType = "application/betula-targets-data", persistenceType = TopComponent.PERSISTENCE_ALWAYS, displayName = "Zensuren", preferredID = "TargetsME", position = 1000)
public class TargetsSelectionElement extends AbstractTableElement implements Serializable, ChangeListener {

    public static final String MIME = "application/betula-targets-data";
    private final static Set<TargetsSelectionElement> TC_TRACKER = new HashSet<>(2);
    private final javax.swing.JScrollPane scrollPane;
    private final JXComboBox termBox;
    private final ColumnSearchComponent columnSearchField;
    private TargetsSelectionModel model;
    private final DefaultComboBoxModel termBoxModel = new DefaultComboBoxModel();
    private TermId savedTermId;
    private final StringValue termStringValue = (value) -> {
        if (value instanceof Term) {
            Term t = (Term) value;
            return t.getDisplayName();
        }
        return null;
    };
    private final int allowedDropActions = DnDConstants.ACTION_COPY; // | DnDConstants.ACTION_REFERENCE;
//    private MoveStudentsToTargetDropSupport dropSupport;
    private final StudentValuesToolTipHighlighter cellHighlighter;
    private final HashMap<StudentId, AbstractNode> studNodes = new HashMap<>();
    private int savedSelectedRow;
    private NodeListener nl;
    private final InstanceContent ic = new InstanceContent();
    private final TargetsSelectionElementNode node = new TargetsSelectionElementNode();

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public TargetsSelectionElement() {
        super(new TableExt());
        cellHighlighter = new StudentValuesToolTipHighlighter(HighlightPredicate.ALWAYS);
        scrollPane = new javax.swing.JScrollPane();
        termBox = new JXComboBox();
        termBox.setModel(termBoxModel);
        termBox.setEditable(false);
        termBox.setRenderer(new DefaultListRenderer(termStringValue));
        columnSearchField = new ColumnSearchComponent(table);
//        toolbar.add(Box.createHorizontalGlue()); // After this every component will be added to the right 
        //If no Layout set ==> problem in ubuntu with ui.., jtextfield
        toolbar.setLayout(new FlowLayout(FlowLayout.RIGHT));
        toolbar.add(columnSearchField);
        toolbar.add(termBox);
        setLayout(new BorderLayout());
        setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        table.getActionMap().put(DefaultEditorKit.pasteAction, Actions.forID("System", "org.openide.actions.PasteAction"));
        scrollPane.setViewportView(quietInit());
        activatedNodes(Collections.EMPTY_LIST);
    }

    private JComponent quietInit() {
        final JLabel loadingLbl = new JLabel(NbBundle.getMessage(TargetsForStudentsElement.class, "TargetsforStudentsElement.quietInit.label")); // NOI18N
        loadingLbl.setOpaque(true);
        loadingLbl.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLbl.setBorder(new EmptyBorder(new Insets(11, 11, 11, 11)));
        return loadingLbl;
    }

    public TargetsSelectionElement(final Lookup context) throws IOException {
        this();
        final TargetsSelectionElementEnv2 e = context.lookup(TargetsSelectionElementEnv2.class);
        if (e == null) {
            throw new IOException();
        }
        ic.add(e);
        ic.add(e.findCloneableOpenSupport());
        initializeComponent();
    }

    private TargetsSelectionElementEnv2 getEnv() {
        return getNode().getLookup().lookup(TargetsSelectionElementEnv2.class);
    }

    private void initializeComponent() throws IOException {
        if (!EventQueue.isDispatchThread()) {
            throw new IOException();
        }
//        env.addPropertyChangeListener(this);
        //init table
        table.setColumnFactory(new TargetsForStudentsColumnFactory(table));
        table.addHighlighter(cellHighlighter);
        CellIconHighlighter ih = new CellIconHighlighter();
        MimeLookup.getLookup(MIME).lookupAll(HighlighterInstanceFactory.class).forEach(hlf -> {
            final Highlighter hl = hlf.createHighlighter(table, this);
            if (hl instanceof CellIconHighlighterDelegate) {
                ih.addIconHighlighterDelegate((CellIconHighlighterDelegate) hl);
            } else {
                table.addHighlighter(hl);
            }
        });
        table.addHighlighter(ih);
//        final MoveStudentsToTargetDropSupport dropSupport = support.findDropSupport();
//        if (dropSupport != null) {
//            final DropTarget dropTarget = new DropTarget(table, allowedDropActions, dropSupport, true);
//            table.setDropTarget(dropTarget);
//        }
        load();
        getEnv().addChangeListener(this);
    }

    private void initModel(final List<RemoteTargetAssessmentDocument> ll) {
        if (model == null) {
            model = new TargetsSelectionModel(getEnv(), this);
        }
        final TargetsForStudentsColumnFactory cf = (TargetsForStudentsColumnFactory) table.getColumnFactory();
        cf.addPrimaryUnitColumn = true;
        cf.term = () -> model.getCurrentIndentity();
        table.setModel(model);
        cellHighlighter.setModel(model);
        scrollPane.setViewportView(table);
        model.init(ll);

        if (savedSelectedRow != -1 && savedSelectedRow < table.getRowCount()) {
            table.getSelectionModel().setSelectionInterval(0, savedSelectedRow);
        }
        savedSelectedRow = -1;

        EventQueue.invokeLater(this::initTermBox);

        Mutex.EVENT.writeAccess(() -> {
            model.getLastTargets().forEach(ic::remove);
            model.getTargets().forEach(ic::add);
            activatedNodes(Collections.EMPTY_LIST);
        });
    }

    void load() {
        final DocumentId[] l = getEnv().getTargets().stream().toArray(DocumentId[]::new);

        final RemoteUnitsModel.Factory fac = Lookup.getDefault().lookupAll(RemoteUnitsModel.Factory.class).stream()
                .filter(f -> f.id().equals(AbstractUnitOpenSupport.FACTORY))
                .collect(CollectionUtil.requireSingleOrNull());
        if (fac == null) {
            throw new IllegalStateException("Factory " + AbstractUnitOpenSupport.FACTORY + " not found.");
        }
        final Map<DocumentId, RemoteTargetAssessmentDocument> loadMap = new HashMap<>();
        for (final DocumentId d : l) {
            class Fetch implements Runnable {

                @Override
                public void run() {
                    try {
                        final RemoteTargetAssessmentDocument rtad = fac.find(getEnv().getProvider(), d);
                        synchronized (loadMap) {
                            loadMap.put(d, rtad);
                            if (loadMap.size() == l.length) {
                                final List<RemoteTargetAssessmentDocument> ll = Arrays.stream(l)
                                        .map(loadMap::get)
                                        .collect(Collectors.toList());
                                initModel(ll);
                            }
                        }
                    } catch (IOException ex) {
                        handleInitializationException(ex);
                    }
                }

            }
            Util.RP(getEnv().getProvider()).post(new Fetch());
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        load();
    }

    @Override
    public void componentClosed() {
//        if (support != null) {
//            try {
//                support.getRemoteUnitsModel().getTargets().stream()
//                        .forEach(rtad -> rtad.removeUndoableEditListener(undoRedo));
//                env.removePropertyChangeListener(this);
//            } catch (IOException ex) {
//            }
//        }
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

    private void closeTC() {
        if (callback != null) {
            Mutex.EVENT.writeAccess(() -> {
                if (callback.getTopComponent().close()) {
//                        targetTypeBox.removeItemListener(currentModel);
                    termBox.removeItemListener(model);
//                        try {
//                            support.getRemoteUnitsModel().removePropertyChangeListener(mListener);
//                            support.getRemoteUnitsModel().removePropertyChangeListener(this);
//                        } catch (IOException ex) {
//                        }
                }
            });
        }
    }

    private void handleInitializationException(Exception initEx) {
        final String msg = NbBundle.getMessage(TargetsSelectionElement.class, "TargetsSelectionElement.initialization.error", getNode().getDisplayName());
        PlatformUtil.getCodeNameBaseLogger(TargetsForStudentsElement.class).log(Level.SEVERE, msg, initEx);
        closeTC();
    }

    private void initTermBox() {
        termBox.removeItemListener(model);
        termBoxModel.removeAllElements();
        model.getTerms().stream()
                .forEach(termBoxModel::addElement);
        if (savedTermId != null) {
            if (model.restoreCurrentIdentity(savedTermId)) {
                savedTermId = null;
            }
        } else {
            model.restoreCurrentIdentity(null);
//                try {
//                    TermId t = support.findTermSchedule().getCurrentTerm().getScheduledItemId();
//                    currentModel.restoreCurrentIdentity(t);
//                } catch (IOException ex) {
//                }
        }
        termBoxModel.setSelectedItem(model.getCurrentIndentity());
        termBox.addItemListener(model);
    }

    Node getNode() {
        return node;
    }

    @Override
    protected Node getNodeForRow(int rowIndex) {
        int row = table.convertRowIndexToModel(rowIndex);
        if (model != null) {
            final RemoteStudent rs = model.getStudentAt(row);
            return studNodes.computeIfAbsent(rs.getStudentId(), (si) -> new AbstractNode(Children.LEAF, Lookups.singleton(rs)));
        }
        return null;
    }

    @Override
    protected final void activatedNodes(List<Node> selection) {
        final Node n = getNode();
        final Node[] arr = Stream.concat(Stream.of(n), selection.stream()).toArray(Node[]::new);
        setActivatedNodes(arr);
        setIcon(n.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16));
        updateName();
    }

    @Override
    protected void updateName() {
        if (callback != null) {
            Mutex.EVENT.writeAccess(() -> {
                TopComponent tc = callback.getTopComponent();
                final Node n = getNode();
                tc.setDisplayName(n.getDisplayName());
                tc.setHtmlDisplayName(n.getHtmlDisplayName());
                tc.setIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
            });
        }
    }

    @Override
    protected JPopupMenu createPopup(int modelCol, int modelRow, Point p, MouseEvent e) {
        if (model != null && modelCol > 0) { //isPopupAllowed()
            final RemoteTargetAssessmentDocument rtad = model.getRemoteTargetAssessmentDocumentAtColumnIndex(modelCol);
            final RemoteStudent rs = model.getStudentAt(modelRow);
            final Term term = model.getCurrentIndentity();
//            String type = currentModel != null ? currentModel.getCurrentTargetType() : null;
            final TargetAssessmentSelectionProvider sp = new TargetAssessmentSelectionProvider(rtad, rs, null, term, null);
            if (rtad != null) {
                final Action[] a = Utilities.actionsForPath("Loaders/" + MIME + "/Actions").stream()
                        .toArray(Action[]::new);
                return Utilities.actionsToPopup(a, sp.getLookup());
            }
        }
        return null;
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
        super.readExternal(oi);
        Object o = oi.readObject();
        if (o instanceof TargetsSelectionElementEnv2) {
            final TargetsSelectionElementEnv2 e = (TargetsSelectionElementEnv2) o;
            if (e == null) {
                throw new IOException();
            }
            ic.add(e);
            ic.add(e.findCloneableOpenSupport());
            o = oi.readObject();
            if (o != null && o instanceof TermId) {
                savedTermId = (TermId) o;
            }
            initializeComponent();
        } else {
            throw new IOException();
        }
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException {
        super.writeExternal(oo);
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
        if (termid == null) {
            throw new IOException();
        }
        oo.writeObject(getEnv());
        oo.writeObject(termid);
        oo.writeInt(srows);
    }

    class TargetsSelectionElementNode extends AbstractNode {

        public TargetsSelectionElementNode() {
            super(Children.LEAF, new AbstractLookup(ic));
//            super.setName(ur.getUnitId().toString());
            setIconBaseWithExtension("org/thespheres/betula/admin/units/resources/table.png");
        }

        @Override
        public String getDisplayName() {
            if (model != null) {
                return model.getDisplayName();
            }
            return getEnv().getTargets().stream()
                    .map(DocumentId::getId)
                    .collect(Collectors.joining(","));
        }

        @Override
        public Action[] getActions(boolean context) {
            return Utilities.actionsForPath("Loaders/" + MIME + "/Actions").stream()
                    .toArray(Action[]::new);
        }
    }

    static class TableExt extends JXTable {

        @Override
        protected JComponent createDefaultColumnControl() {
            return new ColumnControlButtonExt(this);
        }

    }

}
