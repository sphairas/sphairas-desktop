/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.ws;

import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.services.LocalFileProperties;

/**
 *
 * @author boris.heithecker
 */
@LookupProvider.Registration(projectType = {"org-thespheres-betula-project-local"})
public class UnitLkpRegistration implements LookupProvider {

    @Override
    public Lookup createAdditionalLookup(Lookup baseContext) {
        LocalFileProperties prop = baseContext.lookup(LocalFileProperties.class);
        String id = prop.getProperty("unitId");//legacy
        id = prop.getProperty("unit.id", id);
        String authority = prop.getProperty("unitAuthority");//legacy
        authority = prop.getProperty("unit.authority", authority);
        if (id != null && authority != null) {
            UnitId uid = new UnitId(authority, id);
            return Lookups.singleton(new UnitImp2(uid, baseContext));
        } else {
            return Lookup.EMPTY;
        }
    }
}
