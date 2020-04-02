/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.local.timetbl;

import java.net.MalformedURLException;
import org.netbeans.spi.project.LookupProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;
import org.thespheres.betula.project.BetulaProject;
import org.thespheres.betula.services.LocalFileProperties;

/**
 *
 * @author boris.heithecker
 */
@LookupProvider.Registration(projectType = {"org-thespheres-betula-project-local"})
public class TimetableCalendarRegistration implements LookupProvider {

    public static final String LOCAL_TIMETABLE_FILE = "local-timetable.ics";

    @Override
    public Lookup createAdditionalLookup(Lookup baseContext) {
        LocalFileProperties prop = baseContext.lookup(LocalFileProperties.class);
        BetulaProject project = baseContext.lookup(BetulaProject.class);
        if (project != null && prop.getProperty("providerURL") == null) {
            FileObject f = null;
            try {
                f = URLMapper.findFileObject(project.getConfigurationsPath().toURL());
            } catch (MalformedURLException ex) {
            }
            if (f != null) {
                final FileObject parent = f;
                class ReturnLookup extends ProxyLookup {

                    private void setContent(Lookup content) {
                        setLookups(content);
                    }
                }
                ReturnLookup ret = new ReturnLookup();
                class Listener extends FileChangeAdapter {

                    @Override
                    public void fileRenamed(FileRenameEvent fe) {
                        update();
                    }

                    @Override
                    public void fileDataCreated(FileEvent fe) {
                        update();
                    }

                    boolean update() {
                        FileObject fo = parent.getFileObject(LOCAL_TIMETABLE_FILE);
                        if (fo != null) {
                            try {
                                Lookup lookup = DataObject.find(fo).getLookup();
                                ret.setContent(lookup);
                                parent.removeFileChangeListener(this);
                                return true;
                            } catch (DataObjectNotFoundException ex) {
                            }
                        }
                        return false;
                    }

                }
                Listener l = new Listener();
                if (!l.update()) {
                    f.addFileChangeListener(l);
                }
                return ret;
            }
        }
        return Lookup.EMPTY;
    }
}
