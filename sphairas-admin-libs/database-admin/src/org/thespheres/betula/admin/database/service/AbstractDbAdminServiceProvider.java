/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.database.DbAdminService;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.web.SSLUtil;

/**
 *
 * @author boris.heithecker@gmx.net
 */
public abstract class AbstractDbAdminServiceProvider implements WebProvider.SSL {

    protected final RequestProcessor RP = new RequestProcessor(DbAdminServiceProvider.class.getCanonicalName(), 1, true);
    protected SSLContext ssl;
    private final ProviderInfo provider;
    protected final String certName;

    public AbstractDbAdminServiceProvider(ProviderInfo provider, String certName) {
        if (certName == null || certName.isEmpty()) {
            throw new IllegalStateException();
        }
        this.provider = provider;
        this.certName = certName;
    }

    public static List<ProviderInfo> findAllProviders() {
        return Lookups.forPath("Provider").lookupAll(ProviderInfo.class).stream().map(ProviderInfo.class::cast).collect(Collectors.toList());
    }

    public abstract DbAdminService createDbAdminServicePort() throws IOException;

    @Override
    public ProviderInfo getInfo() {
        return provider;
    }

    @Override
    public RequestProcessor getDefaultRequestProcessor() {
        return RP;
    }

    //this replaces System.setProperty("com.sun.enterprise.security.httpsOutboundKeyAlias", "ts1as");
    @Override
    public synchronized SSLContext getSSLContext() {
        if (ssl == null) {
            ssl = SSLUtil.createSSLContext(certName);
        }
        return ssl;
    }

}
