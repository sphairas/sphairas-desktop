/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.Lookup;

/**
 *
 * @author boris.heithecker
 */
public class ProviderFileLists {

    public static final String METAINF_PROVIDER_FILE_LISTS = "META-INF/provider-file-list-names";

    public static String[] findProviderFileListNames() throws IOException {
        final Enumeration<URL> res = Lookup.getDefault().lookup(ClassLoader.class).getResources(METAINF_PROVIDER_FILE_LISTS);
        final Set<String> coll = new HashSet<>();
        while (res.hasMoreElements()) {
            final URL r = res.nextElement();
            try (final InputStream is = r.openStream()) {
                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
                    buffer.lines().forEach(coll::add);
                }
            }
        }
        return coll.stream().toArray(String[]::new);
    }
}
