/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.adminconfig.DefaultAppResourcesProperties.DefaultAppResourcesProperty;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.client.jms.JMSListener;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.services.jms.AppResourceEvent;
import org.thespheres.betula.services.jms.JMSTopic;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris
 */
public class DefaultAppResourcesProperties extends AppResourcesProperties<DefaultAppResourcesProperty> implements Runnable {

    public static final String SEPARATOR = "=";
    private IOException lastException;
    protected final SortedMap<String, List<String>> versions = new TreeMap<>();
    private final RequestProcessor.Task loadTask;
    private final WebProvider service;
    private final URI uri;
    private final Listener listener = new Listener();
    private final String resource;
    private boolean dirty = false;

    @SuppressWarnings({"LeakingThisInConstructor"})
    public DefaultAppResourcesProperties(final String provider, final String resource) {
        super(provider);
        this.resource = resource;
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
            final List<DefaultAppResourcesProperty> result = new ArrayList<>();
            final List<String> l = versions.get(versions.lastKey());
            for (int i = 0; i < l.size(); i++) {
                final String line = l.get(i);
                final DefaultAppResourcesProperty parsed = parse(line, i);
                if (parsed != null) {
                    result.add(parsed);
                }
            }
            synchronized (items) {
                items.clear();
                items.addAll(result);
            }
            dirty = false;
            cSupport.fireChange();
        }
    }

    private DefaultAppResourcesProperty parse(final String line, final int index) {
        if (StringUtils.strip(line).startsWith("#")) {
            return null;
        }
        final int i = line.indexOf(SEPARATOR);
        if (i == -1) {
            return null;
        }
        final String key = StringUtils.stripToNull(line.substring(0, i));
        final String value = StringUtils.stripToNull(line.substring(i + 1));
        if (key == null || value == null) {
            return null;
        }
        return new DefaultAppResourcesProperty(key, value, index, line);
    }

    protected IOException load() throws IOException {
        return HttpUtilities.get(service, uri, this::read, null, false);
    }

    protected IOException read(final String lastMod, final InputStream is) {
        List<String> lines = Collections.EMPTY_LIST;
        try {
            lines = IOUtils.readLines(is, StandardCharsets.ISO_8859_1);
            return null;
        } catch (IOException ex) {
            return ex;
        } finally {
            this.versions.put(lastMod, lines);
        }
    }

    @Override
    public DefaultAppResourcesProperty createTemplateProperty() {
        final DefaultAppResourcesProperty ret = new DefaultAppResourcesProperty("key", "value");
        items.add(ret);
        cSupport.fireChange();
        return ret;
    }

    @Messages("DefaultAppResourcesProperties.save.success=Die aktualisierte Resource {0} wurde erfolgreich hochgeladen.")
    @Override
    public void save() throws IOException {
        if (this.lastException == null && !versions.isEmpty()) {
            final List<DefaultAppResourcesProperty> snapshot = new ArrayList<>();
            synchronized (items) {
                snapshot.addAll(items);
            }
            final List<String> orig = versions.get(versions.lastKey());
            final List<String> update = new ArrayList<>();
            for (int i = 0; i < orig.size(); i++) {
                final String origLine = orig.get(i);
                final int current = i;
                final DefaultAppResourcesProperty found = snapshot.stream()
                        .filter(p -> p.getIndex() == current)
                        .filter(p -> p.getOriginalLine().equals(origLine))
                        .collect(CollectionUtil.requireSingleOrNull());
                if (found != null) {
                    if (found.isForRemoval()) {
                        continue;
                    } else if (found.isModified()) {
                        update.add(found.toEntry());
                        continue;
                    }
                }
                update.add(origLine);
            }
            snapshot.stream()
                    .filter(p -> p.isTemplate())
                    .filter(p -> Objects.nonNull(p.getValue()) && Objects.nonNull(p.getKey()))
                    .map(DefaultAppResourcesProperty::toEntry)
                    .forEach(update::add);
            final byte[] bytes;
            try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                IOUtils.writeLines(update, IOUtils.LINE_SEPARATOR_UNIX, baos, StandardCharsets.ISO_8859_1);
                bytes = baos.toByteArray();
            }
            HttpUtilities.put(service, uri, bytes, null, null);
            dirty = true;
            final String msg = NbBundle.getMessage(DefaultAppResourcesProperties.class, "DefaultAppResourcesProperties.save.success", getResource(), provider);
            StatusDisplayer.getDefault().setStatusText(msg);
            cSupport.fireChange();
        }

    }

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

    public class DefaultAppResourcesProperty extends AppResourcesProperty {

        private final int index;
        private final String originalLine;
        private String keyOverride;

        private DefaultAppResourcesProperty(final String key, final String value, final int index, final String orig) {
            super(key, value);
            this.index = index;
            this.originalLine = orig;
        }

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        private DefaultAppResourcesProperty(final String key, final String value) {
            this(null, null, -1, null);
            this.keyOverride = key;
            this.valueOverride = value;
            setStatus(UpdateStatus.TEMPLATE);
        }

        public int getIndex() {
            return index;
        }

        String getOriginalLine() {
            return originalLine;
        }

        @Override
        public String getKey() {
            if (isTemplate()) {
                return this.keyOverride;
            }
            return super.getKey();
        }

        @Override
        public boolean setKey(final String value) {
            if (isTemplate()) {
                final String key = StringUtils.stripToNull(value);
                boolean ret = !Objects.equals(key, getKey());
                this.keyOverride = key;
                return ret;
            }
            return super.setKey(value);
        }

        @Override
        public boolean setValue(final String text) {
            final String val = StringUtils.stripToNull(text);
            boolean ret = !Objects.equals(val, getValue());
            if (val != null) {
                this.valueOverride = val;
                if (!Objects.equals(this.value, getValue())) {
                    if (!isTemplate()) {
                        setStatus(UpdateStatus.MODIFIED);
                    }
                } else {
                    setStatus(null);
                }
            } else {
                this.valueOverride = null;
                if (!isTemplate()) {
                    setStatus(UpdateStatus.REMOVAL);
                }
            }
            if (ret) {
                cSupport.fireChange();
            }
            return ret;
        }

        String toEntry() {
            return getKey() + SEPARATOR + getValue();
        }

    }

}
