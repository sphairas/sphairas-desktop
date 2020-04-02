/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.ui;

import java.awt.BorderLayout;
import java.util.Objects;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
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
import org.thespheres.betula.termreport.TermReport;
import org.thespheres.betula.termreport.module.TermReportDataObject;

/**
 *
 * @author boris.heithecker
 */
@Messages({"TermReportNavigatorPanel.displayName=Bewertungen"})
@Registration(mimeType = "text/term-report-file+xml", displayName = "#TermReportNavigatorPanel.displayName")
public class TermReportNavigatorPanel extends JPanel implements ExplorerManager.Provider, NavigatorPanel, LookupListener {

    private final BeanTreeView tree = new BeanTreeView();
    private final ExplorerManager manager;
    private final Lookup lookup;
    private Result<TermReportDataObject> lkpres;

    public TermReportNavigatorPanel() {
        this.manager = new ExplorerManager();
        ActionMap map = getActionMap();
        map.put("delete", ExplorerUtils.actionDelete(manager, false));
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
        return NbBundle.getMessage(TermReportNavigatorPanel.class, "TermReportNavigatorPanel.displayName");
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
        ExplorerUtils.activateActions(manager, true);
        lkpres = context.lookupResult(TermReportDataObject.class);
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
                .map(d -> d.getLookup().lookup(TermReport.class))
                .filter(Objects::nonNull)
                .map(tr -> (Node) new FilterNode(tr.getNodeDelegate(), new FilterNode.Children(tr.getNodeDelegate())))
                .orElse(Node.EMPTY);
        Mutex.EVENT.writeAccess(() -> manager.setRootContext(n));
//        ExplorerUtils.activateActions(manager, true);
    }
}
