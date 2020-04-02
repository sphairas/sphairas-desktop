/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import com.google.common.eventbus.EventBus;
import java.beans.PropertyChangeEvent;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ws.WebServiceProvider;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractDataItem {

    public static final String PROP_DISPLAY_NAME = "abstract-remote-display-name";
    final Map<String, ClientProperty> clientProperties = new HashMap<>();
    final ReferenceQueue rq = new ReferenceQueue();
    private final String provider;
    protected final EventBus events;

    protected AbstractDataItem(final String provider, final EventBus events) {
        this.provider = provider;
        this.events = events;
    }

    public String getWebServiceProvider() {
        return provider;
    }

    public EventBus getEventBus() {
        return events;
    }

    protected WebServiceProvider findWebServiceProvider() {
        return WebProvider.find(provider, WebServiceProvider.class);
    }

    public void putClientProperty(String key, Object value) {
        putClientProperty(key, value, true, true);
    }

    public void putClientProperty(final String key, final Object value, boolean strong, boolean firePropertyChange) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null.");
        }
        ClientProperty old;
        synchronized (clientProperties) {
            final ClientProperty cp = new ClientProperty(key, value, strong);
            old = clientProperties.get(key);
            if (value != null) {
                clientProperties.put(key, cp);
            } else {
                clientProperties.remove(key);
            }
        }
        final Object oldValue = old == null ? null : old.value;
        if (firePropertyChange && !Objects.equals(oldValue, value)) {
            final PropertyChangeEvent evt = new PropertyChangeEvent(this, key, oldValue, value);
            getEventBus().post(evt);
        }
    }

    public String getClientProperty(final String key) {
        synchronized (clientProperties) {
            return getClientProperty(key, String.class);
        }
    }

    public <T> T getClientProperty(final String key, final Class<T> type) {
        synchronized (clientProperties) {
            final ClientProperty cp = clientProperties.get(key);
            return cp != null ? cp.get(type) : null;
        }
    }

    public void removeClientProperty(String key) {
        synchronized (clientProperties) {
            clientProperties.remove(key);
        }
    }

//    public static RemoteLookup findRemoteLookup(String prov) {
//        final String np = LocalProperties.find(prov).getProperty("remoteLookup.providerURL", prov);
//        return RemoteLookup.get(np);
//    }
    final class ClientProperty extends WeakReference {

        private final Object value;
        private final String key;

        protected ClientProperty(String key, Object value, boolean strong) {
            super(value, rq);
            this.value = strong ? value : null;
            this.key = key;
        }

        @Override
        public boolean enqueue() {
            boolean ret = super.enqueue();
            if (ret) {
                clientProperties.remove(key);
            }
            return ret;
        }

        private <T> T get(Class<T> type) {
            return type.cast(value != null ? value : get());
        }

//        @Override
//        public int hashCode() {
//            int hash = 5;
//            hash = 29 * hash + Objects.hashCode(this.key);
//            return hash;
//        }
//        
//        @Override
//        public boolean equals(Object obj) {
//            if (obj == null) {
//                return false;
//            }
//            if (getClass() != obj.getClass()) {
//                return false;
//            }
//            final ClientProperty other = (ClientProperty) obj;
//            return Objects.equals(this.key, other.key);
//        }
    }
}
