/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.util;

import org.thespheres.betula.admin.units.impl.MarkerDecorationImpl;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.openide.util.Lookup;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.document.model.MarkerDecoration;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.services.ws.WebServiceProvider;

/**
 *
 * @author boris.heithecker
 */
public abstract class OpenSupportProperties {

    private final Map<String, Object> jMSTopicListenerServices = new HashMap<>();
    private final HashMap<String, SchemeProvider> schemeProviders = new HashMap<>();
    private Object termSchedule;
    private Object namingResolver;
    private Object webServiceProvider;
    private Object documentsModel;
    private Optional<Signees> signees;
    private Optional<Units> units;
    private Object markerDecoration;
    private final String name;

    protected OpenSupportProperties(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract LocalProperties findBetulaProjectProperties() throws IOException;

    public WebServiceProvider findWebServiceProvider() throws IOException {
        if (webServiceProvider == null) {
            final String url = findProviderUrl();
            if (url == null) {
                webServiceProvider = new IOException("No provider URL defined for project \"" + getName() + "\".");
            } else {
                try {
                    webServiceProvider = WebProvider.find(url, WebServiceProvider.class);
                } catch (NoProviderException npex) {
                    webServiceProvider = new IOException("No web service provider found for project \"" + getName() + "\".");
                }
            }
        }
        if (webServiceProvider instanceof IOException) {
            throw (IOException) webServiceProvider;
        }
        return (WebServiceProvider) webServiceProvider;
    }

    public TermSchedule findTermSchedule() throws IOException {
        if (termSchedule == null) {
            SchemeProvider schemeProvider;
            try {
                schemeProvider = findSchemeProvider("termSchedule.providerURL");
                if (schemeProvider != null) {
                    termSchedule = schemeProvider.getScheme(findBetulaProjectProperties().getProperty("termSchemeId", TermSchedule.DEFAULT_SCHEME), TermSchedule.class);
                } else {
                    termSchedule = new IOException("No term schedule found for project \"" + getName() + "\".");
                }
            } catch (IOException ex) {
                termSchedule = ex;
            }
        }
        if (termSchedule instanceof IOException) {
            throw (IOException) termSchedule;
        }
        return (TermSchedule) termSchedule;
    }

    public JMSTopicListenerService findJMSTopicListenerService(final String topic) throws IOException {
        Object jms = jMSTopicListenerServices.get(topic);
        if (jms == null) {
            final LocalProperties lfp = findBetulaProjectProperties();
            final String jmsProp = lfp.getProperty("jms.providerURL", findProviderUrl());
            try {
                jms = JMSTopicListenerService.find(jmsProp, topic);
            } catch (NoProviderException npex) {
                jms = new IOException(npex);
            }
            jMSTopicListenerServices.put(topic, jms);
        }
        if (jms instanceof IOException) {
            throw (IOException) jms;
        }
        return (JMSTopicListenerService) jms;
    }

    public String findDomainProviderUrl() throws IOException {
        String url = findProviderUrl();
        return findBetulaProjectProperties().getProperty("domain.providerURL", url);
    }

    public String findProviderUrl() throws IOException {
        return findBetulaProjectProperties().getProperty("providerURL");
    }

    public SchemeProvider findSchemeProvider(String betulaProperty) throws IOException {
        if (schemeProviders.containsKey(betulaProperty)) {
            return schemeProviders.get(betulaProperty);
        }
        final LocalProperties bpp = findBetulaProjectProperties();
        final String tsProvider = bpp.getProperty(betulaProperty);
        SchemeProvider sp = null;
        if (tsProvider != null) {
            for (final SchemeProvider p : Lookup.getDefault().lookupAll(SchemeProvider.class)) {
                if (p.getInfo().getURL().equals(tsProvider)) {
                    sp = p;
                    break;
                }
            }
        }
        schemeProviders.put(betulaProperty, sp);
        return sp;
    }

    public NamingResolver findNamingResolver() throws IOException {
        if (namingResolver == null) {
            try {
                final String provider = findProviderUrl();
                final String np = findBetulaProjectProperties().getProperty("naming.providerURL", provider);
                if (np != null) {
                    namingResolver = NamingResolver.find(np);
                }
            } catch (IOException ex) {
                namingResolver = ex;
            }
            if (namingResolver == null) {
                namingResolver = new IOException("No naming resolver found for project \"" + getName() + "\".");
            }
        }
        if (namingResolver instanceof IOException) {
            throw (IOException) namingResolver;
        }
        return (NamingResolver) namingResolver;
    }

    public DocumentsModel findDocumentsModel() throws IOException {
        if (documentsModel == null) {
            documentsModel = createDocumentsModel();
        }
        if (documentsModel instanceof IOException) {
            throw (IOException) documentsModel;
        }
        return (DocumentsModel) documentsModel;
    }

    protected DocumentsModel createDocumentsModel() throws IOException {
        final DocumentsModel dm = new DocumentsModel();
        dm.initialize(findBetulaProjectProperties().getProperties());
        return dm;
    }

    public MarkerDecoration findMarkerDecoration() throws IOException {
        if (markerDecoration == null) {
            markerDecoration = createMarkerDecoration();
        }
        if (markerDecoration instanceof IOException) {
            throw (IOException) markerDecoration;
        }
        return (MarkerDecoration) markerDecoration;
    }

    protected MarkerDecoration createMarkerDecoration() throws IOException {
        return new MarkerDecorationImpl((LocalFileProperties) findBetulaProjectProperties());
    }

    public Optional<Signees> getSignees() {
        if (signees == null) {
            try {
                String url = findProviderUrl();
                signees = Signees.get(url);
            } catch (IOException ex) {
                signees = Optional.empty();
            }
        }
        return signees;
    }

    public Optional<Units> getUnits() {
        if (units == null) {
            try {
                String url = findProviderUrl();
                units = Units.get(url);
            } catch (IOException ex) {
                units = Optional.empty();
            }
        }
        return units;
    }

}
