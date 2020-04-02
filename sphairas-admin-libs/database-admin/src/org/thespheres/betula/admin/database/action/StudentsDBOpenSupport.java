/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.util.NbBundle;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.utilities.VCardStudentsUtil;

/**
 *
 * @author boris.heithecker
 */
class StudentsDBOpenSupport extends CloneableOpenSupport implements OpenCookie, EditCookie, CloseCookie {

    public static final String STUDENTSDB_MIME = "text/term-report-file+xml";
    private final VCardStudents students;
    private static final Map<String, WeakReference<StudentsDBOpenSupport>> ENV = new HashMap<>();

    // , NavigatorLookupHint, Lookup.Provider
    StudentsDBOpenSupport(final String prov) throws IOException {
        super(new StudentsDBOpenSupport.Env(prov));
        ConfigurableImportTarget config = null;
        try {
            config = ConfigurableImportTarget.Factory.find(prov, ConfigurableImportTarget.class, Product.NO);
        } catch (NoProviderException e) {
            throw new IOException(e);
        }
        students = VCardStudentsUtil.findFromConfiguration(config);
    }

    static StudentsDBOpenSupport find(final String prov) throws IOException {
        StudentsDBOpenSupport env;
        synchronized (ENV) {
            final WeakReference<StudentsDBOpenSupport> ref = ENV.get(prov);
            if (ref == null || (env = ref.get()) == null) {
                env = new StudentsDBOpenSupport(prov);
                ENV.put(prov, new WeakReference<>(env));
            }
        }
        return env;
    }

    @NbBundle.Messages(value = "StudentsDBOpenSupport.messageOpening=Datenbank wird geöffnet.")
    @Override
    protected String messageOpening() {
        return NbBundle.getMessage(StudentsDBOpenSupport.class, "StudentsDBOpenSupport.messageOpening");
    }

    @Override
    protected String messageOpened() {
        return null;
    }

    @Override
    protected CloneableTopComponent createCloneableTopComponent() {
        return new StudentsDBTopComponent((StudentsDBOpenSupport.Env) env);
    }

    public VCardStudents getVCardStudents() {
        return students;
    }

    static class Env implements Serializable, CloneableOpenSupport.Env {

        //Lookup.Provider,
        public static final long serialVersionUID = 1L;
        private transient PropertyChangeSupport pSupport; //do not initialize in constructor, missing after deserialization
        private transient VetoableChangeSupport vetoableChangeSupport; //see above
        private final String provider;

        Env(final String prov) {
            super();
            this.provider = prov;
        }

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
        public StudentsDBOpenSupport findCloneableOpenSupport() {
            try {
                return StudentsDBOpenSupport.find(provider);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
