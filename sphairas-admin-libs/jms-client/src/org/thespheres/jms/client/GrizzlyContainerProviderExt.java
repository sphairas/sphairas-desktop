/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.jms.client;

import javax.net.ssl.SSLContext;
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.SslEngineConfigurator;
import org.glassfish.tyrus.container.grizzly.client.GrizzlyClientContainer;
import org.glassfish.tyrus.container.grizzly.client.GrizzlyContainerProvider;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.services.ui.web.SSLUtil;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = ContainerProvider.class, supersedes = "org.glassfish.tyrus.container.grizzly.client.GrizzlyContainerProvider")
public class GrizzlyContainerProviderExt extends GrizzlyContainerProvider {

    @Override
    protected WebSocketContainer getContainer() {
        final ClientManager client = ClientManager.createClient(GrizzlyClientContainer.class.getName());
        final Boolean ssl = WsJMSTopicListenerService.SSL.get();
        if (ssl != null && ssl) {
            final SSLContext ctx = SSLUtil.createSSLContext(WsJMSTopicListenerService.CERT_NAME.get());            
            final SslEngineConfigurator sslEngineConfigurator = new SslEngineConfigurator(ctx, true, false, false);
            client.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator);
        }
        return client;
    }

}
