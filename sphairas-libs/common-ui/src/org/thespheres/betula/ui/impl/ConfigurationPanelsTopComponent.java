/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.impl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.UndoRedo;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.thespheres.betula.ui.ConfigurationPanel;
import org.thespheres.betula.ui.ConfigurationPanelLookupHint;
import org.thespheres.betula.ui.util.UIUtilities;

/**
 *
 * @author boris.heithecker
 */
@ConvertAsProperties(
        dtd = "-//org.thespheres.betula.admin.units.configui//ConfigurationPanelsTopComponent//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ConfigurationPanelsTopComponent",
        iconBase = "org/thespheres/betula/ui/resources/property-blue.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "rightSlidingSide", openAtStartup = true)
@ActionID(category = "Window", id = "org.thespheres.betula.ui.impl.ConfigurationPanelsTopComponent")
@ActionReferences({
    @ActionReference(path = "Toolbars/Settings", position = 400),
    @ActionReference(path = "Shortcuts", name = "D-6"),
    @ActionReference(path = "Menu/Window", position = 2000)})
@TopComponent.OpenActionRegistration(
        displayName = "#ConfigurationPanelsTopComponent.action.displayName",
        preferredID = "ConfigurationPanelsTopComponent")
@NbBundle.Messages({
    "ConfigurationPanelsTopComponent.action.displayName=Eigenschaften und Einstellungen",
    "ConfigurationPanelsTopComponent.displayName=Eigenschaften und Einstellungen",
    "ConfigurationPanelsTopComponent.current.displayName=Einstellungen - {0}",
    "ConfigurationPanelsTopComponent.toolTip=Dies ist ein Einstellungsfenster.",
    "ConfigurationPanelsTopComponent.empty.label=Keine Einstellungen"})
public class ConfigurationPanelsTopComponent extends TopComponent {

    private final Listener listener = new Listener();
    final UndoRedo.Manager undoRedo = new UndoRedo.Manager();
    private final DefaultComboBoxModel selectorModel = new DefaultComboBoxModel();
    private final JXComboBox selectionBox = new JXComboBox();
    private ConfigurationPanel current;
    private final JLabel empty;
    private final ActionListener panelSelectionListener;

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public ConfigurationPanelsTopComponent() {
        selectionBox.setMinimumSize(new Dimension(100, 20));
        selectionBox.setModel(selectorModel);
        selectionBox.setRenderer(new DefaultListRenderer(o -> ((ConfigurationPanel) o).getDisplayName()));
        setLayout(new BorderLayout());
        empty = new JLabel(NbBundle.getMessage(ConfigurationPanelsTopComponent.class, "ConfigurationPanelsTopComponent.empty.label"));
        empty.setHorizontalAlignment(SwingConstants.CENTER);
        empty.setFont(empty.getFont().deriveFont(Font.ITALIC));
        Image icon = ImageUtilities.loadImage("org/thespheres/betula/ui/resources/property-blue.png", true);
        setIcon(icon);
        setEmpty();

        panelSelectionListener = e -> setSelectedPanel((ConfigurationPanel) selectorModel.getSelectedItem());
        selectionBox.addActionListener(panelSelectionListener);
    }

    @Override
    protected void componentHidden() {
        TopComponent.getRegistry().removePropertyChangeListener(listener);
        onChange();
    }

    @Override
    protected void componentShowing() {
        onChange();
        TopComponent.getRegistry().addPropertyChangeListener(listener);
    }

    @Override
    public UndoRedo getUndoRedo() {
        return undoRedo;
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

    private synchronized void onChange() {

        final List<ConfigurationPanel> cp = findCurrent();
        EventQueue.invokeLater(() -> update(cp));
    }

    private List<ConfigurationPanel> findCurrent() {
        final List<ConfigurationPanel> l = new ArrayList<>();
        Arrays.stream(TopComponent.getRegistry().getActivatedNodes())
                .flatMap(n -> n.getLookup().lookupAll(ConfigurationPanel.class).stream())
                .sorted(Comparator.comparingInt(ConfigurationPanel::position))
                .forEach(l::add);
        Arrays.stream(TopComponent.getRegistry().getActivatedNodes())
                .flatMap(n -> n.getLookup().lookupAll(DataObject.class).stream())
                .map(this::panelForContent)
                .filter(Objects::nonNull)
                .forEach(l::add);
        Arrays.stream(TopComponent.getRegistry().getActivatedNodes())
                .flatMap(n -> n.getLookup().lookupAll(ConfigurationPanelLookupHint.class).stream())
                .map(this::panelForContent)
                .filter(Objects::nonNull)
                .forEach(l::add);
//        if (arr == null) {
//            final DataObject singleDO = Arrays.stream(TopComponent.getRegistry().getActivatedNodes())
//                    .flatMap(n -> n.getLookup().lookupAll(DataObject.class).stream())
//                    .collect(CollectionUtil.singleOrNull());
//            if (singleDO != null) {
//                final String mime = singleDO.getPrimaryFile().getMIMEType();
//                final String dn = UIUtilities.findDisplayName(singleDO);
//                return panelForContent(mime, dn);
//            }
//            final ConfigurationPanelLookupHint singleHint = Arrays.stream(TopComponent.getRegistry().getActivatedNodes())
//                    .flatMap(n -> n.getLookup().lookupAll(ConfigurationPanelLookupHint.class).stream())
//                    .collect(CollectionUtil.singleOrNull());
//            if (singleHint != null) {
//                return panelForContent(singleHint.getContentType(), singleHint.getDisplayName());
//            }
//        }
        return l;
    }

    private ConfigPanelImpl panelForContent(final ConfigurationPanelLookupHint hint) {
        ConfigPanelImpl cpi = ConfigPanelImpl.get(hint.getContentType(), this);
        if (cpi != null && !cpi.panels.isEmpty()) {
            cpi.setDisplayName(hint.getDisplayName());
            return cpi;
        }
        return null;
    }

    private ConfigPanelImpl panelForContent(final DataObject d) {
        final String mime = d.getPrimaryFile().getMIMEType();
        final String display = UIUtilities.findDisplayName(d);
        ConfigPanelImpl cpi = ConfigPanelImpl.get(mime, this);
        if (cpi != null && !cpi.panels.isEmpty()) {
            cpi.setDisplayName(display);
            return cpi;
        }
        return null;
    }

    private void update(final List<ConfigurationPanel> selected) {
        assert EventQueue.isDispatchThread();
        selectionBox.removeActionListener(panelSelectionListener);
        selectionBox.removeAllItems();
        selected.forEach(selectorModel::addElement);
        if (selected.size() > 1) {
            selectionBox.setVisible(true);
        } else {
            selectionBox.setVisible(false);
        }
        if (!selected.isEmpty()) {
            setSelectedPanel(selected.get(0));
        } else {
            setEmpty();
        }
        revalidate();
        repaint();
        selectionBox.addActionListener(panelSelectionListener);
    }

    private void setSelectedPanel(ConfigurationPanel c) {
        if (current != null) {
            current.panelDeactivated();
        }
        removeAll();
        current = c;
        if (current != null) {
            add(current.getComponent(), BorderLayout.CENTER);
            setName(NbBundle.getMessage(ConfigurationPanelsTopComponent.class, "ConfigurationPanelsTopComponent.current.displayName", current.getDisplayName()));
            setToolTipText(current.getDisplayHint());
            current.panelActivated(Utilities.actionsGlobalContext());
        }
    }

    private void setEmpty() {
        if (current != null) {
            current.panelDeactivated();
        }
        removeAll();
        add(empty, BorderLayout.CENTER);
        setName(NbBundle.getMessage(ConfigurationPanelsTopComponent.class, "ConfigurationPanelsTopComponent.displayName"));
        setToolTipText(NbBundle.getMessage(ConfigurationPanelsTopComponent.class, "ConfigurationPanelsTopComponent.toolTip"));
    }

    private final class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case TopComponent.Registry.PROP_ACTIVATED_NODES:
                    onChange();
                    break;
                case TopComponent.Registry.PROP_TC_CLOSED:
                    TopComponent closed = (TopComponent) evt.getNewValue();
                    if (closed != null && current != null) {
//                        boolean changed = closed.getLookup().lookupAll(MarkerSetDecorated.class).stream()
//                                .anyMatch(msp -> Arrays.stream(current).anyMatch(c -> c == msp));
//                        if (changed) {
//                            setCurrent(null);
//                        }
                    }
                    break;
            }
        }

    }

}
