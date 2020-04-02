/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services;

import java.util.Objects;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public interface WebProvider {

    public RequestProcessor getDefaultRequestProcessor();

    public ProviderInfo getInfo();

    public static <W extends WebProvider> W find(final String url, final Class<W> subType) throws NoProviderException {
        final W found = Lookup.getDefault().lookupAll(subType).stream()
                .map(subType::cast)
                .filter(wp -> wp.getInfo().getURL().equals(url))
                .collect(CollectionUtil.singleton())
                .orElse(null);
        if (found != null) {
            return found;
        }
        return Lookup.getDefault().lookupAll(WebProviders.class).stream()
                .map(wps -> wps.find(url, subType))
                .filter(Objects::nonNull)
                .collect(CollectionUtil.singleton())
                .orElseThrow(() -> new NoProviderException(subType, url));
    }

    public interface SSL extends WebProvider {

        public SSLContext getSSLContext();

        public default HostnameVerifier getHostnameVerifier() {
            return null;
        }
    }

    public static interface WebProviders {

        public <W extends WebProvider> W find(final String url, final Class<W> subType);
    }
}
