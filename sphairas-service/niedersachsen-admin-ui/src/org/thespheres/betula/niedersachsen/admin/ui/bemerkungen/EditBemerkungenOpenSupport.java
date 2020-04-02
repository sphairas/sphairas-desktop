/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import java.io.IOException;
import org.netbeans.api.actions.Openable;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.util.Lookup;
import org.thespheres.betula.ui.util.AbstractCloneableOpenSupportEnv;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;

/**
 *
 * @author boris.heithecker
 */
class EditBemerkungenOpenSupport extends CloneableOpenSupport implements Openable {

    EditBemerkungenOpenSupport(final String p) {
        super(new LookupHelper(p));
    }

    @Override
    protected CloneableTopComponent createCloneableTopComponent() {
        return MultiViews.createCloneableMultiView("application/nds-report-notes", (LookupHelper) env);
    }

    @Override
    protected String messageOpening() {
        return null;
    }

    @Override
    protected String messageOpened() {
        return null;
    }

    static class LookupHelper extends AbstractCloneableOpenSupportEnv<EditBemerkungenOpenSupport> implements Lookup.Provider {

        private final String provider;

        LookupHelper(String provider) {
            this.provider = provider;
        }

        String getProvider() {
            return provider;
        }

        @Override
        public EditBemerkungenOpenSupport findCloneableOpenSupport() {
            try {
                return EditBemerkungenEnv.find(provider).getOpenSupport();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public Lookup getLookup() {
            try {
                return EditBemerkungenEnv.find(provider).getLookup();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

    }

}
