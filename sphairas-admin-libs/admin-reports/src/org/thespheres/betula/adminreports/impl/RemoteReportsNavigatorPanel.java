/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.impl;

import com.google.common.eventbus.Subscribe;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.spi.navigator.NavigatorPanel.Registration;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.reports.model.EditableReportCollection;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 */
@Messages({"RemoteReportsNavigatorPanel.displayname=Einträge"})
@Registration(mimeType = "text/betula-remote-reports+xml", displayName = "#RemoteReportsNavigatorPanel.displayname", position = 1000)
public class RemoteReportsNavigatorPanel extends JPanel implements ExplorerManager.Provider, NavigatorPanel, LookupListener, Lookup.Provider {
    
    private final BeanTreeView tree = new BeanTreeView();
    private final ExplorerManager manager;
    private final Lookup lookup;
    private Result<RemoteReportsSupport> lkpres;
    private final Map<Node, FilterNode> nodes = new WeakHashMap<>();
    
    public RemoteReportsNavigatorPanel() {
        this.manager = new ExplorerManager();
        final ActionMap map = getActionMap();
        map.put("delete", ExplorerUtils.actionDelete(manager, true));
        map.put("cut", ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put("paste", ExplorerUtils.actionPaste(manager));
        lookup = ExplorerUtils.createLookup(manager, map);
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        add(tree, BorderLayout.CENTER);
    }
    
    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(RemoteReportsNavigatorPanel.class, "RemoteReportsNavigatorPanel.displayname");
    }
    
    @Override
    public String getDisplayHint() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public JComponent getComponent() {
        return this;
    }
    
    @Override
    public void panelActivated(Lookup context) {
//        this.context = context;
        lkpres = context.lookupResult(RemoteReportsSupport.class);
        lkpres.addLookupListener(this);
        ExplorerUtils.activateActions(manager, true);
        resultChanged(null);
    }
    
    @Override
    public void panelDeactivated() {
        //lookup = null;
        ExplorerUtils.activateActions(manager, false);
        lkpres.removeLookupListener(this);
    }
    
    @Override
    public Lookup getLookup() {
        return lookup;
    }
    
    @Override
    public void resultChanged(LookupEvent ev) {
        final Node n = lkpres.allInstances().stream()
                .findAny()
                .filter(result -> result.getDocument() != null)
                .map(result -> (Node) nodes.computeIfAbsent(result.getNodeDelegate(), no -> new FilterNode(no, Children.create(new ReportChildren(result), true))))
                .orElse(Node.EMPTY);
        Mutex.EVENT.writeAccess(() -> manager.setRootContext(n));
    }
    
    private static class ReportChildren extends ChildFactory<RemoteEditableReport> implements PropertyChangeListener {
        
        private final RemoteReportsSupport support;
        
        @SuppressWarnings("LeakingThisInConstructor")
        private ReportChildren(RemoteReportsSupport support) {
            this.support = support;
//            support.getRemoteReportsModel().addPropertyChangeListener(this);
            final PropertyChangeSupport pcs = (PropertyChangeSupport) support.getDocument().getProperty(PropertyChangeSupport.class);
            pcs.addPropertyChangeListener(this);
        }
        
        @Override
        protected boolean createKeys(List<RemoteEditableReport> toPopulate) {
            final RemoteEditableReportCollection collection = (RemoteEditableReportCollection) support.getDocument().getProperty(EditableReportCollection.class.getCanonicalName());
            if (collection != null) {
                toPopulate.addAll(collection.getReports());
            }
            return true;
        }
        
        @Override
        protected Node createNodeForKey(final RemoteEditableReport key) {
            return new RemoteReportNode(key, support.getRemoteReportsModel());
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String n = evt.getPropertyName();
            if (n.equals(EditableReportCollection.class.getCanonicalName())) {
                refresh(false);
            }
        }
        
        @Subscribe
        public void collectionChange(CollectionChangeEvent evt) {
            if (evt.getCollectionName().equals(EditableReportCollection.COLLECTION_REPORTS)) {
                refresh(false);
            }
        }
        
    }
}
