/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admindocsrv.action;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"ActionEnvironment.ambiguous.selection=Nicht eindeutige Auswahl."})
public class ActionEnvironment {

    private final WebServiceProvider service;
    private final LocalProperties properties;

    private ActionEnvironment(WebServiceProvider service, LocalProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    public WebServiceProvider getService() {
        return service;
    }

    public LocalProperties getProperties() {
        return properties;
    }

    public static ActionEnvironment create(final Lookup context) {
        LocalProperties properties = null;
        WebServiceProvider service = null;
        for (AbstractUnitOpenSupport auos : context.lookupAll(AbstractUnitOpenSupport.class)) {
            final LocalProperties lp;
            try {
                lp = auos.findBetulaProjectProperties();
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(ActionEnvironment.class).log(Level.FINE, ex.getLocalizedMessage(), ex);
                return null;
            }
            if (properties != null && !properties.getName().equals(lp.getName())) {
                final String msg = NbBundle.getMessage(ActionEnvironment.class, "ActionEnvironment.ambiguous.selection");
                PlatformUtil.getCodeNameBaseLogger(ActionEnvironment.class).log(LogLevel.FINE, msg);
                return null;
            }
            properties = lp;
            final WebServiceProvider wsp;
            try {
                wsp = auos.findWebServiceProvider();
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(ActionEnvironment.class).log(Level.FINE, ex.getLocalizedMessage(), ex);
                return null;
            }
            if (service != null && !service.getInfo().getURL().equals(wsp.getInfo().getURL())) {
                final String msg = NbBundle.getMessage(TargetDocumentDownloadAction.class, "ActionEnvironment.ambiguous.selection");
                PlatformUtil.getCodeNameBaseLogger(ActionEnvironment.class).log(LogLevel.FINE, msg);
                return null;
            }
            service = wsp;
        }
        return properties != null && service != null ? new ActionEnvironment(service, properties) : null;
    }

    public static ActionEnvironment create(final List<RemoteTargetAssessmentDocument> list) {
        final String provider = list.stream()
                .map(RemoteTargetAssessmentDocument::getProvider)
                .findAny()
                .orElse(null);

        if (provider == null) {
            return null;
        }

        final boolean allMatch = list.stream()
                .map(RemoteTargetAssessmentDocument::getProvider)
                .allMatch(provider::equals);

        if (!allMatch) {
            final String msg = NbBundle.getMessage(TargetDocumentDownloadAction.class, "ActionEnvironment.ambiguous.selection");
            PlatformUtil.getCodeNameBaseLogger(ActionEnvironment.class).log(LogLevel.FINE, msg);
            return null;
        }

        LocalFileProperties properties = LocalFileProperties.find(provider);
        WebServiceProvider service = null;
        try {
            service = WebProvider.find(provider, WebServiceProvider.class);
        } catch (NoProviderException ex) {
            PlatformUtil.getCodeNameBaseLogger(ActionEnvironment.class).log(Level.FINE, ex.getLocalizedMessage(), ex);
            return null;
        }
        return properties != null && service != null ? new ActionEnvironment(service, properties) : null;
    }
}
