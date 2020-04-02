/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.navigatorui;

import org.thespheres.betula.admin.units.RemoteStudent;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
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
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;

/**
 *
 * @author boris.heithecker
 */
@Messages({"RemoteUnitStudentsNavigatorPanel.displayname=Schülerinnen/Schüler"})
@Registration(mimeType = "application/betula-unit-data", displayName = "#RemoteUnitStudentsNavigatorPanel.displayname", position = 2000)
public class RemoteUnitStudentsNavigatorPanel extends JPanel implements ExplorerManager.Provider, NavigatorPanel, LookupListener, Lookup.Provider {

    private final BeanTreeView tree = new BeanTreeView();
    private final ExplorerManager manager;
    private final Lookup lookup;
    private Result<PrimaryUnitOpenSupport> lkpres;
    private final Map<Node, FilterNode> nodes = new WeakHashMap<>();

    public RemoteUnitStudentsNavigatorPanel() {
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
        return NbBundle.getMessage(RemoteUnitStudentsNavigatorPanel.class, "RemoteUnitStudentsNavigatorPanel.displayname");
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
        lkpres = context.lookupResult(PrimaryUnitOpenSupport.class);
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
                .map(result -> (Node) nodes.computeIfAbsent(result.getNodeDelegate(), no -> new FilterNode(no, Children.create(new UnitChildren(result), true))))
                .orElse(Node.EMPTY);
        Mutex.EVENT.writeAccess(() -> manager.setRootContext(n));
    }

    private static class UnitChildren extends ChildFactory<RemoteStudent> implements PropertyChangeListener {

        private RemoteUnitsModel  rModel;

        @SuppressWarnings("LeakingThisInConstructor")
        private UnitChildren(PrimaryUnitOpenSupport support) {
            try {
                this.rModel = support.getRemoteUnitsModel(RemoteUnitsModel.INITIALISATION.STUDENTS);
                rModel.addPropertyChangeListener(this);
            } catch (IOException ex) {
            }
        }

        @Override
        protected boolean createKeys(List<RemoteStudent> toPopulate) {
            if (rModel != null) {
                toPopulate.addAll(rModel.getStudents());
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(RemoteStudent key) {
            return new RemoteStudentNode(key, rModel);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String n = evt.getPropertyName();
            if (n.equals(RemoteUnitsModel.PROP_INITIALISATION) || n.equals(RemoteUnitsModel.PROP_STUDENTS)) {
                refresh(false);
            }
        }

    }
}
