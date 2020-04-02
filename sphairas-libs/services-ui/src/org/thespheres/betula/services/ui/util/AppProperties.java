/*
 * To change this license header, choose License Headers in Project AppProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import org.thespheres.betula.services.AppPropertyNames;
import org.thespheres.betula.services.ui.ConfigurationException;
import org.thespheres.betula.services.LocalProperties;

/**
 *
 * @author boris.heithecker
 */
public class AppProperties {

    public static String provider(final LocalProperties prop) {
        final String p = prop.getProperty(AppPropertyNames.LP_PROVIDER);
        if (p == null) {
            throw new ConfigurationException(prop, AppPropertyNames.LP_PROVIDER);
        }
        return p;
    }

    public static String privateKeyAlias(final LocalProperties prop, final String provider) {
        final String p = provider != null ? provider : prop.getProperty(AppPropertyNames.LP_PROVIDER, prop.getName());
        return prop.getProperty(AppPropertyNames.LP_CERTIFICATE_NAME, p);
    }

}
