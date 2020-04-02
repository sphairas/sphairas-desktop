/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.config;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

@NbBundle.Messages("EditStundentafelAction.name=Stundentafel bearbeiten")
final class EditStundentafelAction extends AbstractAction {

    protected final String provider;
    private static final Map<String, WeakReference<TopComponent>> TC = new HashMap<>();

    EditStundentafelAction(String provider) {
        super(NbBundle.getMessage(EditStundentafelAction.class, "EditStundentafelAction.name"));
        this.provider = provider;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            final TopComponent tc = findMultiView(provider);
            tc.open();
            tc.setVisible(true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static TopComponent findMultiView(final String prov) throws IOException {
        final WeakReference<TopComponent> ref = TC.get(prov);
        TopComponent tc;
        if (ref == null || (tc = ref.get()) == null) {
            //TODO no IO in EDT (env.getData()
            final EditStundentafelEnv env = EditStundentafelEnv.create(prov);
            env.getData().getNodeDelegate().setDisplayName("Dipslay");
            tc = MultiViews.createCloneableMultiView("text/curriculum-file+xml", new LookupHelper(env));
            TC.put(prov, new WeakReference<>(tc));
        }
        return tc;
    }

    static class LookupHelper implements Lookup.Provider, Serializable {

        static final long serialVersionUID = 42L;
        private Lookup lkp;

        private LookupHelper(EditStundentafelEnv env) {
            this.lkp = Lookups.fixed(env, env.getData());
        }

        @Override
        public synchronized Lookup getLookup() {
            return lkp;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            final EditStundentafelEnv env = lkp.lookup(EditStundentafelEnv.class);
            out.writeUTF(env.getProvider());
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            final String provider = in.readUTF();
            final EditStundentafelEnv env = EditStundentafelEnv.create(provider);
            this.lkp = Lookups.fixed(env, env.getData());
        }
    }
}
