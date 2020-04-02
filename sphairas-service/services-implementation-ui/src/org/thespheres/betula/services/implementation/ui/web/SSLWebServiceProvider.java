/*
 * To change this license header, choose License Headers in Project AppProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.web;

import javax.net.ssl.HostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.util.AppProperties;
import org.thespheres.betula.services.ui.web.BetulaWebServiceProvider;

/**
 *
 * @author boris.heithecker
 */
public class SSLWebServiceProvider extends BetulaWebServiceProvider implements WebProvider.SSL {

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
            endpoint = createEndpointUrl(attr.getProperty("host"));
        }
        final String certAlias = AppProperties.privateKeyAlias(attr, provider);
        final String hostname = attr.getProperty("host-common-name");
        return new SSLWebServiceProvider(provider, endpoint, certAlias, hostname);
    }

    public static String createEndpointUrl(final String host) {
        return "https://" + host + ":8181/service/betulaws-admin";
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
