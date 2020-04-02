/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.util;

import java.util.logging.Level;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
public class JaxRSUtil {

    public static WebTarget create(final String provider) {
        final WebProvider wp = WebProvider.find(provider, WebProvider.class);
        String base = URLs.adminBase(LocalProperties.find(provider));
        String url = base + "messenger/resource";

        final Client client = ClientBuilder.newBuilder()
                .register(MoxyJsonFeature.class)
                .register(JAXBJSONContextResolver.class)
                .sslContext(((WebProvider.SSL) wp).getSSLContext())
//                .hostnameVerifier(arg0)
                .build();
        return client.target(url);
    }

    @Messages({"JaxRSUtil.checkResponse.message=Provider {0} has returned {1} ({2})"})
    public static boolean checkResponse(final ProviderInfo provider, final Response resp) {
        final Response.StatusType statusInfo = resp.getStatusInfo();
        if (statusInfo.getStatusCode() != Response.Status.OK.getStatusCode()) {
            String msg2 = NbBundle.getMessage(JaxRSUtil.class, "JaxRSUtil.checkResponse.message", provider, statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
            PlatformUtil.getCodeNameBaseLogger(JaxRSUtil.class).log(Level.SEVERE, msg2);
            Util.notify(null, provider);
            return false;
        }
        return true;
    }
}
