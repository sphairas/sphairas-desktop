/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.configui;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.UndoRedo;
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
import org.thespheres.betula.admin.units.ProviderNode;
import org.thespheres.betula.admin.units.RemoteSignee;
import org.thespheres.betula.admin.units.RemoteSignees;
import org.thespheres.betula.admin.units.SigneesTopComponentModel;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.util.Signees;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "SigneesTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "leftSlidingSide", openAtStartup = true)
@ActionID(category = "Window", id = "org.thespheres.betula.admin.units.configui.SigneesTopComponent")
@ActionReference(path = "Menu/Window/betula-beans-services-windows" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#SigneesTopComponent.openAction.displayName",
        preferredID = "SigneesTopComponent")
@NbBundle.Messages({"SigneesTopComponent.openAction.displayName=Unterzeichner",
    "SigneesTopComponent.name=Unterzeichener"})
public class SigneesTopComponent extends CloneableTopComponent implements ExplorerManager.Provider, LookupListener {
    
    private final ExplorerManager manager;
    private final Node modelRoot;
    private final ModelChildren modelChildren;
    private final Lookup.Result<SigneesTopComponentModel.Provider> result;
    protected final UndoRedo.Manager undoRedo = new UndoRedo.Manager();
    
    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public SigneesTopComponent() {
        manager = new ExplorerManager();
        ActionMap map = getActionMap();
        map.put("delete", ExplorerUtils.actionDelete(manager, false));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        associateLookup(ExplorerUtils.createLookup(manager, map));
        initComponents();
        setName(NbBundle.getMessage(SigneesTopComponent.class, "SigneesTopComponent.name"));
        modelChildren = new ModelChildren();
        modelRoot = new AbstractNode(Children.create(modelChildren, true));
        result = Lookup.getDefault().lookupResult(SigneesTopComponentModel.Provider.class);
        result.addLookupListener(this);
        resultChanged(null);
    }
    
    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    @Override
    public void resultChanged(final LookupEvent ev) {
        final List<SigneesTopComponentModel> l = result.allInstances().stream()
                .flatMap(m -> m.findAll().stream())
                .collect(Collectors.toList());
        if (l.isEmpty()) {
            manager.setRootContext(Node.EMPTY);
        } else if (l.size() == 1) {
            final Node single = modelChildren.createNodeForKey(l.iterator().next());
            manager.setRootContext(single);
        } else {
            manager.setRootContext(modelRoot);
            modelChildren.update();
        }
    }
    
    @Override
    protected void componentActivated() {
        modelChildren.update();
    }
    
    @Override
    public UndoRedo getUndoRedo() {
        return undoRedo;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        beanTreeView = new org.openide.explorer.view.BeanTreeView();
        toolbar = new javax.swing.JToolBar();

        setLayout(new java.awt.BorderLayout());

        beanTreeView.setRootVisible(false);
        add(beanTreeView, java.awt.BorderLayout.CENTER);

        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        add(toolbar, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.openide.explorer.view.BeanTreeView beanTreeView;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables

    private class ModelChildren extends ChildFactory<SigneesTopComponentModel> {
        
        @Override
        protected boolean createKeys(final List<SigneesTopComponentModel> toPopulate) {
            result.allInstances().stream()
                    .flatMap(m -> m.findAll().stream())
                    .sorted(Comparator.comparing(m -> m.getProviderInfo().getDisplayName(), Collator.getInstance(Locale.getDefault())))
                    .forEach(toPopulate::add);
            return true;
        }
        
        @Override
        protected Node createNodeForKey(final SigneesTopComponentModel key) {
            return new FilterNode(new ProviderNode(key.getProvider()), Children.create(new SigneeChildren(key), true));
        }
        
        private void update() {
            refresh(false);
        }
        
    }
    
    private class SigneeChildren extends ChildFactory.Detachable<Signee> {
        
        private final SigneesTopComponentModel model;
        
        SigneeChildren(SigneesTopComponentModel model) {
            this.model = model;
        }
        
        @Override
        protected void addNotify() {
            model.addUndoableEditListener(undoRedo);
        }
        
        @Override
        protected void removeNotify() {
            model.removeUndoableEditListener(undoRedo);
        }
        
        @Override
        protected boolean createKeys(final List<Signee> toPopulate) {
            if (model != null) {
                synchronized (this) {
                    Signees.get(model.getSigneesProviderUrl())
                            .ifPresent(s -> {
                                final Comparator<Signee> cmp = Comparator.comparing(sig -> getComparingKey(s, sig), Collator.getInstance(Locale.getDefault()));
                                s.getSigneeSet().stream()
                                        .sorted(cmp)
                                        .forEach(toPopulate::add);
                            });
                }
            }
            return true;
        }
        
        @Override
        protected Node createNodeForKey(Signee key) {
            synchronized (this) {
                return model.nodeForKey(key);
            }
        }
        
        protected String getComparingKey(Signees signees, Signee s) {
            RemoteSignee rs = RemoteSignees.find(signees, s);
            return rs.getCommonName();
        }
        
    }
    
}
