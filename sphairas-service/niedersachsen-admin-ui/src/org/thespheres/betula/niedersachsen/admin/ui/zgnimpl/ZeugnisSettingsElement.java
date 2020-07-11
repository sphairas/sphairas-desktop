/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.UndoableEditListener;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.niedersachsen.admin.ui.Constants;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermNotFoundException;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.ui.swingx.AbstractTableElement;
import org.thespheres.betula.ui.swingx.CellIconHighlighter;
import org.thespheres.betula.ui.swingx.CellIconHighlighterDelegate;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@MultiViewElement.Registration(mimeType = "application/betula-unit-data", persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED, displayName = "Zeugnisse", preferredID = "ZeugnisSettingsElementME", position = 3000)
public class ZeugnisSettingsElement extends AbstractTableElement implements MultiViewElement, Serializable {

    private final static Set<ZeugnisSettingsElement> TC_TRACKER = new HashSet<>(2);
    private PrimaryUnitOpenSupport.Env env;
    private JScrollPane scrollPane;
    private ZeugnisAngabenModel model;
    private final JXComboBox termBox;
    private final DefaultComboBoxModel termBoxModel = new DefaultComboBoxModel();
    private final StringValue termStringValue = v -> v instanceof Term ? ((Term) v).getDisplayName() : null;
    private TermId savedTermId;
    private PrimaryUnitOpenSupport support;
    private List<Term> currentTerms;
    private TermSchedule schedule;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public ZeugnisSettingsElement() {
        termBox = new JXComboBox();
        termBox.setModel(termBoxModel);
        termBox.setEditable(false);
        termBox.setRenderer(new DefaultListRenderer(termStringValue));
        scrollPane = new javax.swing.JScrollPane();
        toolbar.add(Box.createHorizontalGlue()); // After this every component will be added to the right 
        toolbar.add(termBox);
        setLayout(new BorderLayout());
        setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        scrollPane.setViewportView(quietInit());
    }

    public ZeugnisSettingsElement(Lookup context) throws IOException {
        this();
        env = context.lookup(PrimaryUnitOpenSupport.Env.class);
        if (env == null) {
            throw new IOException();
        }
        initializeComponent();
    }

