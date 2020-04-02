/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.module;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.awt.event.MouseEvent;
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
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
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
import org.openide.windows.WindowManager;
import org.thespheres.betula.termreport.AssessmentProvider;
import org.thespheres.betula.termreport.TermReport;
import org.thespheres.betula.termreport.TermReportActions;
import org.thespheres.betula.ui.swingx.AbstractTableElement;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.ui.util.UIUtilities;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@MultiViewElement.Registration(
        displayName = "#TermReportElement.displayname",
        iconBase = "org/thespheres/betula/termreport/resources/betulatrep2_16.png",
        mimeType = "text/term-report-file+xml",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "TermReportElement",
        position = 1000)
@NbBundle.Messages({"TermReportElement.displayname=Tabelle"})
public final class TermReportElement extends AbstractTableElement implements Serializable {
    
    private final static Set<TermReportElement> TC_TRACKER = new HashSet<>(2);
    protected TermReportDataObject obj;
    protected TermReportSupport support;
    protected final JScrollPane scrollPane = new JScrollPane();
    private Lookup.Result<TermReportActions> ctEditorResult;
    private TermReportActions currentEditor;
    private final Listener listener = new Listener();

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public TermReportElement() {
        super();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        toolbar.setLayout(new FlowLayout(FlowLayout.RIGHT));
        Utilities.actionsForPath("Loaders/" + TermReportDataObject.TERMREPORT_MIME + "/Toolbar").stream()
                .forEach(toolbar::add);
        setLayout(new BorderLayout());
        setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public TermReportElement(Lookup lkp) {
        this();
        obj = lkp.lookup(TermReportDataObject.class);
        initializeComponent();
        activatedNodes(Collections.EMPTY_LIST);
    }
    
    protected void initializeComponent() {
        obj.addPropertyChangeListener(listener);
        support = obj.getLookup().lookup(TermReportSupport.class);
        ctEditorResult = obj.getLookup().lookupResult(TermReportActions.class);
        setCurrentEditor();
//        LookupListener weakl = WeakListeners.create(LookupListener.class, listener, ctEditorResult);
//        ctEditorResult.addLookupListener(weakl);
        ctEditorResult.addLookupListener(listener);
        table.setColumnFactory(support.getModel().createColumnFactory());
        Lookup hlkp = Lookups.forPath("Editors/" + TermReportDataObject.TERMREPORT_MIME + "/Highlighter");
        hlkp.lookupAll(HighlighterInstanceFactory.class).stream()
                .map(hlf -> hlf.createHighlighter(table, this))
                .filter(Objects::nonNull)
                .forEach(table::addHighlighter);
        initTable();
    }
    
    protected void initTable() {
        Mutex.EVENT.writeAccess(() -> {
            if (getCurrentActions() != null) {
                //TODO: setClasstest table
//            colFactory.initialize(editor);
                DropTarget dt = new DropTarget();
                try {
                    //TODO: siehe auch OutlineView
                    dt.addDropTargetListener(getCurrentActions());
                    table.setDropTarget(dt);
                    scrollPane.setDropTarget(dt);
                } catch (TooManyListenersException ex) {
                    Logger.getLogger(TermReportElement.class.getName()).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                }
                scrollPane.setViewportView(table);
                table.setModel(support.getModel());
            } else {
                scrollPane.setViewportView(initLoading());
            }
        });
    }
    
    protected TermReportActions getCurrentActions() {
        TermReportActions ret;
        synchronized (this) {
            ret = currentEditor;
        }
        return ret;
    }
    
    protected final void setCurrentEditor() {
        synchronized (this) {
            currentEditor = ctEditorResult.allInstances().stream()
                    .map(TermReportActions.class::cast)
                    .collect(CollectionUtil.singleOrNull());
        }
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
                    WindowManager.getDefault().findTopComponentGroup("TermReportGroup").open();
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
                    WindowManager.getDefault().findTopComponentGroup("TermReportGroup").close();
                }
            }
        });
    }
    
    @Override
    protected void updateName() {
        if (callback != null) {
            Mutex.EVENT.writeAccess(() -> {
                TopComponent tc = callback.getTopComponent();
                if (obj != null && obj.isValid()) {
                    Node n = obj.getNodeDelegate();
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
    protected void activatedNodes(List<Node> sel) {
        if (obj.isValid()) {
            List<Node> selection = new ArrayList<>(sel);
            selection.add(obj.getNodeDelegate());
            setActivatedNodes(selection.toArray(new Node[selection.size()]));
            setIcon(obj.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
            updateName();
        }
    }
    
    @Override
    public void componentClosed() {
//        if (ctEditorResult != null) {
//            ctEditorResult.removeLookupListener(listener);
//        }
        if (obj != null) {
            obj.removePropertyChangeListener(listener);
        }
    }
    
    @Override
    protected JPopupMenu createPopup(int modelCol, int modelRow, Point p, MouseEvent e) {
        if (getCurrentActions() != null) {
            TermReport tr = getCurrentActions().getTermReport();
            if (modelCol > 0 && modelCol <= tr.getProviders().size()) {
                AssessmentProvider provider = tr.getProviders().get(modelCol - 1);
//                StatusDisplayer.getDefault().setStatusText(provider.getDisplayName());
                Action[] actions = provider.getNodeDelegate().getActions();
//        Action[] actions = NodeOp.findActions(selectedNodes);
                return Utilities.actionsToPopup(actions, table);
            }
        }
        return null;
    }
    
    @Override
    protected Node getNodeForRow(int rowIndex) {
        if (support != null) {
            int row = table.convertRowIndexToModel(rowIndex);
//            EditableRecord er = support.getModel().getEditableCalendar().getEditableRecords().get(row);
//            return er.getNode();
        }
        return null;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        if (obj != null && obj.isValid()) {
            out.writeObject(obj);
            out.writeInt(table.getSelectedRow());
        }
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object oi = in.readObject();
        if (oi instanceof TermReportDataObject) {
            obj = (TermReportDataObject) oi;
            int sel = in.readInt();
            if (sel >= 0) {
                table.getSelectionModel().setSelectionInterval(sel, sel);
            }
        }
        initializeComponent();
        activatedNodes(Collections.EMPTY_LIST);
    }
    
    private final class Listener extends NodeAdapter implements LookupListener {
        
        @Override
        public void resultChanged(LookupEvent ev) {
            setCurrentEditor();
            initTable();
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateName();
        }
        
    }
}
