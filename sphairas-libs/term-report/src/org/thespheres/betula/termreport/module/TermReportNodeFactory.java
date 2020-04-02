/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.module;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;

/**
 *
 * @author boris.heithecker
 */
@NodeFactory.Registration(projectType = "org-thespheres-betula-project-local", position = 1500)
public class TermReportNodeFactory implements NodeFactory {

    @Override
    public NodeList<?> createNodes(Project p) {
        return new TermReportNodeList(p.getProjectDirectory());
    }

    private static class TermReportNodeList implements NodeList<DataObject>, PropertyChangeListener {

        private final ChangeSupport cSupport = new ChangeSupport(this);
        private final DataFolder folder;
        private final List<DataObject> keys = new ArrayList<>();

        private TermReportNodeList(FileObject pDir) {
            folder = DataFolder.findFolder(pDir);
//            updateKeys();
        }

        @Override
        public List<DataObject> keys() {
            synchronized (keys) {
                return keys;
            }
        }

        private void updateKeys() {
            synchronized (keys) {
                keys.clear();
                if (folder.isValid()) {
                    Arrays.stream(folder.getChildren())
                            .filter(dob -> dob.getPrimaryFile().getMIMEType().equals(TermReportDataObject.TERMREPORT_MIME))
                            .forEach(keys::add);
                }
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
            return key.getNodeDelegate();
        }

        @Override
        public void addNotify() {
            folder.addPropertyChangeListener(this);
        }

        @Override
        public void removeNotify() {
            folder.removePropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateKeys();
            Mutex.EVENT.postWriteRequest(cSupport::fireChange);
        }
    }
}
