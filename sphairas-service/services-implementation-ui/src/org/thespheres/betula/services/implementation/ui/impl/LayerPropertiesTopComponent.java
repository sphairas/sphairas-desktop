/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import java.awt.FlowLayout;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.thespheres.betula.adminconfig.ProviderReference;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.ui.swingx.treetable.NbSwingXTreeTableElement;
import org.thespheres.betula.ui.util.UIUtilities;

/**
 *
 * @author boris.heithecker
 */
@MultiViewElement.Registration(
        displayName = "#LayerPropertiesTopComponent.displayName",
        //        iconBase = "org/thespheres/betula/termreport/resources/betulatrep2_16.png",
        mimeType = ProviderReference.MIME,
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "LayerPropertiesTopComponent",
        position = 1200)
@NbBundle.Messages({"LayerPropertiesTopComponent.displayName=Verzeichnis",
    "LayerPropertiesTopComponent.displayName.withProvider=Verzeichnis ({0})"})
public class LayerPropertiesTopComponent extends NbSwingXTreeTableElement implements MultiViewElement, Serializable {

    public static final String MIME = "application/app-resources-layer";
    public static final String RESOURCE = "default.properties";
    private ProviderReference env;
    private final Listener listener = new Listener();
    private final LayerPropertiesModel model = LayerPropertiesModel.create();

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public LayerPropertiesTopComponent() {
        super();
        treeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        treeTable.setRootVisible(true);
        toolbar.setLayout(new FlowLayout(FlowLayout.LEADING));
        model.addChangeListener(listener);
        model.addDefaultToolbarActions(toolbar);
        Utilities.actionsForPath("Loaders/" + MIME + "/Toolbar").stream()
                .forEach(toolbar::add);
        setModel(model);
    }

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public LayerPropertiesTopComponent(final Lookup lkp) {
        this();
        this.env = lkp.lookup(ProviderReference.class);
        initializeComponent();
        activatedNodes(Collections.EMPTY_LIST);
    }

    @Override
    protected Node getNodeDelegate() {
        return model.getRootNode();
    }

    @Override
    protected void initializeComponent() {
        model.initialize(env);
        final Lookup hlkp = Lookups.forPath("Editors/" + MIME + "/Highlighter");
        hlkp.lookupAll(HighlighterInstanceFactory.class).stream()
                .map(hlf -> hlf.createHighlighter(treeTable, this)).
                filter(Objects::nonNull)
                .forEach(treeTable::addHighlighter);
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
    }

    @Override
    protected void activatedNodes(final List<Node> selected) {
        Mutex.EVENT.writeAccess(() -> setActivatedNodes(selected.toArray(new Node[selected.size()])));
    }

    @Override
    protected void updateName(final Node n) {
        if (env != null) {
            Mutex.EVENT.writeAccess(() -> {
                final String name = NbBundle.getMessage(LayerPropertiesTopComponent.class, "LayerPropertiesTopComponent.displayName.withProvider", env.getProviderInfo().getDisplayName());
                setDisplayName(name);
                if (callback != null) {
                    final TopComponent tc = callback.getTopComponent();
                    final boolean modif = model.isModified();
                    final boolean readOnly = model.isReadOnly();
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
        updateName(null);
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
            updateName(null);
        }

    }

}
