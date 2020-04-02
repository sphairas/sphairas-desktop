/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.ui.impl;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.logging.Level;
import javax.swing.JCheckBox;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.admin.units.RemoteSignee;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.gpuntis.impl.StudenplanUpdater;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.ui.ConfigurationPanelComponent;
import org.thespheres.betula.ui.ConfigurationPanelContentTypeRegistration;
import org.thespheres.betula.ui.ConfigurationPanelComponentProvider;
import org.thespheres.betula.ui.util.AbstractCheckBoxConfigPanel;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.xmlimport.ImportUtil;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"UpdatesOfSigneeConfigurationPanel.checkbox.name=Änderungen/Vertretungen abonnieren",
    "UpdatesOfSigneeConfigurationPanel.message.http.status=Das Subscriptions-Update für \"{0}\" hat den Status-Code {1} (\"{2}\") zurückgegeben."})
public class UpdatesOfSigneeConfigurationPanel extends AbstractCheckBoxConfigPanel<RemoteSignee> {

    public static final String SYS_PROP_SUBSCRIPTION_OF_UPDATES_SUPPORTED = "subscription.of.updates.supported";

    @SuppressWarnings({"LeakingThisInConstructor"})
    private UpdatesOfSigneeConfigurationPanel(JCheckBox component) {
        super(component);
    }

    @Override
    protected Boolean getCurrentValue() {
        if (current != null) {
            Boolean v = current.getClientProperty("subscribeUpdates", Boolean.class);
            return v != null ? v : Boolean.FALSE;
        }
        return null;
    }

    private void updateSelectionIfCurrent(Signee sig) {
        if (current != null && current.getSignee().equals(sig)) {
            updateValue();
        }
    }

    @Override
    protected void updateValue(boolean cn) {
        if (current != null) {
            final RequestProcessor rl = Util.RP(current.getWebServiceProvider());
            rl.post(new Put(current, cn), 0, Thread.NORM_PRIORITY);
        }
    }

    @Override
    protected void onContextChange(Lookup context) {
        current = context.lookup(RemoteSignee.class);
        if (current != null) {
            Boolean value = current.getClientProperty("subscribeUpdates", Boolean.class);
            if (value == null && current.getClientProperty("subscribeUpdates.lastTime", Long.class) == null) {
                final RequestProcessor rl = Util.RP(current.getWebServiceProvider());
                rl.post(new Fetch(current), 0, 6);
            }
        }
    }

    private WebTarget target() throws NoProviderException {
        WebProvider wp = WebProvider.find(current.getWebServiceProvider(), WebProvider.class);
        String base = LocalProperties.find(wp.getInfo().getURL()).getProperty("admin.base.url");
        String url = base + "calendar/resource"; ///untis/ignored/ignored/lesson-data";
        //                  ClientConfig config = new ClientConfig();
        final Client client = ClientBuilder.newBuilder()
                .sslContext(((WebProvider.SSL) wp).getSSLContext())
                //                .hostnameVerifier(arg0)
                .build();
        final WebTarget target = client.target(url);
        return target;
    }

    private class Fetch implements Runnable {

        private final RemoteSignee rsignee;

        private Fetch(RemoteSignee rsignee) {
            this.rsignee = rsignee;
        }

        @Override
        public void run() {
            WebTarget target = target();

            long lt = System.currentTimeMillis();

            final Response resp;
            try {
                resp = target.path("untis")
                        .path("subscription-types")
                        .path(rsignee.getSignee().toString())
                        .request(MediaType.TEXT_PLAIN)
                        .get();
            } catch (Exception e) {
                e.printStackTrace(ImportUtil.getIO().getErr());
                return;
            }
            final Response.StatusType statusInfo = resp.getStatusInfo();
            if (statusInfo.getStatusCode() != Response.Status.OK.getStatusCode()) {
                String msg2 = NbBundle.getMessage(StudenplanUpdater.class, "StudenplanUpdater.message.http.status", rsignee.getDisplayName(), statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
                PlatformUtil.getCodeNameBaseLogger(UpdatesOfSigneeConfigurationPanel.class).log(Level.SEVERE, msg2);
                Util.notify(rsignee.getSignee(), current.findWebServiceProvider().getInfo());
                return;
            }
            final String types = resp.readEntity(String.class);
            boolean m = types != null ? Arrays.stream(types.split(",")).anyMatch("updates"::equals) : false;
            rsignee.putClientProperty("subscribeUpdates", m);
            rsignee.putClientProperty("subscribeUpdates.lastTime", lt);
            EventQueue.invokeLater(() -> updateSelectionIfCurrent(rsignee.getSignee()));

        }

    }

    private class Put implements Runnable {

        private final RemoteSignee rsignee;
        private final String value;

        private Put(RemoteSignee rsignee, boolean update) {
            this.rsignee = rsignee;
            this.value = update ? "updates" : "";
        }

        @Override
        public void run() {
            WebTarget target = target();

            long lt = System.currentTimeMillis();

            final Response resp;
            try {
                resp = target.path("untis")
                        .path("subscription-types")
                        .path(rsignee.getSignee().toString())
                        .request()
                        .put(Entity.entity(value, MediaType.TEXT_PLAIN), Response.class);
            } catch (Exception e) {
                e.printStackTrace(ImportUtil.getIO().getErr());
                return;
            }
            final Response.StatusType statusInfo = resp.getStatusInfo();
            if (statusInfo.getStatusCode() != Response.Status.OK.getStatusCode()) {
                String msg2 = NbBundle.getMessage(StudenplanUpdater.class, "StudenplanUpdater.message.http.status", rsignee.getDisplayName(), statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
                PlatformUtil.getCodeNameBaseLogger(UpdatesOfSigneeConfigurationPanel.class).log(Level.SEVERE, msg2);
                Util.notify(rsignee.getSignee(), current.findWebServiceProvider().getInfo());
                return;
            }
            rsignee.putClientProperty("subscribeUpdates", "updates".equals(value));
            rsignee.putClientProperty("subscribeUpdates.lastTime", lt);
            EventQueue.invokeLater(() -> updateSelectionIfCurrent(rsignee.getSignee()));
        }

    }

    @ConfigurationPanelContentTypeRegistration(contentType = "RemoteSignee", position = 20000)
    public static class Registration implements ConfigurationPanelComponentProvider {

        @Override
        public ConfigurationPanelComponent createConfigurationPanelComponent() {
            if (!Boolean.getBoolean(SYS_PROP_SUBSCRIPTION_OF_UPDATES_SUPPORTED)) {
                return null;
            }
            final JCheckBox cb = new JCheckBox();
            final String n = NbBundle.getMessage(UpdatesOfSigneeConfigurationPanel.class, "UpdatesOfSigneeConfigurationPanel.checkbox.name");
            cb.setName(n);
            return new UpdatesOfSigneeConfigurationPanel(cb);
        }

    }

}
