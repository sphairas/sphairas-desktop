/*
 * To change this license header, choose License Headers in Project AppProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.admin.services.PlatformWsJMSTopicListenerServiceProvider;
import org.thespheres.betula.adminconfig.ConfigNodeTopComponentNodeList;
import org.thespheres.betula.adminconfig.ConfigurationEventBus;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ServiceConstants;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.client.jms.JMSListener;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.services.implementation.ui.impl.LocalFilePropertiesProviderImpl.FilePropertiesImpl;
import org.thespheres.betula.services.implementation.ui.web.SSLWebServiceProvider;
import org.thespheres.betula.services.jms.AppResourceEvent;
import org.thespheres.betula.services.ui.util.AppProperties;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.services.ui.web.SSLUtil;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
public class SyncedProviderInstance {

    private static final Map<String, SyncedProviderInstance> INSTANCES = new ConcurrentHashMap<>();
    private static boolean initialized;
    private final String provider;
    private final Path baseDir;
    private final FilePropertiesImpl[] properties = new FilePropertiesImpl[]{null};
    private final SSLWebServiceProvider[] serviceProvider = new SSLWebServiceProvider[]{null};
    private final PlatformWsJMSTopicListenerServiceProvider[] jmsProvider = new PlatformWsJMSTopicListenerServiceProvider[]{null};
    final RequestProcessor eventsrp = new RequestProcessor("EVENTS", 1);
    final EventBus events = new AsyncEventBus(eventsrp);
    final ConfigurationEventBusImpl cfgbus = new ConfigurationEventBusImpl();
    final ConfigurationsImpl config = new ConfigurationsImpl(this);
    final Updater updater;
    final Map<String, ConfigNodeTopComponentNodeList> configNodes = new HashMap<>();
    final Listener jmsListener = new Listener();

    private SyncedProviderInstance(String provider, Path baseDir) {
        this.provider = provider;
        this.baseDir = baseDir;
        this.updater = new Updater(this);
//        this.events
    }

    static void ensureInitialized() {
        synchronized (INSTANCES) {
            if (!initialized) {
                final Path base = ServiceConstants.providerConfigBase();
                try {
                    Files.createDirectories(base);
                } catch (IOException ex) {
                    PlatformUtil.getCodeNameBaseLogger(LocalFilePropertiesProviderImpl.class).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                    return;
                }
                try (final DirectoryStream<Path> ds = Files.newDirectoryStream(base, Files::isDirectory)) {
                    for (final Path p : ds) {
                        final Path pPath = p.resolve("provider");
                        if (Files.exists(pPath)) {
                            final List<String> l = Files.readAllLines(pPath, StandardCharsets.UTF_8);
                            String name;
                            if (l.size() == 1 && (name = StringUtils.trimToNull(l.get(0))) != null) {
                                if (!name.startsWith("#")) {
                                    SyncedProviderInstance.add(name, p, false);
                                    PlatformUtil.getCodeNameBaseLogger(LocalFilePropertiesProviderImpl.class).log(Level.CONFIG, "Registered provider {0}", name);
                                }
                            }
                        }
                    }
                    initialized = true;
                } catch (IOException ex) {
                    PlatformUtil.getCodeNameBaseLogger(LocalFilePropertiesProviderImpl.class).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                }
            }
        }
    }

    public static Map<String, SyncedProviderInstance> getInstances() {
        ensureInitialized();
        return INSTANCES;
    }

    static void add(final String provider, final Path dir, final boolean newProvider) {
        synchronized (INSTANCES) {
            if (INSTANCES.containsKey(provider)) {
                throw new IllegalArgumentException("Provider " + provider + " already registered.");
            }
            final SyncedProviderInstance add = new SyncedProviderInstance(provider, dir);
            if (!newProvider) {
                add.checkProvider();
            } else {
                final int delay = getStartupUpdateDelay();
                //Update layers before running other updates
                final RequestProcessor.Task lu = LayerProv.fireUpdate();
                lu.addTaskListener(t -> add.enqueue(delay));
                //TODO: fire change
            }
            INSTANCES.put(provider, add);
        }
    }

    @NbBundle.Messages("checkProvider.message=Remote ({0}) and local ({1}) names not equal.")
    private void checkProvider() {
        try {
            final String name = providerName();
            if (name != null && !name.equals(provider)) {
                final String msg = NbBundle.getMessage(SyncedProviderInstance.class, "checkProvider.message", name, provider);
                throw new IllegalStateException(msg);
            }
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(SyncedProviderInstance.class).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    private String providerName() throws IOException {
        //We can't use this.findLocalFileProperties() because INSTANCES aren't initialized yet
        final Path user = baseDir.resolve(LocalFileProperties.USER_PROPERTIES_FILE);
        final Map<String, String> m = Files.readAllLines(user).stream()
                .collect(Collectors.toMap(s -> s.substring(0, s.indexOf("=")), s -> s.substring(s.indexOf("=") + 1)));
        class Prop implements LocalProperties {

            @Override
            public String getName() {
                return provider;
            }

            @Override
            public Map<String, String> getProperties() {
                return m;
            }

            @Override
            public String getProperty(String name) {
                return m.get(name);
            }

        }
        final LocalProperties prop = new Prop();
        final String url = URLs.providerName(prop);
        final URI uri = URI.create(url);
        final String certAlias = AppProperties.privateKeyAlias(prop, provider);
        final SSLContext ssl = SSLUtil.createSSLContext(certAlias);
        final String hostname = prop.getProperty("host-common-name");
        class Web implements WebProvider.SSL {

            @Override
            public SSLContext getSSLContext() {
                return ssl;
            }

            @Override
            public HostnameVerifier getHostnameVerifier() {
                if (hostname != null) {
                    final HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
                    return hostnameVerifier;
                }
                return WebProvider.SSL.super.getHostnameVerifier();
            }

            @Override
            public RequestProcessor getDefaultRequestProcessor() {
                throw new UnsupportedOperationException("Not called.");
            }

            @Override
            public ProviderInfo getInfo() {
                throw new UnsupportedOperationException("Not called.");
            }

        }
        return HttpUtilities.get(new Web(), uri, null, false);
    }

    public String getProvider() {
        return provider;
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public boolean enqueue(final int delay) {
        final boolean disabled = NbPreferences.forModule(SyncedProviderInstance.class).getBoolean("provider.update.disable." + provider, false);
        if (!disabled) {
            if (delay >= 0) {
                updater.task.schedule(delay);
            } else {
                updater.run();
            }
            return true;
        }
        return false;
    }

    public void submit(final Runnable task) {
        updater.rp.submit(task);
    }

    public void setLastModified(String resource, String lm) throws IOException {
        updater.setLastModified(resource, lm);
    }

    public LocalFileProperties findLocalFileProperties() {
        synchronized (properties) {
//            updater();
            if (properties[0] == null) {
                final Path p = baseDir.resolve("default.properties");
                try {
                    properties[0] = new LocalFilePropertiesProviderImpl.FilePropertiesImpl(provider, p, null);
                    events.register(properties[0]);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return properties[0];
    }

    void resetLocalFileProperties() {
        synchronized (properties) {
            properties[0] = null;
        }
    }

    void postEvent(final ProviderSyncEventImpl evt) {
        events.post(evt);
    }

    List<ConfigNodeTopComponentNodeList> getConfigNodeTopComponentNodes() {
        final List<ConfigNodeTopComponentNodeList.Factory> fac = ConfigNodeTopComponentNodeFacImpl.factories();
        return fac.stream()
                .map(f -> configNodes.computeIfAbsent(f.getName(), k -> f.create(provider, null)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public <W extends WebProvider> W findWebProvider(final Class<W> subType) {
        if (subType.isAssignableFrom(SSLWebServiceProvider.class)) {
            if (serviceProvider[0] == null) {
                serviceProvider[0] = SSLWebServiceProvider.create(provider, findLocalFileProperties());
            }
            return (W) serviceProvider[0];
        }
        return null;
    }

    List<JMSTopicListenerService> getJMSListenerServices() {
        synchronized (jmsProvider) {
            if (jmsProvider[0] == null) {
                final LocalFileProperties prop = findLocalFileProperties();
                final String host = prop.getProperty("host");
                final String certAlias = AppProperties.privateKeyAlias(prop, provider);
                jmsProvider[0] = new PlatformWsJMSTopicListenerServiceProvider(provider, host, 7781, true, certAlias);
            }
        }
        return jmsProvider[0].getListenerServices(provider);
    }

    static int getStartupUpdateDelay() {
        return NbPreferences.forModule(SyncedProviderInstance.class).getInt("startup.updater.delay", 0);
    }

    class ConfigurationEventBusImpl extends ConfigurationEventBus {

        ConfigurationEventBusImpl() {
            super(provider);
        }

        @Override
        protected EventBus getEvents() {
            return events;
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
        public void onMessage(AppResourceEvent event) {
            enqueue(2000);
        }
    }

    @OnStart
    public static class StartupReg implements Runnable {

        @Override
        public void run() {
            SyncedProviderInstance.ensureInitialized();
            if (!Boolean.getBoolean("SyncedProviderInstance.update.disable")) {
                final int delay = getStartupUpdateDelay();
                SyncedProviderInstance.getInstances().forEach((p, i) -> i.enqueue(delay));
            }
        }

    }
}
