/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.project;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.thespheres.betula.admin.units.UnitOpenSupport;

/**
 *
 * @author boris.heithecker
 */
class RemoteUnitNodeList implements NodeList<DataObject> {

    private final ChangeSupport cSupport = new ChangeSupport(this);
    private final TreeMap<DataObject, UnitOpenSupport> files = new TreeMap<>(new PositionComparator());
    private final FileObject projectDir;
    private final Listener listener = new Listener();

    RemoteUnitNodeList(Project project) {
        this.projectDir = project.getProjectDirectory();
    }

    @Override
    public List<DataObject> keys() {
        synchronized (files) {
            return new ArrayList<>(files.keySet());
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
    public Node node(DataObject key) {
        synchronized (files) {
            return Optional.ofNullable(files.get(key))
                    .map(UnitOpenSupport::getNodeDelegate)
                    .orElse(null);
        }
    }

    @Override
    public void addNotify() {
        projectDir.addRecursiveListener(listener);
        init();
    }

    @Override
    public void removeNotify() {
        projectDir.removeRecursiveListener(listener);
    }

    private synchronized void init() {
        final Map<DataObject, UnitOpenSupport> map = new HashMap<>();
        files.forEach((d, m) -> {
            if (d.isValid()) {
                map.put(d, m);
            }
        });
        Enumeration<? extends FileObject> fo = projectDir.getChildren(true);
        while (fo.hasMoreElements()) {
            FileObject n = fo.nextElement();
//            if (n.getMIMEType().equals(RemoteUnitDescriptorDataObject.MIME_TYPE)) {
            try {
                DataObject dob = DataObject.find(n);
                final UnitOpenSupport rpn = dob.getLookup().lookup(UnitOpenSupport.class);
                if (rpn == null) {
                    continue;
                }
                map.computeIfAbsent(dob, d -> rpn);
            } catch (DataObjectNotFoundException ex) {
                Logger.getLogger(getClass().getCanonicalName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
//            }
        }
        synchronized (files) {
            files.clear();
            files.putAll(map);
        }
        cSupport.fireChange();
    }

    private static class PositionComparator implements Comparator<DataObject> {

        @Override
        public int compare(DataObject d1, DataObject d2) {
            int pos1 = position(d1);
            int pos2 = position(d2);
            if (pos1 == pos2) {
                return d1.getPrimaryFile().getName().compareTo(d2.getPrimaryFile().getName());
            } else {
                return pos1 - pos2;
            }
        }

        private int position(DataObject data) {
            Object o1 = data.getPrimaryFile().getAttribute("position");
            return o1 != null ? (Integer) o1 : 0;
        }

    }

    private class Listener extends FileChangeAdapter {

        @Override
        public void fileDeleted(FileEvent fe) {
            init();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            init();
        }

    }
}
