/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.xmldefinitions.XmlMarkerConventionDefinition;
import org.thespheres.betula.xmldefinitions.XmlMarkerDefinition;

/**
 *
 * @author boris.heithecker
 */
class ClientXmlDefinitionSupport extends AbstractClientBundleSupport<XmlMarkerDefinition> {

    private XmlMarkerConventionDefinition definition;
    private String lastModified;
    private final String fastName;

    ClientXmlDefinitionSupport(final String name, final String provider, final String resource, final Map<String, String> arg) {
        super(name, provider, resource, arg);
        this.fastName = name;
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
    protected String getConvention() {
        if (fastName != null) {
            return fastName;
        }
        ensureLoaded();
        return definition.getName();
    }

    @Override
    protected String getDisplayName() {
        ensureLoaded();
        return definition.getDisplayName();
    }

    @Override
    protected synchronized void markForReload() {
        load.schedule(0);
    }

    @Override
    protected void reload() throws IllegalStateException {
        assert RP.isRequestProcessorThread();
        final XmlMarkerDefinition[] before = elements;
        elements = null;
        try {
            definition = HttpUtilities.fetchXmlMarkerDefinition(service, propertiesUrl, this);
            XmlMarkerDefinition[] arr = definition.getMarkerSubsets().stream()
                    .flatMap(set -> set.getMarkerDefinitions().stream())
                    .toArray(XmlMarkerDefinition[]::new);
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

    void setLastModified(String lm) {
        lastModified = lm;
    }

    @Override
    protected void checkLastModified() {
        assert RP.isRequestProcessorThread();
        String lm = null;
        try {
            lm = HttpUtilities.fetchLastModified(service, propertiesUrl);
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(ClientXmlDefinitionSupport.class).log(Level.FINE, ex.getLocalizedMessage(), ex);
        }
        if (lm != null && lastModified != null && !lastModified.equals(lm)) {
            markForReload();
        } else {
            checkModified.schedule(checkUpdatedInterval);
        }
    }

}
