/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ListView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.util.AbstractReloadableMarkerConvention;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"EditBemerkungenPaletteTopComponent.openAction=Bemerkungen (Berichtshefte)"})
@ConvertAsProperties(dtd = "-//org.thespheres.betula.niedersachsen.admin.ui.bemerkungen//EditBemerkungenPaletteTopComponent//EN",
        autostore = false)
@TopComponent.Description(preferredID = "EditBemerkungenPaletteTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "properties", openAtStartup = false)
public class EditBemerkungenPaletteTopComponent extends CloneableTopComponent implements ExplorerManager.Provider {

    private final ListView list = new ListView();
    private final Listener listener = new Listener();
    private final JScrollPane scrollPane;
    private final JToolBar toolbar;
    private final ExplorerManager manager;
    private EditBemerkungenEnv current;
    private final ConventionChildren conventionChildren = new ConventionChildren();
    private final Node root;
//    private Lookup.Result<EditBemerkungenEnv> result;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public EditBemerkungenPaletteTopComponent() {
        super();
        scrollPane = new JScrollPane();
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
//        toolbar.add(Box.createHorizontalGlue());
        setLayout(new BorderLayout());
        setBorder(null);
        list.setDragSource(true);
        scrollPane.setViewportView(list);
        add(scrollPane, BorderLayout.CENTER);
        add(toolbar, BorderLayout.NORTH);
        this.manager = new ExplorerManager();
//        final ActionMap map = getActionMap();
//        map.put("delete", ExplorerUtils.actionDelete(manager, false));
//        map.put("cut", ExplorerUtils.actionCut(manager));
//        map.put("copy", ExplorerUtils.actionCopy(manager));
//        map.put("paste", ExplorerUtils.actionPaste(manager));
//        lookup = ExplorerUtils.createLookup(manager, map);
        final Action gua = new GoUpAction();
        toolbar.add(gua);
        root = new AbstractNode(Children.create(conventionChildren, true));
        manager.setRootContext(root);
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        TopComponent.getRegistry().addPropertyChangeListener(listener);
//        result = Utilities.actionsGlobalContext().lookupResult(EditBemerkungenEnv.class);
//        result.addLookupListener(listener);
        initialize();
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
//        removeAll();
        TopComponent.getRegistry().removePropertyChangeListener(listener);
//        result.removeLookupListener(listener);
//        result = null;
    }

    void initialize() {
        if (!EventQueue.isDispatchThread()) {
            throw new IllegalStateException();
        }
        final TopComponent tc = TopComponent.getRegistry().getActivated();
        final EditBemerkungenEnv jdo = tc == null ? null : tc.getLookup().lookupAll(EditBemerkungenEnv.class).stream()
                .collect(CollectionUtil.singleOrNull());
        if (jdo != null && !Objects.equals(jdo, current)) {
            current = jdo;
            setName(current.getNodeDelegate().getDisplayName());
            conventionChildren.refresh();
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    final class GoUpAction extends AbstractAction {

        public GoUpAction() {
            final Icon icon = ImageUtilities.loadImageIcon("org/thespheres/betula/niedersachsen/admin/ui/resources/arrow-turn-090.png", true);
            putValue(Action.SMALL_ICON, icon);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (list.isTraversalAllowed()) {
                final Node pan = manager.getExploredContext().getParentNode();
                if (pan != null) {
                    manager.setExploredContext(pan, manager.getSelectedNodes());
                }
            }
        }

    }

    final class Listener implements PropertyChangeListener { //, LookupListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case TopComponent.Registry.PROP_TC_CLOSED:
                    TopComponent closed = (TopComponent) evt.getNewValue();
                    EditBemerkungenEnv uos = null;
                    if (closed != null && (uos = closed.getLookup().lookup(EditBemerkungenEnv.class)) != null) {
                        if (Objects.equals(current, uos)) {
                            current = null;
                            conventionChildren.refresh();
                        }
                    }
                    break;
                default:
                    initialize();
                    break;
            }
        }

//        @Override
//        public void resultChanged(LookupEvent ev) {
//            initialize();
//        }
    }

    final class ConventionChildren extends ChildFactory<MarkerConvention> {

        @Override
        protected boolean createKeys(List<MarkerConvention> l) {
            if (current != null) {
                final LocalProperties lp = LocalProperties.find(current.getProvider());
                final String cnv = lp.getProperty("report.notes.conventions");
                if (cnv != null) {
                    Arrays.stream(cnv.split(","))
                            .map(String::trim)
                            .map(MarkerFactory::findConvention)
                            .filter(Objects::nonNull)
                            .forEach(l::add);
                }
                final String customcnv = lp.getProperty("custom.report.notes.conventions");
                if (customcnv != null) {
                    Arrays.stream(customcnv.split(","))
                            .map(String::trim)
                            .map(MarkerFactory::findConvention)
                            .filter(Objects::nonNull)
                            .forEach(l::add);
                }
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(MarkerConvention key) {
            return new ConventionNode(key);
        }

        private void refresh() {
            super.refresh(false);
        }
    }

    final class ConventionNode extends AbstractNode {

        ConventionNode(final MarkerConvention key) {
            super(Children.create(new MarkerChildren(key), true), Lookups.fixed(key, current));
            setDisplayName(key.getDisplayName());
            setIconBaseWithExtension("org/thespheres/betula/niedersachsen/admin/ui/resources/category.png");
        }

    }

    final class MarkerChildren extends ChildFactory<Marker> implements ChangeListener {

        private final MarkerConvention convention;

        @SuppressWarnings({"LeakingThisInConstructor"})
        private MarkerChildren(MarkerConvention key) {
            convention = key;
            if (convention instanceof AbstractReloadableMarkerConvention) {
                ((AbstractReloadableMarkerConvention) convention).addChangeListener(this);
            }
        }

        @Override
        protected boolean createKeys(final List<Marker> l) {
            Arrays.stream(convention.getAllMarkers()).forEach(l::add);
            return true;
        }

        @Override
        protected Node createNodeForKey(final Marker key) {
            return new MarkerNode(convention, key);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }

    }

    final class MarkerNode extends AbstractNode {

        MarkerNode(final MarkerConvention convention, final Marker key) {
            super(Children.LEAF, Lookups.fixed(convention, key, current));
            final ReportData2 sd = ReportContextListener.getDefault().getCurrentReportData();
            setDisplayName(key.getLongLabel(sd != null ? sd.getFormatArgs() : ReportContextListener.getDefault().getDefaultFormatArgs()));
            setIconBaseWithExtension("org/thespheres/betula/niedersachsen/admin/ui/resources/document-horizontal-text.png");
        }

        @Override
        public boolean canCopy() {
            return super.canCopy(); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Transferable drag() throws IOException {
            return super.drag(); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Transferable clipboardCopy() throws IOException {
            return super.clipboardCopy(); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
