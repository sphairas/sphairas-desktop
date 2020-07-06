/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.services.util.AbstractReloadableAssessmentConvention;
import org.thespheres.betula.services.util.XmlAssessmentConventionSupport;
import org.thespheres.betula.xmldefinitions.XmlAssessmentConventionDefintion;

/**
 *
 * @author boris.heithecker
 */
public class ClientReloadableAssessmentConvention extends AbstractReloadableAssessmentConvention {

    static final int DEFAULT_CHECK_INTERVAL = 1000 * 60;
    static final int WAIT_TIME = 12000;
    static final int RETRY_TIME = 60000;
    protected final RequestProcessor RP;
    protected final RequestProcessor.Task load;
    protected final String resource;
    protected final WebProvider service;
    protected RuntimeException initEx;
    protected final int interval;
    private final XmlAssessmentConventionDefintion[] definition = new XmlAssessmentConventionDefintion[]{null};
    private String lastModified = null;

    ClientReloadableAssessmentConvention(final String provider, final String name, final String resource, final int interval) {
        super(provider, name);
        service = WebProvider.find(provider, WebProvider.class);
        this.resource = resource;
        RP = new RequestProcessor(name);
        this.interval = interval;
        load = RP.post(this::reload);
    }

    @Override
    protected XmlAssessmentConventionDefintion getDefinition() {
        ensureLoaded();
        return definition[0];
    }

    protected void ensureLoaded() {
        synchronized (load) {
            if (!load.isFinished()) {
                try {
                    load.waitFinished(WAIT_TIME);
                } catch (final InterruptedException ex) {
                    final String msg = "Could not load AssessmentConvention " + getName() + " in " + Integer.toString(WAIT_TIME) + "ms.";
                    initEx = new IllegalStateException(msg, ex);
                    load.schedule(RETRY_TIME);
                }
            }
        }
        if (initEx != null) {
            throw initEx;
        }
        if (definition[0] == null) {
            throw new IllegalStateException("No XmlAssessmentConventionDefintion: " + getName());
        }
    }

    @Override
    protected synchronized void markForReload() {
        load.schedule(0);
    }

    protected void reload() throws IllegalStateException {
        assert RP.isRequestProcessorThread();
        final String base = URLs.adminBase(LocalProperties.find(provider));
        final String href = base + resource;
        final URI uri = URI.create(href);
        XmlAssessmentConventionDefintion result = null;
        try {
            result = HttpUtilities.get(service, uri, (lm, is) -> {
                final XmlAssessmentConventionDefintion c = XmlAssessmentConventionSupport.load(is);
                lastModified = lm;
                return c;
            }, lastModified, false);
        } catch (IOException ex) {
            initEx = new IllegalStateException(ex);
            load.schedule(RETRY_TIME);
        }
        if (result != null) {
            synchronized (definition) {
                definition[0] = result;
            }
            cSupport.fireChange();
        }
        load.schedule(interval);
    }

    @ServiceProvider(service = AbstractReloadableAssessmentConvention.Factory.class)
    public static class ClientReloadableAssessmentConventionFactory extends AbstractReloadableAssessmentConvention.Factory {

        @Override
        protected AbstractReloadableAssessmentConvention create(final String provider, final String name, final String resource, final Map<String, String> arg) throws IllegalStateException {
            int i = Optional.ofNullable(arg)
                    .map(a -> a.get("check-update-interval"))
                    .map(v -> {
                        try {
                            return Integer.valueOf(v);
                        } catch (NumberFormatException nfex) {
                            return null;
                        }
                    })
                    .orElse(DEFAULT_CHECK_INTERVAL);
            return new ClientReloadableAssessmentConvention(provider, name, resource, i);
        }

    }

}
