/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.module2;

import java.awt.BorderLayout;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.DefaultEditorKit;
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

/**
 *
 * @author boris.heithecker
 */
@Messages({"StudentsNavigatorPanel.displayname=Teilnehmer/innen"})
@NavigatorPanel.Registration(mimeType = "text/betula-classtest-file+xml", displayName = "#StudentsNavigatorPanel.displayname", position = 1000)
public class StudentsNavigatorPanel extends JPanel implements ExplorerManager.Provider, NavigatorPanel, LookupListener {

    private final BeanTreeView tree = new BeanTreeView();
    private final ExplorerManager manager;
    private final Lookup lookup;
    private Result<ClasstestDataObject> lkpres;

    public StudentsNavigatorPanel() {
        this.manager = new ExplorerManager();
      final  ActionMap map = getActionMap();
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
        return NbBundle.getMessage(StudentsNavigatorPanel.class, "StudentsNavigatorPanel.displayname");
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
                .map(StudentsNode::createStudentsNode)
                .orElse(Node.EMPTY);
        Mutex.EVENT.writeAccess(() -> manager.setRootContext(n));
    }

    private static final class StudentsNode extends FilterNode {

        private StudentsNode(Node original, Node withChildren) {
            super(original, new FilterNode.Children(withChildren), new ProxyLookup(original.getLookup(), withChildren.getLookup()));
        }

        static Node createStudentsNode(ClasstestDataObject jdo) {
            final Node n = jdo.getNodeDelegate();
            final ClassroomTestEditor2 ej = jdo.getLookup().lookup(ClassroomTestEditor2.class);
            if (ej != null) {
                return new StudentsNode(n, ej.getNodeWithStudentChildren());
            }
            return null;
        }
    }
}
