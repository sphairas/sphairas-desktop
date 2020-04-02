/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.icalendar.local;

import java.net.MalformedURLException;
import org.netbeans.spi.project.LookupProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.thespheres.betula.project.BetulaProject;

/**
 *
 * @author boris.heithecker
 */
@LookupProvider.Registration(projectType = {"org-thespheres-betula-project-local"})
public class ProjectLocalCalendarRegistration implements LookupProvider {

    @Override
    public Lookup createAdditionalLookup(Lookup baseContext) {
        BetulaProject project = baseContext.lookup(BetulaProject.class);
        if (project != null) {
            FileObject f = null;
            try {
                f = URLMapper.findFileObject(project.getConfigurationsPath().toURL());
            } catch (MalformedURLException ex) {
            }
            if (f != null) {
                FileObject fo = f.getFileObject("local.ics");
                if (fo != null) {
                    try {
                        return DataObject.find(fo).getLookup();
                    } catch (DataObjectNotFoundException ex) {
                    }
                }
            }
        }
        return Lookup.EMPTY;
    }
}
