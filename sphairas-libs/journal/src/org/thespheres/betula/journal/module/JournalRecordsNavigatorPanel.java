/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.module;

import java.awt.BorderLayout;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.spi.navigator.NavigatorPanel.Registration;
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

/**
 *
 * @author boris.heithecker
 */
@Messages({"JournalRecordsNavigatorPanel.displayName=Stunden/Termine"})
@Registration(mimeType = "text/betula-journal-file+xml", displayName = "#JournalRecordsNavigatorPanel.displayName")
public class JournalRecordsNavigatorPanel extends JPanel implements ExplorerManager.Provider, NavigatorPanel, LookupListener {

    private final BeanTreeView tree = new BeanTreeView();
    private final ExplorerManager manager;
    private final Lookup lookup;
    private Result<JournalDataObject> lkpres;

    public JournalRecordsNavigatorPanel() {
        this.manager = new ExplorerManager();
        final ActionMap map = getActionMap();
        map.put("delete", ExplorerUtils.actionDelete(manager, false));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
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
        return NbBundle.getMessage(JournalRecordsNavigatorPanel.class, "JournalRecordsNavigatorPanel.displayName");
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
        lkpres = context.lookupResult(JournalDataObject.class);
        lkpres.addLookupListener(this);
        ExplorerUtils.activateActions(manager, true);
        resultChanged(null);

//        WindowManager.getDefault().findTopComponent("JournalNotesEditor").open();
//        JournalNotesSupport.getInstance().open();
    }

    @Override
    public void panelDeactivated() {
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
                //                .map(d -> d.getLookup().lookup(TermReport.class))
                //                .filter(Objects::nonNull)
                .map(JournalNode::createJournalNode)
                .orElse(Node.EMPTY);
        Mutex.EVENT.writeAccess(() -> manager.setRootContext(n));
//        ExplorerUtils.activateActions(manager, true);
    }

    private static final class JournalNode extends FilterNode {

        private JournalNode(Node original, Node withChildren) {
            super(original, new FilterNode.Children(withChildren), new ProxyLookup(original.getLookup(), withChildren.getLookup()));
        }

        static Node createJournalNode(JournalDataObject jdo) {
            final Node n = jdo.getNodeDelegate();
            final EditableJournalImpl ej = jdo.getLookup().lookup(EditableJournalImpl.class);
            if (ej != null) {
                return new JournalNode(n, ej.getNodeWithRecordChildren());
            }
            return null;
        }
    }
}
