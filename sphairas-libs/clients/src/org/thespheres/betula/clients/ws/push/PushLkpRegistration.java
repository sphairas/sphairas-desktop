/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.ws.push;

import java.io.IOException;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.services.LocalFileProperties;

/**
 *
 * @author boris.heithecker
 */
@LookupProvider.Registration(projectType = {"org-thespheres-betula-project-local"})
public class PushLkpRegistration implements LookupProvider {

    @Override
    public Lookup createAdditionalLookup(Lookup baseContext) {
        final LocalFileProperties prop = baseContext.lookup(LocalFileProperties.class);
        final String prov = prop.getProperty("providerURL");
        final String urlProp = prop.getProperty("documentsPushUrl");
        if (prov != null && urlProp != null) {
            try {
                final PushNotificationServiceImpl service = Atmosphere.findPushNotificationService(prov, urlProp);
                if (service != null) {
                    return Lookups.singleton(service);
                }
            } catch (IOException ex) {
            }
        }
        return Lookup.EMPTY;
    }
}
