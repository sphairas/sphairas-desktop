/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.local.timetbl;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.project.BetulaProject;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.scheme.spi.ClassSchedule;
import org.thespheres.betula.services.scheme.spi.Scheme;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;

/*
 *@author boris.heithecker
 */
//@ServiceProvider(service = SchemeProvider.class)
class LocalTimetableScheduleProvider implements SchemeProvider {

    private final LocalClassScheduleHolder holder;

    private LocalTimetableScheduleProvider(URI config) {
        this.holder = LocalClassScheduleHolder.get(config);
    }

    @Override
    public ProviderInfo getInfo() {
        return null;
    }

    @Override
    public <G extends Scheme> G[] getAllSchemes(Class<G> type) {
        G ret = null;
        if (type.isAssignableFrom(ClassSchedule.class)) {
            try {
                ret = (G) holder.getLocalClassSchedule();
            } catch (IOException ioex) {
                throw new IllegalStateException(ioex);
            }
        }
        if (ret == null) {
            return (G[]) new Scheme[]{};
        }
        G[] rr = (G[]) Array.newInstance(type, 1);
        rr[0] = ret;
        return rr;
    }

    @Override
    public <G extends Scheme> G getScheme(String id, Class<G> type) {
        if (id == null || id.isEmpty()) {
            id = Scheme.DEFAULT_SCHEME;
        }
        if (Scheme.DEFAULT_SCHEME.equals(id) && type.isAssignableFrom(ClassSchedule.class)) {
            try {
                return type.cast(holder.getLocalClassSchedule());
            } catch (ClassCastException | IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return null;
    }

    @LookupProvider.Registration(projectType = {"org-thespheres-betula-project-local"})
    public static class LkpRegistration implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup baseContext) {
            LocalFileProperties prop = baseContext.lookup(LocalFileProperties.class);
            BetulaProject prj = baseContext.lookup(BetulaProject.class);
            if (prj != null && prop.getProperty("providerURL") == null) {
                final URI config = prj.getConfigurationsPath();
                return Lookups.singleton(new LocalTimetableScheduleProvider(config));
            }
            return Lookup.EMPTY;
        }
    }
}
