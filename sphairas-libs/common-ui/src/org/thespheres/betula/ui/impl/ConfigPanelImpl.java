/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEditSupport;
import org.jdesktop.swingx.VerticalLayout;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.ui.ConfigurationPanel;
import org.thespheres.betula.ui.ConfigurationPanelComponent;
import org.thespheres.betula.ui.ConfigurationPanelComponentProvider;

/**
 *
 * @author boris.heithecker
 */
class ConfigPanelImpl implements ConfigurationPanel {

    private final static Map<String, ConfigPanelImpl> MAP = new HashMap<>();
    private final String contentTypeOrMime;
    private final JPanel pane = new JPanel();
    final List<ConfigurationPanelComponent> panels = new ArrayList<>(10);
    protected final UndoableEditSupport undoSupport = new UndoableEditSupport(this);
    private String displayName;
    private final ConfigurationPanelsTopComponent parent;

    ConfigPanelImpl(String content, ConfigurationPanelsTopComponent tc) {
        this.contentTypeOrMime = content;
        this.displayName = content;
        this.parent = tc;
        initialize();
    }

    static ConfigPanelImpl get(final String mime, final ConfigurationPanelsTopComponent tc) {
        synchronized (MAP) {
            return MAP.computeIfAbsent(mime, m -> new ConfigPanelImpl(m, tc));
        }
    }

    private void initialize() {
        pane.setLayout(new VerticalLayout());
//        MimePath.validate(mime)
        Lookup l;
        try {
            l = MimeLookup.getLookup(MimePath.parse(contentTypeOrMime));
        } catch (IllegalArgumentException e) {
            l = Lookups.forPath("ConfigurationPanelComponent/" + contentTypeOrMime);
        }
        l.lookupAll(ConfigurationPanelComponentProvider.class).stream()
                .map(ConfigurationPanelComponentProvider::createConfigurationPanelComponent)
                .filter(Objects::nonNull)
                //                .map(ConfigurationPanelComponentContainer::new)
                .forEach(panels::add);
        panels.forEach(pane::add);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDisplayHint() {
        return getDisplayName();
    }

    @Override
    public JComponent getComponent() {
        return pane;
    }

    @Override
    public void panelActivated(Lookup context) {
        panels.forEach(p -> {
            p.panelActivated(context);
            p.addUndoableEditListener(parent.undoRedo);
        });
    }

    @Override
    public void panelDeactivated() {
        panels.forEach(p -> {
            p.removeUndoableEditListener(parent.undoRedo);
            p.panelDeactivated();
        });
    }

    @Override
    public Lookup getLookup() {
        return null;
    }

    public UndoableEditSupport getUndoSupport() {
        return undoSupport;
    }

    public synchronized void addUndoableEditListener(UndoableEditListener l) {
        undoSupport.addUndoableEditListener(l);
    }

    public synchronized void removeUndoableEditListener(UndoableEditListener l) {
        undoSupport.removeUndoableEditListener(l);
    }

}
