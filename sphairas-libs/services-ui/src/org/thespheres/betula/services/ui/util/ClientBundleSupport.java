/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.thespheres.betula.document.AbstractMarker;
import org.thespheres.betula.services.ui.util.ClientBundleSupport.ReloadableClientMarker;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
class ClientBundleSupport extends AbstractClientBundleSupport<ReloadableClientMarker> {

    protected ResourceBundle bundle;
    private final String convention;

    ClientBundleSupport(final String convention, final String provider, final String resource, final Map<String, String> arg) {
        super(convention, provider, resource, arg);
        this.convention = convention;
    }

    @Override
    public String getConvention() {
        return convention;
    }

    @Override
    protected void reload() throws IllegalStateException {
        assert RP.isRequestProcessorThread();
        final ReloadableClientMarker[] before = elements;
        elements = null;
        try {
            bundle = HttpUtilities.fetchResourceBundle(service, propertiesUrl);
            //Do not use bundle.keySet() --> ordering not guaranteed
            //Ordering of elements 
            final Set<String> ids = Collections.list(bundle.getKeys()).stream()
                    .filter(key -> IDPATTERN.matcher(key).matches())
                    .collect(Collectors.toSet());
            final Set<String> retain;
            if (before != null) {
                retain = Arrays.stream(before)
                        .map(AbstractMarker::getId)
                        .filter(ids::contains)
                        .collect(Collectors.toSet());
            } else {
                retain = Collections.EMPTY_SET;
            }
            final List<ReloadableClientMarker> update = new ArrayList<>();
            if (before != null) {
                for (final ReloadableClientMarker old : before) {
                    if (retain.contains(old.getId())) {
                        old.setMessage(bundle.getString(old.getId()));
                        update.add(old);
                    }
                }
            }
            ids.stream()
                    .filter(id -> !retain.contains(id))
                    .map(id -> new ReloadableClientMarker(convention, id, bundle.getString(id)))
                    .forEach(update::add);
            final ReloadableClientMarker[] arr = update.stream()
                    .sorted(Comparator.comparing(AbstractMarker::getId))
                    .toArray(ReloadableClientMarker[]::new);
            elements = arr;
            checkModified.schedule(checkUpdatedInterval);
        } catch (IOException ex) {
            initEx = new IllegalStateException(ex);
            load.schedule(RETRY_TIME);
        }
        if (before != null && elements != null) {
            cSupport.fireChange();
        }
    }

    @Override
    protected void checkLastModified() {
        assert RP.isRequestProcessorThread();
        String lm = null;
        try {
            lm = HttpUtilities.fetchLastModified(service, propertiesUrl);
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(ClientBundleSupport.class).log(Level.FINE, ex.getLocalizedMessage(), ex);
        }
        final HttpUtilities.PropertyResourceBundleExt b = (HttpUtilities.PropertyResourceBundleExt) bundle;
        if (lm != null && !lm.equals(b.getLastModified())) {
            markForReload();
        } else {
            checkModified.schedule(checkUpdatedInterval);
        }
    }

    static class ReloadableClientMarker extends AbstractMarker implements Serializable {

        private String message;

        ReloadableClientMarker(String convention, String id, String mf) {
            super(convention, id, null);
            this.message = mf;
        }

        @Override
        public String getLongLabel(Object... args) {
            return MessageFormat.format(message, args);
        }

        protected String getMessage() {
            return message;
        }

        void setMessage(final String msg) {
            this.message = msg;
        }
    }

}
