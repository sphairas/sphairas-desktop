/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.openide.util.Exceptions;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.util.WebUtil;
import org.thespheres.betula.services.ws.WebServiceProvider;

/**
 *
 * @author boris.heithecker
 */
public class UIExceptions {

    public enum ServiceFailureState {
        IGNORE, RETRY, ABANDON;

    }
    private final static Map<ServiceKey, ServiceFailures> SERVICE_FAILURE = new HashMap<>();

    private UIExceptions() {
    }

    public static void handle(Exception ex, Object[] sources) {
        Exceptions.printStackTrace(ex);//TODO identify repeated exception, log, notify, dialog respectively
    }

    public static ServiceFailureState handleServiceException(String service, String provider, final Exception ex) {
        ServiceKey key = new ServiceKey(service, provider);
        final ServiceFailures sf;
        synchronized (SERVICE_FAILURE) {
            sf = SERVICE_FAILURE.computeIfAbsent(key, k -> new ServiceFailures());
        }
        final ServiceFailureState ret;
        synchronized (sf) {
            sf.lastException = ex;
            final long newTime = System.currentTimeMillis();
            if (sf.lastExceptionTime > 0 && newTime - sf.lastExceptionTime > 60000) {
                sf.count = 0;
            }
            sf.lastExceptionTime = newTime;
            if (++sf.count > 5) {
                ret = ServiceFailureState.ABANDON;
            } else {
                ret = ServiceFailureState.RETRY;
            }
        }
        try {
            final WebServiceProvider wsp = WebProvider.find(service, WebServiceProvider.class);
            WebUtil.resetProvider(wsp, ex);
        } catch (NoProviderException nopex) {
        }
        return ret;
    }

    private final static class ServiceKey {

        private final String service;
        private final String provider;

        private ServiceKey(String service, String provider) {
            this.service = service;
            this.provider = provider;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + Objects.hashCode(this.service);
            hash = 53 * hash + Objects.hashCode(this.provider);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ServiceKey other = (ServiceKey) obj;
            if (!Objects.equals(this.service, other.service)) {
                return false;
            }
            return Objects.equals(this.provider, other.provider);
        }

    }

    private static class ServiceFailures {

        private Exception lastException;
        private long lastExceptionTime = -1l;
        private int count = 0;

    }
}
