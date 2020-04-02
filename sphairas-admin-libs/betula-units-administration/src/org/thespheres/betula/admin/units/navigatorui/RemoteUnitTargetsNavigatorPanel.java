/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.navigatorui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
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
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.document.DocumentId;

/**
 *
 * @author boris.heithecker
 */
@Messages({"RemoteUnitTargetsNavigatorPanel.displayname=Kurse"})
@Registration(mimeType = "application/betula-unit-data", displayName = "#RemoteUnitTargetsNavigatorPanel.displayname", position = 1000)
public class RemoteUnitTargetsNavigatorPanel extends JPanel implements ExplorerManager.Provider, NavigatorPanel, LookupListener {

    private final BeanTreeView tree = new BeanTreeView();
    private final ExplorerManager manager;
    private final Lookup lookup;
    private Result<AbstractUnitOpenSupport> lkpres;
    private final Map<Node, FilterNode> nodes = new WeakHashMap<>();

    public RemoteUnitTargetsNavigatorPanel() {
        this.manager = new ExplorerManager();
        ActionMap map = getActionMap();
        map.put("delete", ExplorerUtils.actionDelete(manager, true));
        map.put("cut", ExplorerUtils.actionCut(manager));
        map.put("copy", ExplorerUtils.actionCopy(manager));
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
        return NbBundle.getMessage(RemoteUnitTargetsNavigatorPanel.class, "RemoteUnitTargetsNavigatorPanel.displayname");
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
        lkpres = context.lookupResult(AbstractUnitOpenSupport.class);
        lkpres.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public void panelDeactivated() {
        //lookup = null;
//        ExplorerUtils.activateActions(manager, false);
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
                .map(result -> (Node) nodes.computeIfAbsent(result.getNodeDelegate(), no -> new FilterNode(no, Children.create(new TargetChildren(result), true))))
                .orElse(Node.EMPTY);
//        manager.setRootContext(n);
        Mutex.EVENT.writeAccess(() -> manager.setRootContext(n));
    }

    private static class TargetChildren extends ChildFactory<DocumentId> implements PropertyChangeListener {

        private RemoteUnitsModel rModel;
        private final Map<DocumentId, RemoteTargetNode> registry = new HashMap<>();
        private DocumentsModel docModel;

        @SuppressWarnings("LeakingThisInConstructor")
        private TargetChildren(AbstractUnitOpenSupport support) {
            try {
                rModel = support.getRemoteUnitsModel(RemoteUnitsModel.INITIALISATION.STUDENTS);
                docModel = support.findDocumentsModel();
                rModel.addPropertyChangeListener(this);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        protected boolean createKeys(List<DocumentId> toPopulate) {
            if (rModel != null && docModel != null) {
                final Map<DocumentId, List<RemoteTargetAssessmentDocument>> model = rModel.getTargets().stream()
                        .map(RemoteTargetAssessmentDocument::getDocumentId)
                        .collect(Collectors.groupingBy(docModel::convert,
                                Collectors.mapping(rModel::getTarget,
                                        Collectors.toList())));
                model.keySet().stream()
                        .map(did -> RemoteTargetNode.create(model.get(did), rModel, did))
                        .peek(n -> registry.put(n.key(), n))
                        .sorted(Comparator.comparing(RemoteTargetNode::getDisplayName, Collator.getInstance(Locale.GERMAN)))
                        .map(RemoteTargetNode::key)
                        .forEach(toPopulate::add);
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(DocumentId key) {
            return registry.get(key);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String n = evt.getPropertyName();
            if (n.equals(RemoteUnitsModel.PROP_INITIALISATION) || n.equals(RemoteUnitsModel.PROP_TARGETS)) {
                refresh(false);
            }
        }

    }
}
