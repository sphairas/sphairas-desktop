/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig.ui;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.ActionMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import org.thespheres.betula.adminconfig.ConfigNodeTopComponentNodeList;
import org.thespheres.betula.admin.units.ProviderNode;

/**
 * Top component which displays something.
 */
@TopComponent.Description(preferredID = "ConfigTopComponent",
        iconBase="org/thespheres/betula/adminconfig/resources/gear.png", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "leftSlidingSide", position = 17000, openAtStartup = true)
@ActionID(category = "Window", id = "org.thespheres.betula.admin.units.configui.ConfigTopComponent")
@ActionReference(path = "Menu/Window", position = 17000)
@TopComponent.OpenActionRegistration(displayName = "#ConfigTopComponent.openAction.displayName",
        preferredID = "ConfigTopComponent")
@NbBundle.Messages({"ConfigTopComponent.openAction.displayName=Konfigurationen",
    "ConfigTopComponent.name=Konfigurationen"})
public class ConfigTopComponent extends CloneableTopComponent implements ExplorerManager.Provider, LookupListener {

    private final ExplorerManager manager;
    private final Node modelRoot;
    private final ProviderChildren modelChildren;
    private final Lookup.Result<ConfigNodeTopComponentNodeList.Provider> result;
    protected final UndoRedo.Manager undoRedo = new UndoRedo.Manager();

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public ConfigTopComponent() {
        manager = new ExplorerManager();
        ActionMap map = getActionMap();
        map.put("delete", ExplorerUtils.actionDelete(manager, false));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        associateLookup(ExplorerUtils.createLookup(manager, map));
        initComponents();
        setName(NbBundle.getMessage(ConfigTopComponent.class, "ConfigTopComponent.name"));
        modelChildren = new ProviderChildren();
        modelRoot = new AbstractNode(Children.create(modelChildren, true));
        result = Lookup.getDefault().lookupResult(ConfigNodeTopComponentNodeList.Provider.class);
        result.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    @Override
    public void resultChanged(final LookupEvent ev) {
        final Set<String> l = result.allInstances().stream()
                    .flatMap(m -> m.nodeLists().stream())
                    .map(p -> p.getProvider())
                    .collect(Collectors.toSet());
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

    private class ProviderChildren extends ChildFactory<String> {

        @Override
        protected boolean createKeys(final List<String> toPopulate) {
            result.allInstances().stream()
                    .flatMap(m -> m.nodeLists().stream())
                    .sorted(Comparator.comparing(m -> m.getProviderInfo().getDisplayName(), Collator.getInstance(Locale.getDefault())))
                    .map(p -> p.getProvider())
                    .distinct()
                    .forEach(toPopulate::add);
            return true;
        }

        @Override
        protected Node createNodeForKey(final String key) {
            return new FilterNode(new ProviderNode(key), Children.create(new ConfigChildren(key), true));
//            Children.Keys<?> children = (Children.Keys<?>) f.getChildren();
//            children.
        }

        private void update() {
            refresh(false);
        }

    }

    private class ConfigChildren extends ChildFactory.Detachable<ConfigNodeTopComponentNodeList.Key> implements ChangeListener {

        private final String provider;

        ConfigChildren(String provider) {
            this.provider = provider;
        }

        @Override
        protected void removeNotify() {
            result.allInstances().stream()
                    .flatMap(m -> m.nodeLists().stream())
                    .filter(n -> n.getProvider().equals(provider))
                    .forEach(l -> l.removeChangeListener(this));
        }

        @Override
        protected void addNotify() {
            result.allInstances().stream()
                    .flatMap(m -> m.nodeLists().stream())
                    .filter(n -> n.getProvider().equals(provider))
                    .forEach(l -> l.addChangeListener(this));
        }

        @Override
        protected boolean createKeys(final List<ConfigNodeTopComponentNodeList.Key> toPopulate) {
            if (provider != null) {
                final List<ConfigNodeTopComponentNodeList> nl = result.allInstances().stream()
                        .flatMap(m -> m.nodeLists().stream())
                        .filter(n -> n.getProvider().equals(provider))
                        .sorted(Comparator.comparingInt(n -> n.getPreferredPosition()))
                        .collect(Collectors.toList());
                nl.stream()
                        .flatMap(l -> Arrays.stream(l.getKeys()))
                        .forEach(toPopulate::add);
            }
            return true;
        }

        @Override
        protected Node[] createNodesForKey(final ConfigNodeTopComponentNodeList.Key key) {
            return key.getConfigNodeTopComponentNodeList().getNodes(key);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }

    }

}
