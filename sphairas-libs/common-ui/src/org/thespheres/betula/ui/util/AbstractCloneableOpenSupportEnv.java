package org.thespheres.betula.ui.util;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import org.openide.windows.CloneableOpenSupport;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractCloneableOpenSupportEnv<C extends CloneableOpenSupport> implements CloneableOpenSupport.Env, Serializable {

    public static final long serialVersionUID = 1L;
    private transient PropertyChangeSupport pSupport; //do not initialize in constructor, missing after deserialization
    private transient VetoableChangeSupport vetoableChangeSupport; //see above

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
    public abstract C findCloneableOpenSupport();

}
