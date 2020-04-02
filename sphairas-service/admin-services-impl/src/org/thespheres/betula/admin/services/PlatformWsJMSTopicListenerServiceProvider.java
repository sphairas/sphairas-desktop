/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.services;

import java.util.Map;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.swing.Icon;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.services.client.jms.JMSTopicListenerServiceProvider;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.jms.client.WsJMSTopicListenerService;
import org.thespheres.jms.client.WsJMSTopicListenerServiceProvider;

/**
 *
 * @author boris.heithecker
 */
public class PlatformWsJMSTopicListenerServiceProvider extends WsJMSTopicListenerServiceProvider implements JMSTopicListenerServiceProvider {

    public PlatformWsJMSTopicListenerServiceProvider() {
    }

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public PlatformWsJMSTopicListenerServiceProvider(final String provider, final String host, final int port, final boolean ssl, final String certName) {
        addDefaultServices(provider, host, port, ssl, certName);
    }

    public static WsJMSTopicListenerServiceProvider create(final Map<String, ?> attr) {
        final String host = (String) attr.get("host");
        final int port = (Integer) attr.get("port");
        final String provider = (String) attr.get("provider");
        final String alias = (String) attr.get("certificate-name");
        final Boolean secure = (Boolean) attr.get("ssl");
        return new PlatformWsJMSTopicListenerServiceProvider(provider, host, port, secure != null ? secure : false, alias);
    }

    protected void addDefaultServices(final String provider, final String host, final int port, final boolean ssl, final String certName) {
        final String addressList = createAddressList(host, port, ssl);
        addDefaultServices(provider, addressList, ssl ? certName : null);
    }

    @Override
    protected void onInitialisationException(JMSException ex, WsJMSTopicListenerService service) {
        initialisationException(ex, service.getTopicJNDIName(), service.getAddressList());
    }

    static String createAddressList(String host, int port, boolean secure) {
        final String p = Integer.toString(port);
        return !secure ? "mqws://" + host + ":" + p + "/wsjms"
                : "mqwss://" + host + ":" + p + "/wssjms";
    }

    static void initialisationException(JMSException ex, String topicJNDI, String addressList) {
        /*Do not log exceptions during startup,
            will cause user notification.
            Log later....
         */
        Logger.getLogger(JMSTopicListenerService.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage(), ex);
        final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        final String title = NbBundle.getMessage(JMSTopicListenerService.class, "JMSTopicListenerService.error.title");
        final String message = NbBundle.getMessage(JMSTopicListenerService.class, "JMSTopicListenerService.error.message", addressList, topicJNDI);
        NotificationDisplayer.getDefault()
                .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }
}
