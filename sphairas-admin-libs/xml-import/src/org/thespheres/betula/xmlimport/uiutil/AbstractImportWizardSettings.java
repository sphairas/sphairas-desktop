/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.HashMap;
import org.openide.WizardDescriptor;
import org.thespheres.betula.xmlimport.ImportTarget;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
public abstract class AbstractImportWizardSettings<I extends ImportTarget> implements ImportWizardSettings<I> {

    protected final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    protected final HashMap<String, Object> properties = new HashMap<>(7);
    protected WizardDescriptor panel;

    /**
     * Allows Panels that use WizardDescriptor as settings object to store
     * additional settings into it.
     *
     * @param name name of the property
     * @param value value of property
     */
    public void putProperty(final String name, final Object value) {
        Object oldValue;
        synchronized (this) {
            oldValue = properties.get(name);
            properties.put(name, value);
        }
        if (WizardDescriptor.PROP_ERROR_MESSAGE.equals(name) && panel != null) {
            // #76318: New Entity wizard shows unreadable red error
            panel.putProperty(name, value);
        }
        if ((WizardDescriptor.PROP_WARNING_MESSAGE.equals(name) || WizardDescriptor.PROP_INFO_MESSAGE.equals(name)) && panel != null) {
            panel.putProperty(name, value);
        }
        // bugfix #27738, firing changes in a value of the property
        pSupport.firePropertyChange(name, oldValue, value);
    }

    public synchronized <T> T getProperty(final String name, final Class<T> type) {
        try {
            return (properties == null) ? null : (T) properties.get(name);
        } catch (final ClassCastException cce) {
            return null;
        }
    }

    public synchronized Object getProperty(final String name) {
        return getProperty(name, Object.class);
    }

    public void initialize(WizardDescriptor panel) throws IOException {
        this.panel = panel;
    }

    @Override
    public I getImportTargetProperty() {
        return (I) getProperty(AbstractFileImportAction.IMPORT_TARGET);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pSupport.removePropertyChangeListener(listener);
    }

}
