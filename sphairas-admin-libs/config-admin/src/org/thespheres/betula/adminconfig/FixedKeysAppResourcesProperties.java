/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.adminconfig.FixedKeysAppResourcesProperties.FixedKeyAppResourcesProperty;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.client.jms.JMSListener;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.services.jms.AppResourceEvent;
import org.thespheres.betula.services.jms.JMSTopic;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ui.util.dav.URLs;

/**
 *
 * @author boris
 */
public abstract class FixedKeysAppResourcesProperties<M> extends AppResourcesProperties<FixedKeyAppResourcesProperty> implements Runnable {

    public static final String SEPARATOR = "=";
    private IOException lastException;
    protected final SortedMap<String, M> versions = new TreeMap<>();
    private final RequestProcessor.Task loadTask;
    private final WebProvider service;
    private final URI uri;
    private final Listener listener = new Listener();
    private final String resource;
    private boolean dirty = false;
    private final String[] keys;

    @SuppressWarnings({"LeakingThisInConstructor"})
    public FixedKeysAppResourcesProperties(final String provider, final String resource, final String[] keys) {
        super(provider);
        this.resource = resource;
        this.keys = keys;
        final String base = URLs.adminResourcesDavBase(LocalProperties.find(provider));
        uri = URI.create(base + "/" + this.resource);
        service = WebProvider.find(provider, WebProvider.class);
        loadTask = RP.create(this);
        final JMSTopicListenerService jms = JMSTopicListenerService.find(provider, JMSTopic.APP_RESOURCES_TOPIC.getJmsResource());
        if (jms != null) {
            jms.registerListener(AppResourceEvent.class, listener);
        }
        loadTask.schedule(0);
    }

    public URI getUri() {
        return uri;
    }

    public String getResource() {
        return resource;
    }

    @Override
    public boolean isReadOnly() {
        return versions.isEmpty();
    }

    boolean markedDirty() {
        return dirty;
    }

    @Override
    public void run() {
        try {
            this.lastException = load();
        } catch (IOException ex) {
            this.lastException = ex;
        }
        if (this.lastException == null && !versions.isEmpty()) {
            final List<FixedKeyAppResourcesProperty> result = Arrays.stream(keys)
                    .map(this::createProperty)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            synchronized (items) {
                items.clear();
                items.addAll(result);
            }
            dirty = false;
            cSupport.fireChange();
        }
    }

    protected abstract FixedKeyAppResourcesProperty createProperty(final String key);

    protected IOException load() throws IOException {
        return HttpUtilities.get(service, uri, this::readVersion, null, false);
    }

    protected IOException readVersion(final String lastMod, final InputStream is) {
        M file = null;
        try {
            file = read(is);
            return null;
        } catch (IOException ex) {
            return ex;
        } finally {
            this.versions.put(lastMod, file);
        }
    }

    @Override
    public FixedKeyAppResourcesProperty createTemplateProperty() {
        throw new UnsupportedOperationException("New entries are not supported.");
    }

    @Messages("FixedKeysAppResourcesProperties.save.success=Die aktualisierte Resource {0} wurde erfolgreich hochgeladen.")
    @Override
    public void save() throws IOException {
        if (this.lastException == null && !versions.isEmpty()) {
            final List<FixedKeyAppResourcesProperty> snapshot = new ArrayList<>();
            synchronized (items) {
                items.stream()
                        .map(FixedKeyAppResourcesProperty.class::cast)
                        .forEach(snapshot::add);
            }
            final M orig = versions.get(versions.lastKey());
            byte[] bytes = write(orig, snapshot);
            HttpUtilities.put(service, uri, bytes, null, null);
            dirty = true;
            final String msg = NbBundle.getMessage(FixedKeysAppResourcesProperties.class, "FixedKeysAppResourcesProperties.save.success", getResource(), provider);
            StatusDisplayer.getDefault().setStatusText(msg);
            cSupport.fireChange();
        }

    }

    protected abstract M read(final InputStream is) throws IOException;

    protected abstract byte[] write(final M orig, final List<FixedKeyAppResourcesProperty> snapshot);

    private class Listener implements JMSListener<AppResourceEvent> {

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }

        @Override
        public void onMessage(final AppResourceEvent event) {
            if (Objects.equals(getResource(), event.getResource())) {
                loadTask.schedule(1500);
            }
        }
    }

    public class FixedKeyAppResourcesProperty extends AppResourcesProperty {

        private FixedKeyAppResourcesProperty(final String key, final String value) {
            super(key, value);
        }

        @Override
        public boolean setValue(final String text) {
            final String val = StringUtils.stripToNull(text);
            boolean ret = !Objects.equals(val, getValue());
            this.valueOverride = val;
            if (!Objects.equals(this.value, getValue())) {
                setStatus(UpdateStatus.MODIFIED);
            } else {
                setStatus(null);
            }
            if (ret) {
                cSupport.fireChange();
            }
            return ret;
        }

    }

}
