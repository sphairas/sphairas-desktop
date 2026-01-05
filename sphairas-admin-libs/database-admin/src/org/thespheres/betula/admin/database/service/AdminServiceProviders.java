/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.service;

import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.ProviderInfo;

/**
 *
 * @author boris.heithecker@gmx.net
 */
public class AdminServiceProviders {

    public static AbstractDbAdminServiceProvider create(ProviderInfo pi) {
        String serverVersion = LocalFileProperties.find(pi).getProperty("server.version", "1");
        if ("2".equals(serverVersion)) {
            return DbAdminRESTProvider.create(pi);
        } else {
            return DbAdminServiceProvider.create(pi);
        }
    }
}
