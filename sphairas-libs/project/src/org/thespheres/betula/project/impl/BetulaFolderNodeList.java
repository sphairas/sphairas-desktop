/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.impl;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;

/**
 *
 * @author boris.heithecker
 */
class BetulaFolderNodeList implements NodeList<DataObject>, PropertyChangeListener { //, FileChangeListener extends FileChangeAdapter 

    private final ChangeSupport cSupport = new ChangeSupport(this);
    private final Map<DataObject, Node> dirs = new HashMap<>();
    private final List<DataObject> keys = new ArrayList<>();
    private final FileObject rootDirectory;
    private final RequestProcessor RP = new RequestProcessor(BetulaFolderNodeList.class);
    private final DataFolder data;

    private BetulaFolderNodeList(FileObject pDir) {
        this.rootDirectory = pDir;
        data = DataFolder.findFolder(rootDirectory);
    }

    @Override
    public List<DataObject> keys() {
        return keys;
    }

    private void init(boolean expected) {
        keys.clear();
        dirs.clear();
        rootDirectory.refresh(expected);
        for (DataObject dfo : NbCollections.iterable(data.children(false))) {
            if (initFileObject(dfo)) {
                keys.add(dfo);
            }
        }
        EventQueue.invokeLater(cSupport::fireChange);
    }

    private boolean initFileObject(DataObject dob) {
        if (!Util.isHidden(dob.getPrimaryFile())) {
            AnnotatedDataNode adn = new AnnotatedDataNode(dob);
            dirs.put(dob, adn);
            return true;
        }
        return false;
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
        return dirs.get(key);
    }

    @Override
    public void addNotify() {
        RP.post(() -> init(false));
//        rootDirectory.addFileChangeListener(this);
        data.addPropertyChangeListener(this);
    }

    @Override
    public void removeNotify() {
//        rootDirectory.removeFileChangeListener(this);
        data.removePropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        change();
    }

//    @Override
//    public void fileFolderCreated(FileEvent fe) {
//        fe.runWhenDeliveryOver(this::change);
//    }
//    
//    @Override
//    public void fileDataCreated(FileEvent fe) {
//        fe.runWhenDeliveryOver(this::change);
//    }
//    
//    @Override
//    public void fileDeleted(FileEvent fe) {
//        fe.runWhenDeliveryOver(this::change);
//    }
    private void change() {
        RP.post(() -> init(true));
    }

    @NodeFactory.Registration(projectType = "org-thespheres-betula-project-local", position = 1000000)
    public static class BetulaFolderNodeFactory implements NodeFactory {

        @Override
        public NodeList<?> createNodes(Project p) {
            return new BetulaFolderNodeList(p.getProjectDirectory());
        }

    }
}
