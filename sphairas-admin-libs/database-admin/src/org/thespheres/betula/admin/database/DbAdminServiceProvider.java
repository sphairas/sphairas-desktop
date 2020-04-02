/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import javax.net.ssl.SSLContext;
import javax.xml.ws.WebServiceException;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.admin.database.service.DbAdminService;
import org.thespheres.betula.admin.database.service.DbAdminServiceClient;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.WebProvider;
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

    public static DbAdminServiceProvider create(final Map<String, ?> props) {
        final String provider = (String) props.get("provider");
        final String alias = (String) props.get("alias");
        final String endpoint = (String) props.get("endpoint");
        final ProviderInfo info = ProviderRegistry.getDefault().get(provider);
        class DbAdminServiceProviderImpl extends DbAdminServiceProvider {

            public DbAdminServiceProviderImpl() {
                super(endpoint, alias);
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
