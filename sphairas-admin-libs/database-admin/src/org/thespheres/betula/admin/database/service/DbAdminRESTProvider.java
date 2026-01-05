/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.service;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import org.thespheres.betula.database.DbAdminService;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ui.util.AppProperties;

/**
 *
 * @author boris.heithecker
 */
public class DbAdminRESTProvider extends AbstractDbAdminServiceProvider {

    public static final int DEFAULT_PORT = 8181;
    public static final String PORT_PROP = "port";
    private final URI baseURI;

    private DbAdminRESTProvider(final ProviderInfo provider, final URI baseURI, final String certAlias) {
        super(provider, certAlias);
        this.baseURI = baseURI;
    }

    public static DbAdminRESTProvider create(final ProviderInfo provider) {
        final LocalProperties attr = LocalProperties.find(provider.getURL());
        String endpoint = attr.getProperty("betula.service.endpoint");
        URI base;
        if (endpoint != null) {
            base = URI.create(endpoint);
        } else {
            final int port = Optional.ofNullable(attr.getProperty(PORT_PROP))
                    .map(Integer::parseInt)
                    .orElse(DEFAULT_PORT);
            base = createBaseURI(attr.getProperty("host"), port);
        }
        final String certAlias = AppProperties.privateKeyAlias(attr, provider.getURL());
        return new DbAdminRESTProvider(provider, base, certAlias);
    }

    static URI createBaseURI(final String host, final int port) {
        return URI.create("https://" + host + ":" + Integer.toString(port) + "/admins/service/api/db/");
    }

    @Override
    public DbAdminService createDbAdminServicePort() throws IOException {
        return new DbAdminRESTClient(this, baseURI);
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
