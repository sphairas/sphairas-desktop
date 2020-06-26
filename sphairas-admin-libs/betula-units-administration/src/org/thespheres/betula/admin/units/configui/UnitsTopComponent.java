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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.ActionMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.ProviderNode;
import org.thespheres.betula.admin.units.UnitsTopComponentModel;
import org.thespheres.betula.services.util.Units;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "UnitsTopComponent",
        iconBase = "org/thespheres/betula/admin/units/resources/table-medium.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "leftSlidingSide", position = 16000, openAtStartup = true)
@ActionID(category = "Window", id = "org.thespheres.betula.admin.units.configui.UnitsTopComponent")
@ActionReference(path = "Menu/Window", position = 16000)
@TopComponent.OpenActionRegistration(displayName = "#UnitsTopComponent.openAction.displayName",
        preferredID = "UnitsTopComponent")
@NbBundle.Messages({"UnitsTopComponent.openAction.displayName=Lerngruppen",
    "UnitsTopComponent.name=Lerngruppen"})
public class UnitsTopComponent extends CloneableTopComponent implements ExplorerManager.Provider, LookupListener {

    private final ExplorerManager manager;
    private final Node modelRoot;
    private final ModelChildren modelChildren;
    private final Lookup.Result<UnitsTopComponentModel.Provider> result;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public UnitsTopComponent() {
        manager = new ExplorerManager();
        ActionMap map = getActionMap();
        map.put("delete", ExplorerUtils.actionDelete(manager, false));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        associateLookup(ExplorerUtils.createLookup(manager, map));
        initComponents();
        setName(NbBundle.getMessage(UnitsTopComponent.class, "UnitsTopComponent.name"));
        modelChildren = new ModelChildren();
        modelRoot = new AbstractNode(Children.create(modelChildren, true));
        result = Lookup.getDefault().lookupResult(UnitsTopComponentModel.Provider.class);
        result.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    @Override
    public void resultChanged(final LookupEvent ev) {
        final List<UnitsTopComponentModel> l = result.allInstances().stream()
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

    private class ModelChildren extends ChildFactory<UnitsTopComponentModel> {

        @Override
        protected boolean createKeys(final List<UnitsTopComponentModel> toPopulate) {
            result.allInstances().stream()
                    .flatMap(m -> m.findAll().stream())
                    .sorted(Comparator.comparing(m -> m.getProviderInfo().getDisplayName(), Collator.getInstance(Locale.getDefault())))
                    .forEach(toPopulate::add);
            return true;
        }

        @Override
        protected Node createNodeForKey(UnitsTopComponentModel key) {
            return new FilterNode(new ProviderNode(key.getProvider()), Children.create(new UnitChildren(key), true));
        }

        private void update() {
            refresh(false);
        }

    }

    private class UnitChildren extends ChildFactory.Detachable<UnitId> implements ChangeListener {

        private final UnitsTopComponentModel model;

        UnitChildren(UnitsTopComponentModel model) {
            this.model = model;
        }

        @Override
        protected void addNotify() {
            Units.get(model.getProviderInfo().getURL())
                    .ifPresent(u -> u.addChangeListener(this));
        }

        @Override
        protected void removeNotify() {
            Units.get(model.getProviderInfo().getURL())
                    .ifPresent(u -> u.removeChangeListener(this));
        }

        @Override
        protected boolean createKeys(List<UnitId> toPopulate) {
            if (model != null) {
                final Comparator<UnitId> cmp = model.comparator();
                synchronized (this) {
                    Units.get(model.getProviderInfo().getURL()).map(Units::getUnits)
                            .map(Set::stream)
                            .orElse(Stream.empty())
                            .sorted(cmp)
                            .forEach(toPopulate::add);
                }
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(UnitId key) {
            synchronized (this) {
                return model.nodeForKey(key);
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }

    }
}
