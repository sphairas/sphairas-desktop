/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.project;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.LookupProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ProxyLookup;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;

/**
 *
 * @author boris.heithecker
 */
@LookupProvider.Registration(projectType = {"org-thespheres-betula-project-local"})
public class UnitOpenSupportProjectLkpRegistration implements LookupProvider {

    static final RequestProcessor RP = new RequestProcessor(UnitOpenSupportProjectLkpRegistration.class);

    @Override
    public Lookup createAdditionalLookup(Lookup baseContext) {
        ProjectInformation pi = baseContext.lookup(ProjectInformation.class);
        return new UOSLookup(pi.getProject().getProjectDirectory());
    }

    private final static class UOSLookup extends ProxyLookup implements ChangeListener {

        private final FileObject project;

        @SuppressWarnings({"LeakingThisInConstructor"})
        private UOSLookup(FileObject dir) {
            this.project = dir;
            PrimaryUnitOpenSupport.Registry.get().addChangeListener(this);
            stateChanged(null);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            final Lookup[] lkp = PrimaryUnitOpenSupport.Registry.get().getRegistered().stream()
                    .filter(uos -> uos.getProjectDirectory().equals(project))
                    .map(PrimaryUnitOpenSupport::getLookup)
                    .toArray(Lookup[]::new);
            setLookups(lkp);
        }

    }
}
