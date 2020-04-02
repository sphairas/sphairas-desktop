/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.Unit;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.ui.PrimaryUnitNode;
import org.thespheres.betula.services.LocalProperties;

/**
 *
 * @author boris.heithecker
 */
public class UnitNodeList implements NodeList<UnitId>, LookupListener {

    private final ChangeSupport cSupport = new ChangeSupport(this);
    private final TreeMap<UnitId, PrimaryUnitOpenSupport> units = new TreeMap<>((o1, o2) -> o1.getId().compareTo(o2.getId()));
    private final Lookup.Result<Unit> lkp;
    private final FileObject projectDir;
    private final RequestProcessor PROC = new RequestProcessor(PrimaryUnitNode.class.getCanonicalName());

    private UnitNodeList(Project project) {
        this.projectDir = project.getProjectDirectory();
        lkp = project.getLookup().lookupResult(Unit.class);
    }

    @Override
    public List<UnitId> keys() {
        synchronized (units) {
            return new ArrayList<>(units.keySet());
        }
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        cSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        cSupport.removeChangeListener(l);
    }

    @Override
    public Node node(UnitId key) {
        synchronized (units) {
            return Optional.ofNullable(units.get(key))
                    .map(PrimaryUnitOpenSupport::getNodeDelegate)
                    .orElse(null);
        }
    }

    @Override
    public void addNotify() {
        lkp.addLookupListener(this);
        postInit();
    }

    @Override
    public void removeNotify() {
        lkp.removeLookupListener(this);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        postInit();
    }

    private void postInit() {
        PROC.post(this::init, 0, Thread.NORM_PRIORITY);
    }

    private void init() {
        final Map<UnitId, PrimaryUnitOpenSupport> m = lkp.allInstances().stream()
                .map(Unit::getUnitId)
                .collect(Collectors.toMap(u -> u, u -> PrimaryUnitOpenSupport.Registry.get().find(projectDir, u)));
        synchronized (units) {
            units.clear();
            units.putAll(m);
        }
        cSupport.fireChange();
    }

    @NodeFactory.Registration(projectType = "org-thespheres-betula-project-local", position = 5000)
    public static class UnitNodeFactory implements NodeFactory {

        @Override
        public NodeList<UnitId> createNodes(Project p) {
            LocalProperties bp = p.getLookup().lookup(LocalProperties.class);
            if (bp != null && bp.getProperty("project.type", "").equals("org.thespheres.betula.admin")) {
                return new UnitNodeList(p);
            }
            return (NodeList<UnitId>) NodeFactorySupport.fixedNodeList();
        }
    }
}
