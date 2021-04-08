/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.thespheres.betula.services.ProviderRegistry;

/**
 *
 * @author boris.heithecker@gmx.net
 */
public class ProviderUtilities {

    public static Preferences findPreferences(final String provider) {
        final String cnb = ProviderRegistry.getDefault().get(provider).getURL();
        final String path = cnb.replaceAll("\\.", "/");
        return NbPreferences.root().node(path);
    }
}