    private void initializeComponent() {
        support = env.findCloneableOpenSupport();
        final RemoteUnitsModel remoteModel;
        try {
            remoteModel = support.getRemoteUnitsModel(RemoteUnitsModel.INITIALISATION.STUDENTS);
            schedule = support.findTermSchedule();
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(ZeugnisSettingsElement.class).log(Level.SEVERE, ex.getMessage(), ex);
            callback.getTopComponent().close();
            return;
        }
        class PCL implements PropertyChangeListener {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(RemoteUnitsModel.PROP_INITIALISATION)
                        || evt.getPropertyName().equals(RemoteUnitsModel.PROP_TARGETS)) {
                    Mutex.EVENT.writeAccess(() -> doInit(remoteModel));
                }
            }
        }
        remoteModel.addPropertyChangeListener(new PCL());
        doInit(remoteModel);
    }

    private JComponent quietInit() {
        JLabel loadingLbl = new JLabel(NbBundle.getBundle("org.thespheres.betula.admin.units.ui.Bundle").getString("TargetsforStudentsElement.quietInit.label")); // NOI18N
        loadingLbl.setOpaque(true);
        loadingLbl.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLbl.setBorder(new EmptyBorder(new Insets(11, 11, 11, 11)));
        return loadingLbl;
    }

    private void doInit(final RemoteUnitsModel remoteModel) {
        if (!EventQueue.isDispatchThread()) {
            throw new RuntimeException(ZeugnisSettingsElement.class.getName() + " must be initialized in EventQueue.");
        }
        if (model != null) {
            updateTerms(remoteModel);
            return;
        }
        model = ZeugnisAngabenModel.create(support);
        updateTerms(remoteModel);
        table.setColumnFactory(model.createColumnFactory());
        table.setModel(model);
        final CellIconHighlighter ih = new CellIconHighlighter();
        MimeLookup.getLookup(Constants.UNIT_NDS_ZEUGNIS_SETTINGS_MIME).lookupAll(HighlighterInstanceFactory.class).forEach(hlf -> {
            final Highlighter hl = hlf.createHighlighter(table, this);
            if (hl instanceof CellIconHighlighterDelegate) {
                ih.addIconHighlighterDelegate((CellIconHighlighterDelegate) hl);
            } else {
                table.addHighlighter(hl);
            }
        });
        table.addHighlighter(ih);
        scrollPane.setViewportView(table);
        model.getItemsModel().getUndoSupport().addUndoableEditListener((UndoableEditListener) getUndoRedo());
        activatedNodes(Collections.EMPTY_LIST);
    }

    //EventQueue
    private void updateTerms(final RemoteUnitsModel remoteModel) {
        final List<Term> terms = remoteModel.getTerms().stream()
                .map(tid -> {
                    try {
                        return schedule.resolve(tid);
                    } catch (TermNotFoundException | IllegalAuthorityException ex) {
                        PlatformUtil.getCodeNameBaseLogger(ZeugnisSettingsElement.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Term::getBegin))
                .collect(Collectors.toList());
        if (!Objects.equals(terms, currentTerms)) {
            currentTerms = terms;
            termBox.removeActionListener(model);
            termBoxModel.removeAllElements();
            currentTerms.stream()
                    .forEach(termBoxModel::addElement);
            termBox.addActionListener(model);
        }
        if (currentTerms.isEmpty()) {
            return;
        }
        Term restore = null;
        if (savedTermId != null) {
            restore = currentTerms.stream()
                    .filter(t -> t.getScheduledItemId().equals(savedTermId))
                    .collect(CollectionUtil.singleOrNull());
            if (restore != null && remoteModel.getInitialization().satisfies(RemoteUnitsModel.INITIALISATION.MAXIMUM)) {
                savedTermId = null;
            }
        }
        if (restore == null) {
            final TermId c = schedule.getCurrentTerm().getScheduledItemId();
            restore = currentTerms.stream()
                    .filter(t -> t.getScheduledItemId().equals(c))
                    .collect(CollectionUtil.singleOrNull());
        }
        if (restore == null && !currentTerms.isEmpty()) {
            restore = currentTerms.get(currentTerms.size() - 1);
        }
        if (restore != null && !Objects.equals(model.getCurrentTermId(), restore.getScheduledItemId())) {
            model.setCurrentTerm(restore);
            termBoxModel.setSelectedItem(model.getCurrentTerm());
        }
    }

    @Override
    protected void activatedNodes(List<Node> sel) {
        if (env.isValid()) {
            final List<Node> selection = new ArrayList<>(sel);
            if (model != null) {
                selection.add(model.getNodeDelegate());
            }
            final Node ourNode = env.findCloneableOpenSupport().getNodeDelegate();
            selection.add(ourNode);
            setActivatedNodes(selection.toArray(new Node[selection.size()]));
            setIcon(ourNode.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16));
        }
    }

    @Override
    protected Node getNodeForRow(int rowIndex) {
        final int row = table.convertRowIndexToModel(rowIndex);
        if (model != null) {
            return model.getItemAt(row).getNodeDelegate();
        }
        return null;
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolbar;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
//        updateName();
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        EventQueue.invokeLater(() -> {
            synchronized (TC_TRACKER) {
                final boolean open = TC_TRACKER.isEmpty();
                TC_TRACKER.add(this);
                if (open) {
                    ZeugnisBemerkungenEnv.getInstance().open();
                    WindowManager.getDefault().findTopComponentGroup("ZeugnisGroup").open();
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
                    ZeugnisBemerkungenEnv.getInstance().close();
                    WindowManager.getDefault().findTopComponentGroup("ZeugnisGroup").close();
                }
            }
        });
    }

    @Override
    protected JPopupMenu createPopup(int column, int row, Point p, MouseEvent e) {
        if (model != null) {
            final ReportData2 rd = model.getItemAt(row);
//isPopupAllowed()
            final RemoteStudent rs = rd.getRemoteStudent();
            final List<Action> al = (List<Action>) Utilities.actionsForPath("Loaders/application/betula-unit-context/Actions");
            final Action[] a = al.toArray(new Action[al.size()]);
            return Utilities.actionsToPopup(a, this);
        }
        return null;
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
        super.readExternal(oi);
        Object o = oi.readObject();
        if (o instanceof PrimaryUnitOpenSupport.Env) {
            final PrimaryUnitOpenSupport.Env r = (PrimaryUnitOpenSupport.Env) o;
            if (r.isValid()) {
                env = r;
            } else {
                throw new IOException();
            }
            try {
                o = oi.readObject();
                if (o instanceof TermId) {
                    savedTermId = (TermId) o;
                }
            } catch (OptionalDataException odex) {
            }
        }
        activatedNodes(Collections.EMPTY_LIST);
        initializeComponent();
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException {
        super.writeExternal(oo);
        if (env != null && env.isValid()) {
            oo.writeObject(env);
            final Term t = (Term) termBoxModel.getSelectedItem();
            if (t != null) {
                oo.writeObject(t.getScheduledItemId());
            }
        }
    }
}
