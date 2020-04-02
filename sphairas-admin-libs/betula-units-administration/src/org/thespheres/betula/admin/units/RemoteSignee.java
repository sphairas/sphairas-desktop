/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import com.google.common.eventbus.EventBus;
import java.util.Objects;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.ConfigurationPanelLookupHint;

/**
 *
 * @author boris.heithecker
 */
public class RemoteSignee extends AbstractDataItem implements ConfigurationPanelLookupHint {

    public static final String REMOTE_SIGNEE_HINT = "RemoteSignee";
//    public static final String PROP_REMOTE_LOOKUP = "remote-signee-remote-lookup";
    public static final String PROP_WEBSERVICE_PROVIDER = "remote-signee-web-service";
    private final Signee signee;
    private final Signees signees;

    RemoteSignee(Signees signees, Signee sig, EventBus events) {
        super(signees.getProviderUrl(), events);
        this.signees = signees;
        this.signee = sig;
    }

    @Override
    public WebServiceProvider findWebServiceProvider() {
        return WebProvider.find(signees.getProviderUrl(), WebServiceProvider.class);
    }

    public Signee getSignee() {
        return signee;
    }

    public Signees getSignees() {
        return signees;
    }

    public String getCommonName() {
        String cn = signees.getSignee(signee);
        return cn != null ? cn : signee.getId();
    }

    @Override
    public String getContentType() {
        return REMOTE_SIGNEE_HINT;
    }

    @Override
    public String getDisplayName() {
        return getCommonName();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.signee);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteSignee other = (RemoteSignee) obj;
        return Objects.equals(this.signee, other.signee);
    }

    public static class DocumentInfo {

        private final DocumentId document;
        private final String[] entitlement;

        public DocumentInfo(DocumentId document, String[] entitlement) {
            this.document = document;
            this.entitlement = entitlement;
        }

        public DocumentId getDocument() {
            return document;
        }

        public String[] getEntitlement() {
            return entitlement;
        }

    }

}
