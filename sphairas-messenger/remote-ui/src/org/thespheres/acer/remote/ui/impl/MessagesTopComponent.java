/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.impl;

import com.google.common.eventbus.Subscribe;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.ActionMap;
import javax.swing.event.ChangeEvent;
import javax.swing.text.DefaultEditorKit;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.thespheres.acer.remote.ui.MessagesTopComponentModel;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "MessagesTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "leftSlidingSide", openAtStartup = true)
@ActionID(category = "Window", id = "org.thespheres.acer.remote.ui.impl.MessagesTopComponent")
@ActionReference(path = "Menu/Window/betula-beans-services-windows" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#MessagesTopComponent.openAction.displayName",
        preferredID = "MessagesTopComponent")
@NbBundle.Messages({"MessagesTopComponent.openAction.displayName=Mitteilungen",
    "MessagesTopComponent.name=Mitteilungen"})
public class MessagesTopComponent extends CloneableTopComponent implements ExplorerManager.Provider, LookupListener {

    final ExplorerManager manager;
    private final Node root;
    private final ModelChildren children;
    private final Lookup.Result<MessagesTopComponentModel.Provider> result;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public MessagesTopComponent() {
        manager = new ExplorerManager();
        ActionMap map = getActionMap();
        map.put("delete", ExplorerUtils.actionDelete(manager, false));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        associateLookup(ExplorerUtils.createLookup(manager, map));
        initComponents();
        setName(NbBundle.getMessage(MessagesTopComponent.class, "MessagesTopComponent.name"));
        children = new ModelChildren();
        root = new AbstractNode(Children.create(children, true));
        manager.setRootContext(root);
        result = Lookup.getDefault().lookupResult(MessagesTopComponentModel.Provider.class);
        result.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        children.update();
    }

    @Override
    protected void componentActivated() {
        children.update();
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
        super.readExternal(oi);
//        final String p = oi.readUTF();
//        if (model != null && p.equals(model.getProviderInfo().getURL())) {
        try {
            final List<String[]> paths = (List<String[]>) oi.readObject();
            ((MessagesTreeView) messagesTreeView).expandNodes(paths);
        } catch (ClassCastException clex) {
        }
//        }
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException {
        super.writeExternal(oo);
//        oo.writeUTF(model.getProviderInfo().getURL());
        final List<String[]> paths = ((MessagesTreeView) messagesTreeView).getExpandedPaths();
        oo.writeObject(paths);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        messagesTreeView = new MessagesTreeView(manager);
        toolbar = new javax.swing.JToolBar();

        setLayout(new java.awt.BorderLayout());

        messagesTreeView.setRootVisible(false);
        add(messagesTreeView, java.awt.BorderLayout.CENTER);

        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        add(toolbar, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    org.openide.explorer.view.BeanTreeView messagesTreeView;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables

    private class ModelChildren extends ChildFactory<MessagesTopComponentModel> {

        @Override
        protected boolean createKeys(final List<MessagesTopComponentModel> toPopulate) {
            result.allInstances().stream()
                    .flatMap(m -> m.findAll().stream())
                    .sorted(Comparator.comparing(m -> m.getProviderInfo().getDisplayName(), Collator.getInstance(Locale.getDefault())))
                    .forEach(toPopulate::add);
            return true;
        }

        @Override
        protected Node createNodeForKey(MessagesTopComponentModel key) {
            return new FilterNode(key.getRemoteMessagesModel().getNodeDelegate(), Children.create(new MessagesChildren(key), true));
        }

        private void update() {
            refresh(false);
        }

    }

    private class MessagesChildren extends ChildFactory.Detachable<String> {

        private final MessagesTopComponentModel model;

        MessagesChildren(MessagesTopComponentModel model) {
            this.model = model;
        }

        @Override
        protected void addNotify() {
            this.model.getRemoteMessagesModel().registerEventListener(this);
        }

        @Override
        protected void removeNotify() {
            this.model.getRemoteMessagesModel().unregisterEventListener(this);
        }

        @Override
        protected boolean createKeys(final List<String> toPopulate) {
            synchronized (this) {
                model.getRemoteMessagesModel().getRemoteChannels().forEach(toPopulate::add);
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(String key) {
            synchronized (this) {
                return model.getRemoteMessagesModel().getRemoteChannel(key).getNodeDelegate();
            }
        }

        @Subscribe
        void update(final ChangeEvent evt) {
            refresh(false);
        }

    }
}
