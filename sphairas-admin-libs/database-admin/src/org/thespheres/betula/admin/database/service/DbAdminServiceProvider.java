/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.service;

import java.io.IOException;
import java.util.Objects;
import javax.xml.ws.WebServiceException;
import org.thespheres.betula.database.DbAdminService;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ui.util.AppProperties;

/**
 *
 * @author boris.heithecker
 */
public class DbAdminServiceProvider extends AbstractDbAdminServiceProvider {

    protected final DbAdminServiceClient client;

    protected DbAdminServiceProvider(final ProviderInfo pi, final String endpoint, final String certName) {
        super(pi, certName);
        client = new DbAdminServiceClient(endpoint, this);
    }

    public static DbAdminServiceProvider create(final ProviderInfo info) {
        final LocalProperties attr = LocalProperties.find(info.getURL());
        final String endpoint = attr.getProperty("dbadmin.service.endpoint", createEndpointUrl(attr.getProperty("host")));
        final String certAlias = attr.getProperty("dbadmin.service.endpoint", AppProperties.privateKeyAlias(attr, info.getURL()));
        return new DbAdminServiceProvider(info, endpoint, certAlias);
    }

    @Override
    public DbAdminService createDbAdminServicePort() throws IOException {
        try {
            return client.getBetulaServicePort();
        } catch (WebServiceException ex) {
            throw new IOException(ex);
        }
    }

    static String createEndpointUrl(final String host) {
        return "https://" + host + ":8181/service/dbadmin";
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
        final DbAdminServiceProvider other = (DbAdminServiceProvider) obj;
        return Objects.equals(getInfo().getURL(), other.getInfo().getURL());
    }
}
