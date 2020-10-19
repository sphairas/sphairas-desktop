/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import javax.xml.ws.WebServiceException;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.admin.database.service.DbAdminService;
import org.thespheres.betula.admin.database.service.DbAdminServiceClient;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.util.AppProperties;
import org.thespheres.betula.services.ui.web.SSLUtil;

/**
 *
 * @author boris.heithecker
 */
public abstract class DbAdminServiceProvider implements WebProvider.SSL {

    protected final DbAdminServiceClient client;
    protected final RequestProcessor RP = new RequestProcessor(DbAdminServiceProvider.class.getCanonicalName(), 1, true);
    protected SSLContext ssl;
    protected final String certName;

    protected DbAdminServiceProvider(final String endpoint, final String certName) {
        client = new DbAdminServiceClient(endpoint, this);
        this.certName = certName;
    }

    public static List<ProviderInfo> findAllProviders() {
        return Lookups.forPath("Provider").lookupAll(ProviderInfo.class).stream()
                .map(ProviderInfo.class::cast)
                .collect(Collectors.toList());
    }

    public static DbAdminServiceProvider create(final ProviderInfo info) {
        final LocalProperties attr = LocalProperties.find(info.getURL());
        final String endpoint = attr.getProperty("dbadmin.service.endpoint", createEndpointUrl(attr.getProperty("host")));
        final String certAlias = attr.getProperty("dbadmin.service.endpoint", AppProperties.privateKeyAlias(attr, info.getURL()));
        class DbAdminServiceProviderImpl extends DbAdminServiceProvider {

            public DbAdminServiceProviderImpl() {
                super(endpoint, certAlias);
            }

            @Override
            public ProviderInfo getInfo() {
                return info;
            }

            @Override
            public int hashCode() {
                int hash = 3;
                return 59 * hash + Objects.hashCode(getInfo().getURL());
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final DbAdminServiceProviderImpl other = (DbAdminServiceProviderImpl) obj;
                return Objects.equals(getInfo().getURL(), other.getInfo().getURL());
            }

        }
        return new DbAdminServiceProviderImpl();
    }

    static String createEndpointUrl(final String host) {
        return "https://" + host + ":8181/service/dbadmin";
    }

    public DbAdminService createDbAdminServicePort() throws IOException {
        try {
            return client.getBetulaServicePort();
        } catch (WebServiceException ex) {
            throw new IOException(ex);
        }
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
