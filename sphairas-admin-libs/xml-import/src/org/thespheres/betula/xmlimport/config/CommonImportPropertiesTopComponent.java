/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.config;

import org.thespheres.betula.adminconfig.DefaultAppResourcesProperties;
import org.thespheres.betula.adminconfig.AppResourcesProperties;
import org.thespheres.betula.adminconfig.AppResourcesProperty;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.thespheres.betula.adminconfig.ProviderReference;
import org.thespheres.betula.ui.swingx.AbstractTableElement;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.ui.util.UIUtilities;

/**
 *
 * @author boris.heithecker
 */
@MultiViewElement.Registration(
        displayName = "#CommonImportPropertiesTopComponent.displayName",
        //        iconBase = "org/thespheres/betula/termreport/resources/betulatrep2_16.png",
        mimeType = ProviderReference.MIME,
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "CommonImportPropertiesTopComponent",
        position = 5000)
@NbBundle.Messages({"CommonImportPropertiesTopComponent.displayName=Basis-Import",
    "CommonImportPropertiesTopComponent.displayName.withProvider=Basis-Import ({0})"})
public class CommonImportPropertiesTopComponent extends AbstractTableElement implements MultiViewElement, Serializable {

    public static final String MIME = "application/app-resources-common-import-properties";
    public static final String RESOURCE = "common-import.properties";
    private ProviderReference env;
    private final Listener listener = new Listener();
    private final CommonImportPropertiesTableModel model = CommonImportPropertiesTableModel.create(MIME);
    private final javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane();

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public CommonImportPropertiesTopComponent() {
        super();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        toolbar.setLayout(new FlowLayout(FlowLayout.LEADING));
        table.setColumnFactory(model.createColumnFactory());
        model.addDefaultToolbarActions(toolbar);
        Utilities.actionsForPath("Loaders/" + MIME + "/Toolbar").stream()
                .forEach(toolbar::add);
        setLayout(new BorderLayout());
        setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public CommonImportPropertiesTopComponent(final Lookup lkp) {
        this();
        this.env = lkp.lookup(ProviderReference.class);
        initializeComponent();
        activatedNodes(Collections.EMPTY_LIST);
    }

    protected void initializeComponent() {
        Optional.ofNullable(model.getItemsModel())
                .ifPresent(m -> m.removeChangeListener(listener));
        final DefaultAppResourcesProperties props = new DefaultAppResourcesProperties(this.env.getProviderInfo().getURL(), RESOURCE);
        model.initialize(props, Lookup.EMPTY);
        model.getItemsModel().addChangeListener(listener);

        final Lookup hlkp = Lookups.forPath("Editors/" + MIME + "/Highlighter");
        hlkp.lookupAll(HighlighterInstanceFactory.class).stream()
                .map(hlf -> hlf.createHighlighter(table, this)).
                filter(Objects::nonNull)
                .forEach(table::addHighlighter);
//        final ActionMap am = table.getActionMap();
//        am.put("delete", new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                final List<AppResourcesProperty> toDelete = Arrays.stream(table.getSelectedRows())
//                        .map(table::convertRowIndexToModel)
//                        .mapToObj(model::getItemAt)
//                        .filter(Objects::nonNull)
//                        .collect(Collectors.toList());
//                Mutex.EVENT.writeAccess(() -> {
////                    final StudentsDBRemoveStudentAction ac = new StudentsDBRemoveStudentAction(studs, model.getItemsModel());
////                    ac.actionPerformed(null);
//                });
//            }
//        });
        initTable();
        updateName();
    }

    protected void initTable() {
        Mutex.EVENT.writeAccess(() -> {
            scrollPane.setViewportView(table);
            table.setModel(model);
        });
    }

    @Override
    protected Node getNodeForRow(final int rowIndex) {
        final AppResourcesProperty item = model.getItemAt(rowIndex);
        return new PropertyNode(item, model.getItemsModel());
    }

    @Override
    protected void activatedNodes(final List<Node> selected) {
        Mutex.EVENT.writeAccess(() -> setActivatedNodes(selected.toArray(new Node[selected.size()])));
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
                final String name = NbBundle.getMessage(CommonImportPropertiesTopComponent.class, "CommonImportPropertiesTopComponent.displayName.withProvider", env.getProviderInfo().getDisplayName());
                setDisplayName(name);
                if (callback != null) {
                    final TopComponent tc = callback.getTopComponent();
                    final AppResourcesProperties m = model.getItemsModel();
                    final boolean modif = m != null && m.isModified();
                    final boolean readOnly = m == null || m.isReadOnly();
                    tc.setDisplayName(UIUtilities.annotateName(name, false, modif, readOnly));
                    tc.setHtmlDisplayName(UIUtilities.annotateName(name, true, modif, readOnly));
                    //TODO respect markedDirty() => Badge Icon
                }
            });
        }
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        updateName();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        if (env != null) {
            out.writeObject(env);
        } else {
            throw new IOException("No AppResourcesConfigOpenSupport.Env to serialize component.");
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        final Object oi = in.readObject();
        if (oi instanceof ProviderReference) {
            env = (ProviderReference) oi;
        } else {
            throw new IOException("No AppResourcesConfigOpenSupport.Env in serialized component.");
        }
        initializeComponent();
        activatedNodes(Collections.EMPTY_LIST);
    }

    class Listener implements ChangeListener {

        @Override
        public void stateChanged(final ChangeEvent e) {
            updateName();
        }

    }

    class PropertyNode extends AbstractNode {

        PropertyNode(final AppResourcesProperty student, final AppResourcesProperties coll) {
            super(Children.LEAF, Lookups.fixed(student, coll));
        }

    }
}
