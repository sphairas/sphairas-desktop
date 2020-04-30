/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.*;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ListView;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.thespheres.betula.curriculum.Curriculum;
import org.thespheres.betula.curriculumimport.config.CurriculumConfigNodeList;
import org.thespheres.betula.curriculumimport.config.CurriculumFilterNode;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"SelectCurriculumVisualPanel.step.name=Stundentafel auswählen"})
public class SelectCurriculumVisualPanel extends javax.swing.JPanel implements ExplorerManager.Provider {

    private final ExplorerManager manager;
    private final ListView view;
    private final FileChildren fileChildren;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    SelectCurriculumVisualPanel() {
        manager = new ExplorerManager();
        fileChildren = new FileChildren();
        final AbstractNode root = new AbstractNode(Children.create(fileChildren, true));
        manager.setRootContext(root);
        setLayout(new java.awt.BorderLayout());
        view = new ListView();
        final JComponent list = (JComponent) view.getViewport().getComponent(0);
        list.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        view.setShowParentNode(false);
        view.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(view, java.awt.BorderLayout.CENTER);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(StundentafelImportConfigVisualPanel.class, "SelectCurriculumVisualPanel.step.name");
    }

    void store(final StundentafelImportSettings d) {
        final Node[] sel = this.manager.getSelectedNodes();
        final Node n = sel.length == 1 ? sel[0] : null;
        d.setCurriculum(n.getLookup().lookup(DataObject.class));
        if (fileChildren.model != null) {
            fileChildren.model.removeChangeListener(fileChildren);
            fileChildren.model = null;
        }
    }

    void read(final StundentafelImportSettings settings) {
        final String provider = settings.getImportTargetProperty().getProviderInfo().getURL();
        fileChildren.model = CurriculumConfigNodeList.find(provider);
        fileChildren.stateChanged(null);
        fileChildren.model.addChangeListener(fileChildren);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    private class FileChildren extends ChildFactory<CurriculumConfigNodeList.Key<DataObject>> implements ChangeListener {

        CurriculumConfigNodeList model;

        @Override
        protected boolean createKeys(final List<CurriculumConfigNodeList.Key<DataObject>> toPopulate) {
            if (model != null) {
                synchronized (this) {
                    final CurriculumConfigNodeList.Key<DataObject>[] keys = model.getKeys();
                    Arrays.stream(keys).forEach(toPopulate::add);
                }
            }
            return true;
        }

        @Override
        protected Node[] createNodesForKey(CurriculumConfigNodeList.Key<DataObject> key) {
            return Arrays.stream(model.getNodes(key))
                    .map(CurriculumFilterNode::new)
                    .toArray(Node[]::new);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }

    }

    public static class SelectCurriculumPanel implements WizardDescriptor.Panel<StundentafelImportSettings>, PropertyChangeListener {

        private final ChangeSupport cSupport = new ChangeSupport(this);
        private SelectCurriculumVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public SelectCurriculumVisualPanel getComponent() {
            if (component == null) {
                component = new SelectCurriculumVisualPanel();
                component.manager.addPropertyChangeListener(this);
            }
            return component;
        }

        @Override
        public HelpCtx getHelp() {
            // Show no Help button for this panel:
            return HelpCtx.DEFAULT_HELP;
            // If you have context help:
            // return new HelpCtx("help.key.here");
        }

        @Override
        public boolean isValid() {
            // If it depends on some condition (form filled out...) and
            // this condition changes (last form field filled in...) then
            // use ChangeSupport to implement add/removeChangeListener below.
            // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
            return getComponent().manager.getSelectedNodes().length == 1;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            cSupport.fireChange();
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            cSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cSupport.removeChangeListener(l);
        }

        @Override
        public void readSettings(final StundentafelImportSettings wiz) {
            getComponent().read(wiz);
        }

        @Override
        public void storeSettings(final StundentafelImportSettings wiz) {
            getComponent().store(wiz);
        }

    }

}
