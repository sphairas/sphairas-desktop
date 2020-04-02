/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.project;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.*;
import org.openide.windows.OnShowing;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
@OnShowing
public class ProjectsSupport implements Runnable {

    @Override
    public void run() {
        final Path base = projectsBaseFolder();
        if (base == null || !Files.exists(base) || !Files.isDirectory(base)) {
            return;
        }
        final FileObject folder = FileUtil.toFileObject(base.toFile());
        final List<Project> toOpen = new ArrayList<>();
        if (folder != null && folder.isFolder()) {
            final Enumeration<? extends FileObject> e = folder.getFolders(false);
            while (e.hasMoreElements()) {
                final FileObject p = e.nextElement();
                try {
                    final Project project = ProjectManager.getDefault().findProject(p);
                    if (project != null) {
                        toOpen.add(project);
                    }
                } catch (Exception ex) {
                    PlatformUtil.getCodeNameBaseLogger(getClass()).log(Level.WARNING, null, ex);
                }
            }
        }
        if (!toOpen.isEmpty()) {
            Mutex.EVENT.writeAccess(() -> openProjects(toOpen));
        }
    }

    private void openProjects(List<Project> toOpen) {
        //WindowManager.getDefault().findTopComponent("ExplorerViewTopComponent");
        TopComponent tc = WindowManager.getDefault().findTopComponent("projectTabLogical_tc");
        tc.open();
        OpenProjects.getDefault().open(toOpen.toArray(new Project[toOpen.size()]), true, true);
    }

    public static Path projectsBaseFolder() {
        final String nbuser = System.getProperty("netbeans.user");
        if (nbuser != null) {
            return Paths.get(nbuser).resolve("Projects");
        }
        throw new IllegalStateException("netbeans.user not found.");
    }

}
