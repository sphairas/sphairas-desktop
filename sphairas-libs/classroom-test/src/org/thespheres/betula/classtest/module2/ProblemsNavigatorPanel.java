/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.module2;

import java.awt.BorderLayout;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ProxyLookup;
import org.thespheres.betula.classtest.model.ClassroomTestEditor2;
import org.thespheres.betula.ui.util.FilterNodeProvider;

/**
 *
 * @author boris.heithecker
 */
@Messages({"ProblemsNavigatorPanel.displayname=Aufgaben"})
@NavigatorPanel.Registration(mimeType = "text/betula-classtest-file+xml", displayName = "#ProblemsNavigatorPanel.displayname", position = 1000)
public class ProblemsNavigatorPanel extends JPanel implements ExplorerManager.Provider, NavigatorPanel, LookupListener {

    private final BeanTreeView tree = new BeanTreeView();
    private final ExplorerManager manager;
    private final Lookup lookup;
    private Result<ClasstestDataObject> lkpres;
    private final Map<Node, FilterNode> nodes = new WeakHashMap<>();

    public ProblemsNavigatorPanel() {
        this.manager = new ExplorerManager();
        final ActionMap map = getActionMap();
        map.put("delete", ExplorerUtils.actionDelete(manager, false));
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
        return NbBundle.getMessage(ProblemsNavigatorPanel.class, "ProblemsNavigatorPanel.displayname");
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
        ExplorerUtils.activateActions(manager, true);
        lkpres = context.lookupResult(ClasstestDataObject.class);
        lkpres.addLookupListener(this);
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
                .map(ProblemsNode::createProblemsNode)
                .orElse(Node.EMPTY);
        Mutex.EVENT.writeAccess(() -> manager.setRootContext(n));
    }

    private static final class ProblemsNode extends FilterNode {

        private ProblemsNode(Node original, Node withChildren) {
            super(original, new FilterNode.Children(withChildren), new ProxyLookup(original.getLookup(), withChildren.getLookup()));
        }

        static Node createProblemsNode(ClasstestDataObject jdo) {
            Node n = jdo.getNodeDelegate();
            ClassroomTestEditor2 ej = jdo.getLookup().lookup(ClassroomTestEditor2.class);
            if (ej != null) {
                for (FilterNodeProvider fnp : MimeLookup.getLookup(ClasstestDataObject.CLASSTEST_MIME).lookupAll(FilterNodeProvider.class)) {
                    FilterNode fn = fnp.createFilterNode(n, ej);
                    if (fn != null) {
                        return fn;
                    }
                }
                return new ProblemsNode(n, ej.getNodeWithProblemChildren());
            }
            return null;
        }
    }
}
