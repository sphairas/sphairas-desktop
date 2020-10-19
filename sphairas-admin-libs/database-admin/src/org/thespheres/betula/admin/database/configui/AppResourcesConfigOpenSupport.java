/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.configui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.util.NbBundle;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;
import org.thespheres.betula.adminconfig.ProviderReference;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;

/**
 *
 * @author boris.heithecker
 */
class AppResourcesConfigOpenSupport extends CloneableOpenSupport implements OpenCookie, EditCookie, CloseCookie {

    private static final Map<String, WeakReference<AppResourcesConfigOpenSupport>> ENV = new HashMap<>();

    AppResourcesConfigOpenSupport(final String prov) {
        super(new AppResourcesConfigOpenSupport.Env(prov));
    }

    static AppResourcesConfigOpenSupport find(final String prov) {
        AppResourcesConfigOpenSupport env;
        synchronized (ENV) {
            final WeakReference<AppResourcesConfigOpenSupport> ref = ENV.get(prov);
            if (ref == null || (env = ref.get()) == null) {
                env = new AppResourcesConfigOpenSupport(prov);
                ENV.put(prov, new WeakReference<>(env));
            }
        }
        return env;
    }

    Env getEnv() {
        return (Env) env;
    }

    @NbBundle.Messages(value = "AppResourcesConfigOpenSupport.messageOpening=Konfigurations-UI wird geöffnet.")
    @Override
    protected String messageOpening() {
        return NbBundle.getMessage(AppResourcesConfigOpenSupport.class, "AppResourcesConfigOpenSupport.messageOpening");
    }

    @Override
    protected String messageOpened() {
        return null;
    }

    @Override
    protected CloneableTopComponent createCloneableTopComponent() {
        return MultiViews.createCloneableMultiView(ProviderReference.MIME, (AppResourcesConfigOpenSupport.Env) env);
    }

    static class Env implements ProviderReference, CloneableOpenSupport.Env {

        public static final long serialVersionUID = 1L;
        private transient PropertyChangeSupport pSupport; //do not initialize in constructor, missing after deserialization
        private transient VetoableChangeSupport vetoableChangeSupport; //see above
        private final String provider;

        Env(final String prov) {
            super();
            this.provider = prov;
        }

        @Override
        public ProviderInfo getProviderInfo() {
            return ProviderRegistry.getDefault().get(provider);
        }

        protected PropertyChangeSupport getPropertyChangeSupport() {
            synchronized (this) {
                if (pSupport == null) {
                    pSupport = new PropertyChangeSupport(this);
                }
                return pSupport;
            }
        }

        protected VetoableChangeSupport getVetoableChangeSupport() {
            synchronized (this) {
                if (vetoableChangeSupport == null) {
                    vetoableChangeSupport = new VetoableChangeSupport(this);
                }
                return vetoableChangeSupport;
            }
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            getPropertyChangeSupport().addPropertyChangeListener(l);
        }

        @Override
        public void addVetoableChangeListener(VetoableChangeListener l) {
            getVetoableChangeSupport().addVetoableChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            getPropertyChangeSupport().removePropertyChangeListener(l);
        }

        @Override
        public void removeVetoableChangeListener(VetoableChangeListener l) {
            getVetoableChangeSupport().removeVetoableChangeListener(l);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isModified() {
            return false;
        }

        @Override
        public void markModified() throws IOException {
        }

        @Override
        public void unmarkModified() {
        }

        @Override
        public AppResourcesConfigOpenSupport findCloneableOpenSupport() {
            return AppResourcesConfigOpenSupport.find(provider);
        }
    }
}
