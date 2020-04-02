/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import org.netbeans.api.actions.Openable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@Messages("EditBemerkungenAction.name=Bemerkungen bearbeiten")
public final class EditBemerkungenAction extends AbstractAction {

    protected final String provider;
    private static final Map<String, WeakReference<TopComponent>> TC = new HashMap<>();

    EditBemerkungenAction(String provider) {
        super(NbBundle.getMessage(EditBemerkungenAction.class, "EditBemerkungenAction.name"));
        this.provider = provider;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            EditBemerkungenEnv env = EditBemerkungenEnv.find(provider);
            env.getLookup().lookup(Openable.class).open();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
