/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.dav.DAVProp;
import org.thespheres.betula.services.dav.DisplayName;
import org.thespheres.betula.services.dav.Multistatus;
import org.thespheres.betula.services.dav.PropStat;
import org.thespheres.betula.services.dav.ResourceType;
import org.thespheres.betula.services.dav.Response;
import org.thespheres.betula.services.ui.ConfigurationException;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class MultistatusUtilities {

    public static String findLastModified(final Multistatus m) {
        return m.getResponses().stream()
                .collect(CollectionUtil.singleton())
                .flatMap(r -> r.getPropstat().stream()
                .filter(ps -> ps.getStatusCode() == 200)
                .collect(CollectionUtil.singleton()))
                .map(ps -> ps.getProp())
                .filter(DAVProp.class::isInstance)
                .map(DAVProp.class::cast)
                .map(dav -> dav.getGetLastModified())
                .map(lm -> lm.getValue())
                .orElse(null);
    }

    public static List<String> dir(final WebProvider wp, final URI uri) throws IOException {
        try {
            final Multistatus ms = HttpUtilities.getProperties(wp, uri, 1, true);
            //parse ms, trim, remove ".xml" set knownNames, checkName()
            final List<Response> rr = ms.getResponses();
            final List<String> ret = new ArrayList<>();
            for (final Response r : rr) {
                final List<PropStat> l = r.getPropstat();
                for (final PropStat ps : l) {
                    final int sc = ps.getStatusCode();
                    if (sc == 200) {
                        final DAVProp prop = (DAVProp) ps.getProp();
                        final boolean folder = Optional.ofNullable(prop.getResourcetype())
                                .map(ResourceType::getCollection)
                                .isPresent();
                        Optional.ofNullable(prop.getDisplayName())
                                .map(DisplayName::getValue)
                                .map(p -> folder ? p + "/" : p)
                                .ifPresent(ret::add);
                    }
                }
            }
            return ret;
        } catch (NoProviderException | ConfigurationException ex) {
            throw new IOException(ex);
        }
    }
}
