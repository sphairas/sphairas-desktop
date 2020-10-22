/*
 * To change this license header, choose License Headers in Project AppProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.web;

import java.util.Optional;
import javax.net.ssl.HostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.implementation.ui.impl.SyncedProviderInstance;
import org.thespheres.betula.services.ui.util.AppProperties;
import org.thespheres.betula.services.ui.web.BetulaWebServiceProvider;

/**
 *
 * @author boris.heithecker
 */
public class SSLWebServiceProvider extends BetulaWebServiceProvider implements WebProvider.SSL {

    public static final int DEFAULT_PORT = 8181;
    public static final String PORT_PROP = "port";
    private final ProviderInfo provider;
    private final String hostname;

    private SSLWebServiceProvider(final String provider, final String endpoint, final String certAlias, final String hostname) {
        super(endpoint, certAlias);
        this.provider = ProviderRegistry.getDefault().get(provider);
        this.hostname = hostname;
    }

    public static SSLWebServiceProvider create(final String provider, final LocalProperties attr) {
        String endpoint = attr.getProperty("betula.service.endpoint");
        if (endpoint == null) {
            final int port = Optional.ofNullable(attr.getProperty(PORT_PROP))
                    .map(Integer::parseInt)
                    .orElse(DEFAULT_PORT);
            endpoint = createEndpointUrl(attr.getProperty(SyncedProviderInstance.HOST_PROP), port);
        }
        final String certAlias = AppProperties.privateKeyAlias(attr, provider);
        final String hostname = attr.getProperty("host-common-name");
        return new SSLWebServiceProvider(provider, endpoint, certAlias, hostname);
    }

    public static String createEndpointUrl(final String host, final int port) {
        return "https://" + host + ":" + Integer.toString(port) + "/service/betulaws-admin";
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        if (this.hostname != null) {
            final HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            return hostnameVerifier;
        }
        return SSL.super.getHostnameVerifier();
    }

    @Override
    public ProviderInfo getInfo() {
        return provider;
    }

}
