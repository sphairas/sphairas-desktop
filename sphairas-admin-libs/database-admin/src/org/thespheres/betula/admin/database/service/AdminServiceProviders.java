/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.service;

import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ui.util.dav.URLs;

/**
 *
 * @author boris.heithecker@gmx.net
 */
public class AdminServiceProviders {

    public static AbstractDbAdminServiceProvider create(ProviderInfo pi) {
        if (URLs.serverVersionAtLeast(LocalFileProperties.find(pi), "2")) {
            return DbAdminRESTProvider.create(pi);
        } else {
            return DbAdminServiceProvider.create(pi);
        }
    }
}
