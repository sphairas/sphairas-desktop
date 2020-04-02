/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.client.jms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.jms.JMSEvent;
import org.thespheres.betula.services.util.MessageUtil;
import org.thespheres.betula.services.util.MessageUtil.Dispatch;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@Messages({"JMSTopicListenerService.error.title=Verbindungs-Fehler",
    "JMSTopicListenerService.error.message=Der Benachrichtigungsdienst {0} für {1} konnte nicht abonniert werden."})
public abstract class JMSTopicListenerService implements Runnable {

    public final RequestProcessor RP = new RequestProcessor(JMSTopicListenerService.class.getName(), 4);
    protected final String topicJNDI;
    protected boolean initialized = false;
    private final Map<Class, Set<JMSListener>> listeners = new HashMap<>();

    protected JMSTopicListenerService(String topic) {
        this.topicJNDI = topic;
    }

    public static JMSTopicListenerService find(final String providerUrl, final String topic) throws NoProviderException {
        return Lookup.getDefault().lookupAll(JMSTopicListenerServiceProvider.class).stream()
                .map(p -> p.getListenerService(providerUrl, topic))
                .filter(Objects::nonNull)
                .collect(CollectionUtil.requireSingleton())
                .orElseThrow(() -> new NoProviderException(JMSTopicListenerService.class, providerUrl + " [" + topic + "]"));
    }

    public static List<JMSTopicListenerService> findAll(final String providerUrl) throws NoProviderException {
        return Lookup.getDefault().lookupAll(JMSTopicListenerServiceProvider.class).stream()
                .flatMap(p -> p.getListenerServices(providerUrl).stream())
                .collect(Collectors.toList());
    }

    public abstract ProviderInfo getInfo();

    public String getTopicJNDIName() {
        return topicJNDI;
    }

    public <E extends JMSEvent> void registerListener(final Class<E> clz, final JMSListener<E> l) {
        synchronized (listeners) {
            listeners.computeIfAbsent(clz, k -> new HashSet<>())
                    .add(l);
        }
    }

    public <E extends JMSEvent> void unregisterListener(final JMSListener<E> l) {
        synchronized (listeners) {
            listeners.values().stream()
                    .forEach(s -> s.remove(l));
        }
    }

    public void run(final JMSEvent evt) {
        if (initialized && evt != null) {
            final JMSListener[] l;
            synchronized (listeners) {
                l = listeners.entrySet().stream()
                        .filter(e -> e.getKey().isAssignableFrom(evt.getClass()))
                        .flatMap(e -> e.getValue().stream())
                        .toArray(JMSListener[]::new);
            }
            final DispatchImpl dispatch = new DispatchImpl(this, l, evt);
            boolean d = MessageUtil.addDispatch(dispatch);
            if (!d) {
                dispatch.doDispatch();
            }
        }
    }

    public void initialize() {
        RP.post(this);
    }

    @Override
    public void run() {
        if (initialized || !RP.isRequestProcessorThread()) {
            return;
        }
        initListener();
    }

    protected abstract void initListener();
    
    static class DispatchImpl extends Dispatch {

        final JMSTopicListenerService service;
        final JMSListener[] listeners;
        final JMSEvent event;

        DispatchImpl(JMSTopicListenerService service, JMSListener[] listeners, JMSEvent event) {
            this.listeners = listeners;
            this.event = event;
            this.service = service;
        }

        private void doDispatch() {
            Arrays.stream(listeners).forEach(l -> l.onMessage(event));
        }

        @Override
        protected void dispatch() {
            service.RP.post(this::doDispatch, 0, Thread.NORM_PRIORITY);
        }
    }

}
