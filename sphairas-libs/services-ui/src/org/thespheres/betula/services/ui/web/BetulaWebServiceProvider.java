/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.web;

import java.io.IOException;
import javax.net.ssl.SSLContext;
import javax.xml.ws.WebServiceException;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.web.ContextCredentials;
import org.thespheres.betula.services.ws.BetulaWebService;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.services.ws.api.BetulaServiceClient;

/**
 *
 * @author boris.heithecker
 */
public abstract class BetulaWebServiceProvider implements WebServiceProvider {

    protected final BetulaServiceClient client;
    protected final RequestProcessor RP = new RequestProcessor(BetulaWebServiceProvider.class.getCanonicalName(), 1, true);
    protected SSLContext ssl;
    protected final ContextCredentialsSupport creds;
    protected final String certName;

    protected BetulaWebServiceProvider(String endpoint) {
        this(endpoint, null, null, null);
    }

    protected BetulaWebServiceProvider(String endpoint, String provider, String usernameKey, String passwordKey) {
        if (StringUtils.isEmpty(usernameKey) || StringUtils.isEmpty(passwordKey) || StringUtils.isEmpty(provider)) {
            throw new IllegalStateException();
        }
        client = new BetulaServiceClient(endpoint, this instanceof WebProvider.SSL ? (WebProvider.SSL) this : null);
        creds = ContextCredentialsSupport.createContextCredentialsSupport(provider, usernameKey, passwordKey);
        this.certName = null;
    }

    protected BetulaWebServiceProvider(String endpoint, String certName) {
        if (certName == null || certName.isEmpty()) {
            throw new IllegalStateException();
        }
        client = new BetulaServiceClient(endpoint, this instanceof WebProvider.SSL ? (WebProvider.SSL) this : null);
        creds = null;
        this.certName = certName;
    }

    @Override
    public BetulaWebService createServicePort() throws IOException {
        try {
//            if (certName != null) {
//                PrivateKeyCallback.setCurrent(callback);
//            }
            if (this instanceof ContextCredentials.Provider) {
                return client.getBetulaServicePort(((ContextCredentials.Provider) this).getContextCredentials());
            } else {
                return client.getBetulaServicePort();
            }
        } catch (WebServiceException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public RequestProcessor getDefaultRequestProcessor() {
        return RP;
    }

    public ContextCredentials getContextCredentials() {
        return creds != null ? creds.getContextCredentials() : null;
    }

    //this replaces System.setProperty("com.sun.enterprise.security.httpsOutboundKeyAlias", "ts1as");
    public synchronized SSLContext getSSLContext() {
        if (ssl == null) {
            ssl = SSLUtil.createSSLContext(certName);
        }
        return ssl;
    }

}
