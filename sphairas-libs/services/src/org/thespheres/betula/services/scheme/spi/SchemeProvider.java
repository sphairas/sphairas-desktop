/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.scheme.spi;

import org.openide.util.Lookup;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public interface SchemeProvider {

    public <G extends Scheme> G[] getAllSchemes(Class<G> type);

    public <G extends Scheme> G getScheme(String id, Class<G> type);

    public ProviderInfo getInfo();

    public static SchemeProvider find(final String url) throws NoProviderException {
        return Lookup.getDefault().lookupAll(SchemeProvider.class).stream()
                .map(SchemeProvider.class::cast)
                .filter(wp -> wp.getInfo().getURL().equals(url))
                .collect(CollectionUtil.requireSingleton())
                .orElseThrow(() -> new NoProviderException(SchemeProvider.class, url));
    }
}
