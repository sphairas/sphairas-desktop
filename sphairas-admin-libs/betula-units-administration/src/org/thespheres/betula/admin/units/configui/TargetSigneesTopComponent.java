/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.configui;

import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.painter.MattePainter;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarPool;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
//@ConvertAsProperties(
//        dtd = "-//org.thespheres.betula.admin.units.configui//MarkerList//EN",
//        autostore = false
//)
@TopComponent.Description(preferredID = "TargetSigneesTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "markers", openAtStartup = false)
@ActionID(category = "Window", id = "org.thespheres.betula.admin.units.configui.TargetSigneesTopComponent")
@ActionReference(path = "Menu/Window/betula-beans-services-windows" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#TargetSigneesTopComponent.action.displayName",
        preferredID = "TargetSigneesTopComponent"
)
@NbBundle.Messages({
    "TargetSigneesTopComponent.action.displayName=Listenunterzeichner",
    "TargetSigneesTopComponent.displayName=Listenunterzeichner",
    "TargetSigneesTopComponent.contextDisplayName=Listenunterzeichner - {0}",
    "TargetSigneesTopComponent.numDisplayName=Listenunterzeichner - {0} Listen",
    "TargetSigneesTopComponent.toolTip=Dies ist das Fenster zur Ansicht der Listenunterzeichner."
})
public class TargetSigneesTopComponent extends TopComponent {

    private static ToolbarPool pool;
    private final JToolBar toolbar;
    private final Listener listener = new Listener();
    private final UndoRedo.Manager undoRedo = new UndoRedo.Manager();

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public TargetSigneesTopComponent() {
        setLayout(new org.jdesktop.swingx.VerticalLayout());
        toolbar = new javax.swing.JToolBar();//Add-Marker, Copy Insert Delete Selection
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
        toolbar.setFloatable(false);
        add(toolbar);
        addPanels();
        setName(NbBundle.getMessage(TargetSigneesTopComponent.class, "TargetSigneesTopComponent.displayName"));
        setToolTipText(NbBundle.getMessage(TargetSigneesTopComponent.class, "TargetSigneesTopComponent.toolTip"));
    }

    private void addPanels() {
        final ToolbarPool toolbars = getToolbars();
        for (final Toolbar bar : toolbars.getToolbars()) {
            final TitledToolbarPanel p = new TitledToolbarPanel(bar);
            add(p);
        }
    }

    @Override
    protected void componentHidden() {
        TopComponent.getRegistry().removePropertyChangeListener(listener);
//        setCurrentList(new RemoteTargetAssessmentDocument[0]);
    }

    @Override
    protected void componentShowing() {
        TopComponent.getRegistry().addPropertyChangeListener(listener);
    }

    @Override
    public UndoRedo.Manager getUndoRedo() {
        return undoRedo;
    }
//    void writeProperties(java.util.Properties p) {
//        // better to version settings since initial version as advocated at
//        // http://wiki.apidesign.org/wiki/PropertyFiles
//        p.setProperty("version", "1.0");
//        // TODO store your settings
//    }
//
//    void readProperties(java.util.Properties p) {
//        String version = p.getProperty("version");
//        // TODO read your settings according to their version
//    }

    static synchronized ToolbarPool getToolbars() {
        if (pool == null) {
            final FileObject root = FileUtil.getConfigRoot();
            FileObject fo = null;
            try {
                fo = FileUtil.createFolder(root, "TargetSigneesTopComponent/Toolbars");
            } catch (IOException ex) {
                Logger.getLogger(TargetSigneesTopComponent.class.getName()).log(Level.CONFIG, "Cannot create TargetSigneesTopComponent/Toolbars folder.", ex);
            }
            if (fo == null) {
                throw new IllegalStateException("No TargetSigneesTopComponent/Toolbars/");
            }
            final DataFolder folder = DataFolder.findFolder(fo);
            pool = new ToolbarPool(folder);
        }
        return pool;
    }

    private void updateName() {
        final Collection<? extends RemoteTargetAssessmentDocument> all = Utilities.actionsGlobalContext().lookupAll(RemoteTargetAssessmentDocument.class);
        final String dn = all.stream()
                .map(rtad -> rtad.getName().getDisplayName(null))
                .distinct()
                .collect(CollectionUtil.singleOrNull());

        if (dn != null) {
            setName(NbBundle.getMessage(MarkerListTopComponent.class, "MarkerListTopComponent.contextDisplayName", dn));
        } else if (!all.isEmpty()) {
            setName(NbBundle.getMessage(MarkerListTopComponent.class, "MarkerListTopComponent.numDisplayName", all.size()));
        } else {
            setName(NbBundle.getMessage(MarkerListTopComponent.class, "TargetSigneesTopComponent.displayName"));
        }
    }

    private final class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case TopComponent.Registry.PROP_TC_CLOSED:
                    final TopComponent closed = (TopComponent) evt.getNewValue();
//                    if (closed != null && current != null) {
//                        boolean changed = closed.getLookup().lookupAll(RemoteTargetAssessmentDocument.class).stream()
//                                .anyMatch(msp -> Arrays.stream(current).anyMatch(c -> c == msp));
//                        if (changed) {
//                            setCurrentList(new RemoteTargetAssessmentDocument[0]);
//                        }
                    break;
                case TopComponent.Registry.PROP_ACTIVATED:
                    updateName();
                    break;
            }
        }
    }

    private final class TitledToolbarPanel extends JXTitledPanel {

        private final Toolbar internal;

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        private TitledToolbarPanel(Toolbar toolbar) {
            internal = toolbar;
            setTitle(toolbar.getDisplayName());
            getContentContainer().setLayout(new java.awt.BorderLayout());
            final MattePainter p = new MattePainter(UIManager.getColor("Label.background"));
            setTitlePainter(p);
            getContentContainer().add(internal, java.awt.BorderLayout.CENTER);
        }

    }
}
