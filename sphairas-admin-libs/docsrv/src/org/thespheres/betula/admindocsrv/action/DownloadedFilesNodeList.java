/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admindocsrv.action;

import java.io.IOException;
import java.nio.file.Path;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.thespheres.betula.admindocsrv.DownloadTargetFolders;

/**
 *
 * @author boris.heithecker
 */
public class DownloadedFilesNodeList implements NodeList<DataObject>, ChangeListener {

    private final String projectPath;
    private final ChangeSupport cSupport = new ChangeSupport(this);

    DownloadedFilesNodeList(final Project p) {
        projectPath = p.getProjectDirectory().getPath();
    }

    @Override
    public List<DataObject> keys() {
        final List<DataObject> ret = new ArrayList<>();
        try {
            final List<Path> l = DownloadTargetFolders.getDefault().forOwner(projectPath);
            for (final Path p : l) {
                final FileObject fo = FileUtil.toFileObject(p.toFile());
                if (fo != null) {
                    try {
                        ret.add(DataObject.find(fo));
                    } catch (DataObjectNotFoundException ex) {
                    }
                }
            }
        } catch (IOException ioex) {
        }
        Collections.sort(ret, Comparator.comparing(d -> d.getPrimaryFile().getNameExt(), Collator.getInstance(Locale.getDefault())));
        return ret;
    }

    @Override
    public Node node(final DataObject key) {
        return new FilterNode(key.getNodeDelegate());
    }

    @Override
    public void addNotify() {
        DownloadTargetFolders.getDefault().addChangeListener(this);
    }

    @Override
    public void removeNotify() {
        DownloadTargetFolders.getDefault().removeChangeListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        cSupport.fireChange();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        cSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        cSupport.removeChangeListener(l);
    }

    @NodeFactory.Registration(projectType = "org-thespheres-betula-project-local", position = 20000)
    public static class RemoteUnitDescriptorUnitNodeFactory implements NodeFactory {

        @Override
        public NodeList<DataObject> createNodes(Project p) {
            return new DownloadedFilesNodeList(p);
        }
    }
}
