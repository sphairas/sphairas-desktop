/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project;

import org.thespheres.betula.services.LocalFileProperties;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.services.ws.WebServiceProvider;

/**
 *
 * @author boris.heithecker
 */
@LookupProvider.Registration(projectType = {"org-thespheres-betula-project-local"})
public class BetulaServiceProjectRegistration implements LookupProvider {

    @Override
    public Lookup createAdditionalLookup(Lookup baseContext) {
        String url = baseContext.lookup(LocalFileProperties.class).getProperty("providerURL");
        if (url != null) {
            for (WebServiceProvider provider : Lookup.getDefault().lookupAll(WebServiceProvider.class)) {
                if (provider.getInfo().getURL().equals(url)) {
                    return Lookups.singleton(provider);
                }
            }
        }
        return Lookup.EMPTY; //.singleton(new LocalBetulaServiceImpl());
    }
}
