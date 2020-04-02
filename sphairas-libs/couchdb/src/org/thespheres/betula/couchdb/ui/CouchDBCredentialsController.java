/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.couchdb.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author boris.heithecker
 */
public abstract class CouchDBCredentialsController extends OptionsPanelController {

    private CouchDBCredentialsPanel panel;
    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    @Override
    public final void update() {
        getPanel().load();
        changed = false;
    }

    @Override
    public final void applyChanges() {
//        SwingUtilities.invokeLater(() -> {
        if (isChanged()) {
            getPanel().store();
        }
        changed = false;
//        });
    }

    @Override
    public final void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public final boolean isValid() {
        return getPanel().valid();
    }

    @Override
    public final boolean isChanged() {
        return changed;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    private CouchDBCredentialsPanel getPanel() {
        if (panel == null) {
            panel = new CouchDBCredentialsPanel(this);
        }
        return panel;
    }

    final void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    public abstract String loadCouchDBDatabase();

    public abstract void storeCouchDBDatabase(String db);

    public abstract String loadCouchDBUser();

    public abstract void storeCouchDBUser(String user);

    public abstract boolean hasStoredPassword();

    public abstract void storeCouchDBPassword(char[] pw);

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
}
