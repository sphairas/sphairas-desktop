/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.ks;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@OptionsPanelController.SubRegistration(
        location = "Security",
        displayName = "#KeystoresOptionsPanelController.displayName",
        keywords = "#KeystoresOptionsPanelController.keywords",
        keywordsCategory = "Security/Keystores",
        position = 10000000)
@NbBundle.Messages({"KeystoresOptionsPanelController.displayName=Schlüsselbund",
    "KeystoresOptionsPanelController.keywords=security,keystores"})
public class KeystoresOptionsPanelController extends OptionsPanelController {

    private KeystoresOptionsPanelComponent panel;
    private boolean changed;
    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public final void update() {
        getPanel().load();
        changed = false;
    }

    @Override
    public final void applyChanges() {
//        SwingUtilities.invokeLater(() -> {
//            getPanel().apply();
//            changed = false;
//        });
        if (isChanged()) {
            getPanel().apply();
        }
        changed = false;
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
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    private KeystoresOptionsPanelComponent getPanel() {
        if (panel == null) {
            panel = new KeystoresOptionsPanelComponent(this);
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

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

}
