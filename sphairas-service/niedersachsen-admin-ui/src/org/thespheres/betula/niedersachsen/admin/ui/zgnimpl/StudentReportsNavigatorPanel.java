/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl;

import java.awt.BorderLayout;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import org.thespheres.betula.TermId;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.util.StudentComparator;

/**
 *
 * @author boris.heithecker
 */
@Messages({"StudentReportsNavigatorPanel.displayname=Zeugnisse"})
@Registration(mimeType = "application/betula-report-data", displayName = "#StudentReportsNavigatorPanel.displayname", position = 5000)
public class StudentReportsNavigatorPanel extends JPanel implements ExplorerManager.Provider, NavigatorPanel, LookupListener, Lookup.Provider {
    
    private final BeanTreeView tree = new BeanTreeView();
    private final ExplorerManager manager;
    private final Lookup lookup;
    private Result<ZeugnisAngabenModel> lkpres;
    private final Map<Node, FilterNode> nodes = new WeakHashMap<>();
    
    public StudentReportsNavigatorPanel() {
        this.manager = new ExplorerManager();
        ActionMap map = getActionMap();
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
        return NbBundle.getMessage(StudentReportsNavigatorPanel.class, "StudentReportsNavigatorPanel.displayname");
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
    public void panelActivated(final Lookup context) {
//        this.context = context;
        lkpres = context.lookupResult(ZeugnisAngabenModel.class);
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
                .map(result -> (Node) nodes.computeIfAbsent(result.getNodeDelegate(), no -> new FilterNode(no, Children.create(new ReportChildren(result), true))))
                .orElse(Node.EMPTY);
        Mutex.EVENT.writeAccess(() -> manager.setRootContext(n));
    }
    
    private static class ReportChildren extends ChildFactory.Detachable<ReportData2> implements ChangeListener {
        
        private final ZeugnisAngabenModel model;
        
        @SuppressWarnings("LeakingThisInConstructor")
        private ReportChildren(final ZeugnisAngabenModel m) {
            this.model = m;
        }
        
        @Override
        protected void removeNotify() {
            model.removeChangeListener(this);
        }
        
        @Override
        protected void addNotify() {
            model.addChangeListener(this);
        }
        
        @Override
        protected boolean createKeys(final List<ReportData2> toPopulate) {
            final TermId term = model.getCurrentTermId();
            final StudentComparator sc = new StudentComparator();
            if (term != null) {
                model.getItemsModel().getReports(term).stream()
                        .sorted(Comparator.comparing(r -> r.getRemoteStudent(), sc))
                        .forEach(toPopulate::add);
            }
            return true;
        }
        
        @Override
        protected Node createNodeForKey(ReportData2 key) {
            return key.getNodeDelegate();
        }
        
        @Override
        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }
        
    }
}
