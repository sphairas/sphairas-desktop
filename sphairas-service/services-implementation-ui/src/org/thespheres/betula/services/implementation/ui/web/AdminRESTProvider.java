/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.web;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.implementation.ui.impl.SyncedProviderInstance;
import org.thespheres.betula.services.ui.util.AppProperties;
import org.thespheres.betula.services.ui.web.BetulaWebServiceProvider;
import org.thespheres.betula.services.ui.web.SSLUtil;
import org.thespheres.betula.services.ws.BetulaWebService;
import org.thespheres.betula.services.ws.WebServiceProvider;

/**
 *
 * @author boris.heithecker@gmx.net
 */
public class AdminRESTProvider implements WebServiceProvider, WebProvider.SSL {

    public static final int DEFAULT_PORT = 8181;
    public static final String PORT_PROP = "port";
    protected final RequestProcessor RP = new RequestProcessor(BetulaWebServiceProvider.class.getCanonicalName(), 1, true);
    private final ProviderInfo provider;
    private final URI baseURI;
    protected final String certName;
    protected SSLContext ssl;

    private AdminRESTProvider(final String provider, final URI baseURI, final String certAlias) {
        if (certAlias == null || certAlias.isEmpty()) {
            throw new IllegalStateException();
        }
        this.provider = ProviderRegistry.getDefault().get(provider);
        this.baseURI = baseURI;
        this.certName = certAlias;
    }

    public static AdminRESTProvider create(final String provider, final LocalProperties attr) {
        String endpoint = attr.getProperty("betula.service.endpoint");
        URI base;
        if (endpoint != null) {
            base = URI.create(endpoint);
        } else {
            final int port = Optional.ofNullable(attr.getProperty(PORT_PROP))
                    .map(Integer::parseInt)
                    .orElse(DEFAULT_PORT);
            base = createBaseURI(attr.getProperty(SyncedProviderInstance.HOST_PROP), port);
        }
        final String certAlias = AppProperties.privateKeyAlias(attr, provider);
        return new AdminRESTProvider(provider, base, certAlias);
    }

    public static URI createBaseURI(final String host, final int port) {
//        return URI.create("https://" + host + ":" + Integer.toString(port) + "/admins/service/api/units/");
        return URI.create("https://" + host + ":" + Integer.toString(port) + "/service/api/units/");
    }

    @Override
    public BetulaWebService createServicePort() throws IOException {
        return new BetulaRESTServicePort(this, baseURI);
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        if (this.baseURI != null) {
            final HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            return hostnameVerifier;
        }
        return SSL.super.getHostnameVerifier();
    }

    @Override
    public ProviderInfo getInfo() {
        return provider;
    }

    @Override
    public RequestProcessor getDefaultRequestProcessor() {
        return RP;
    }

    @Override
    public synchronized SSLContext getSSLContext() {
        if (ssl == null) {
            ssl = SSLUtil.createSSLContext(certName);
        }
        return ssl;
    }

}
