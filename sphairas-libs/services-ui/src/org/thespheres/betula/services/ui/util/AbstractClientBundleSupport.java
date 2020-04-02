/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.services.util.BundleSupport;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 * @param <M>
 */
public abstract class AbstractClientBundleSupport<M extends Marker> extends BundleSupport<M> {

    static final int DEFAULT_CHECK_INTERVAL = 60000;
    static final int WAIT_TIME = 12000;
    static final int RETRY_TIME = 60000;
    protected final RequestProcessor RP;
    protected final RequestProcessor.Task load;
    protected final String propertiesUrl;
    protected final WebProvider service;
    protected RuntimeException initEx;
    protected final RequestProcessor.Task checkModified;
    protected final int checkUpdatedInterval;

    protected AbstractClientBundleSupport(final String name, final String provider, final String resource, final Map<String, String> arg) {
        super();
        service = WebProvider.find(provider, WebProvider.class);
        propertiesUrl = resource;
        RP = new RequestProcessor(name != null ? name : resource);
        checkUpdatedInterval = Optional.ofNullable(arg)
                .map(a -> a.get("check-update-interval"))
                .map(v -> {
                    try {
                        return Integer.valueOf(v);
                    } catch (NumberFormatException nfex) {
                        return null;
                    }
                })
                .orElse(DEFAULT_CHECK_INTERVAL);

        load = RP.post(this::reload);
        checkModified = RP.create(this::checkLastModified);
    }

    @Override
    protected void ensureLoaded() {
        synchronized (load) {
            if (!load.isFinished()) {
                try {
                    load.waitFinished(WAIT_TIME);
                } catch (InterruptedException ex) {
                    final String msg = "Could not load MarkerConvention " + getConvention() + " in " + Integer.toString(WAIT_TIME) + "ms.";
                    initEx = new IllegalStateException(msg, ex);
                    load.schedule(RETRY_TIME);
                }
            }
        }
        if (initEx != null) {
            throw initEx;
        }
        assert elements != null;
    }

    @Override
    protected synchronized void markForReload() {
        load.schedule(0);
    }

    @Override
    protected abstract void reload() throws IllegalStateException;

    protected abstract void checkLastModified();

    @ServiceProvider(service = BundleSupport.Factory.class, position = 1000)
    public static class Factory extends BundleSupport.Factory {

        @Override
        protected BundleSupport<?> create(final String name, final String provider, final String resource, final Map<String, String> arg) {
            final LocalProperties lp = LocalProperties.find(provider);
            final String base = URLs.adminResourcesDavBase(lp);
            final String url = base + resource;
            if (resource.endsWith(".xml")) {
                if(name != null) {
                    final String msg = "Xml marker resource " + resource + " provides a \"name\" in its loadable settings. Please ensure that this name corresponds to the name in the xml definitions file.";
                    PlatformUtil.getCodeNameBaseLogger(AbstractClientBundleSupport.Factory.class).log(Level.INFO, msg);
                }
                return new ClientXmlDefinitionSupport(name, provider, url, arg);
            } else {
                return new ClientBundleSupport(name, provider, url, arg);
            }
        }
    }
}
